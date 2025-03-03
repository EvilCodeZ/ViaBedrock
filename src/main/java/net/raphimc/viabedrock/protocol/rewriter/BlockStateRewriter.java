/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.protocol.rewriter;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.fastutil.ints.*;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.util.HashedPaletteComparator;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.BlockProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BlockStateRewriter extends StoredObject {

    private final Int2IntMap blockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Java
    private final Int2IntMap legacyBlockStateIdMappings = new Int2IntOpenHashMap(); // Bedrock -> Bedrock
    private final Map<BlockState, Integer> blockStateTagMappings = new HashMap<>(); // Bedrock -> Bedrock
    private final IntList waterIds = new IntArrayList(); // Bedrock

    public BlockStateRewriter(final UserConnection user, final BlockProperties[] blockProperties) {
        super(user);

        this.blockStateIdMappings.defaultReturnValue(-1);
        this.legacyBlockStateIdMappings.defaultReturnValue(-1);

        final List<BlockState> bedrockBlockStates = new ArrayList<>(BedrockProtocol.MAPPINGS.getBedrockBlockStates());
        final Map<BlockState, Integer> javaBlockStates = BedrockProtocol.MAPPINGS.getJavaBlockStates();
        final Map<BlockState, BlockState> bedrockToJavaBlockStates = BedrockProtocol.MAPPINGS.getBedrockToJavaBlockStates();

        for (BlockProperties blockProperty : blockProperties) {
            bedrockBlockStates.add(BlockState.AIR.withNamespacedIdentifier(blockProperty.name()));
        }
        bedrockBlockStates.sort((a, b) -> HashedPaletteComparator.INSTANCE.compare(a.getNamespacedIdentifier(), b.getNamespacedIdentifier()));

        for (int bedrockId = 0; bedrockId < bedrockBlockStates.size(); bedrockId++) {
            final BlockState bedrockBlockState = bedrockBlockStates.get(bedrockId);
            this.blockStateTagMappings.put(bedrockBlockState, bedrockId);

            if (bedrockBlockState.getNamespacedIdentifier().equals("minecraft:water") || bedrockBlockState.getNamespacedIdentifier().equals("minecraft:flowing_water")) {
                this.waterIds.add(bedrockId);
            }

            if (bedrockBlockState.getIdentifier().contains("hanging_sign")) continue;
            if (bedrockBlockState.getIdentifier().contains("bamboo")) continue;
            if (bedrockBlockState.getIdentifier().equals("chiseled_bookshelf")) continue;
            if (bedrockBlockState.getIdentifier().equals("mangrove_propagule")) continue;

            if (!bedrockToJavaBlockStates.containsKey(bedrockBlockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing Bedrock -> Java block state mapping: " + bedrockBlockState);
                continue;
            }

            final BlockState javaBlockState = bedrockToJavaBlockStates.get(bedrockBlockState);
            if (!javaBlockStates.containsKey(javaBlockState)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing java block state mapping: " + javaBlockState);
                continue;
            }

            final int javaId = javaBlockStates.get(javaBlockState);
            this.blockStateIdMappings.put(bedrockId, javaId);
        }

        for (Int2ObjectMap.Entry<BlockState> entry : BedrockProtocol.MAPPINGS.getLegacyBlockStates().int2ObjectEntrySet()) {
            final int legacyId = entry.getIntKey();
            final int bedrockId = this.blockStateTagMappings.get(entry.getValue());
            if (bedrockId == -1) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Legacy block state " + entry.getValue() + " is not mapped to a modern block state");
                continue;
            }

            this.legacyBlockStateIdMappings.put(legacyId, bedrockId);
        }
    }

    public int bedrockId(final Tag bedrockBlockStateTag) {
        BlockState blockState = BlockState.fromNbt((CompoundTag) bedrockBlockStateTag);
        int runtimeId = this.bedrockId(blockState);
        if (runtimeId == -1) {
            final String convertedNamespacedIdentifier = BedrockProtocol.MAPPINGS.getLegacyToModernBlockIdentifiers().get(blockState.getNamespacedIdentifier());
            if (convertedNamespacedIdentifier != null) {
                blockState = blockState.withNamespacedIdentifier(convertedNamespacedIdentifier);
            }
            runtimeId = this.bedrockId(blockState);
            if (runtimeId == -1) {
                blockState = BedrockProtocol.MAPPINGS.getDefaultBlockStates().getOrDefault(blockState.getNamespacedIdentifier(), null);
                if (blockState != null) {
                    runtimeId = this.bedrockId(blockState);
                }
            }
        }

        return runtimeId;
    }

    public int bedrockId(final BlockState bedrockBlockState) {
        return this.blockStateTagMappings.getOrDefault(bedrockBlockState, -1);
    }

    public int bedrockId(final int legacyBlockStateId) {
        return this.legacyBlockStateIdMappings.get((legacyBlockStateId >> 4) << 6 | legacyBlockStateId & 15);
    }

    public int javaId(final int bedrockBlockStateId) {
        return this.blockStateIdMappings.get(bedrockBlockStateId);
    }

    public int waterlog(final int javaBlockStateId) {
        if (BedrockProtocol.MAPPINGS.getPreWaterloggedStates().contains(javaBlockStateId)) {
            return javaBlockStateId;
        }

        final BlockState waterlogged = BedrockProtocol.MAPPINGS.getJavaBlockStates().inverse().get(javaBlockStateId).withProperty("waterlogged", "true");
        return BedrockProtocol.MAPPINGS.getJavaBlockStates().getOrDefault(waterlogged, -1);
    }

    public boolean isWater(final int bedrockBlockStateId) {
        return this.waterIds.contains(bedrockBlockStateId);
    }

}
