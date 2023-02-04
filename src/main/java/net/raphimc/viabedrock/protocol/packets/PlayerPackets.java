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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.SpawnPositionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class PlayerPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESPAWN, ClientboundPackets1_19_3.PLAYER_POSITION, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
                    final short state = wrapper.read(Type.UNSIGNED_BYTE); // state
                    wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id

                    if (state != 1) { // SERVER_READY
                        wrapper.cancel();
                        return;
                    }

                    System.out.println(position);

                    wrapper.write(Type.DOUBLE, (double) position.x()); // x
                    wrapper.write(Type.DOUBLE, (double) position.y() - 1.62D); // y
                    wrapper.write(Type.DOUBLE, (double) position.z()); // z
                    wrapper.write(Type.FLOAT, 0F); // yaw
                    wrapper.write(Type.FLOAT, 0F); // pitch
                    wrapper.write(Type.BYTE, (byte) 0b11000); // flags | keep rotation
                    wrapper.write(Type.VAR_INT, 0); // teleport id
                    wrapper.write(Type.BOOLEAN, false); // dismount vehicle

                    { // Close the "Downloading Terrain" screen
                        final SpawnPositionStorage spawnPositionStorage = wrapper.user().get(SpawnPositionStorage.class);
                        final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);

                        final PacketWrapper spawnPosition = PacketWrapper.create(ClientboundPackets1_19_3.SPAWN_POSITION, wrapper.user());
                        spawnPosition.write(Type.POSITION1_14, spawnPositionStorage.getSpawnPosition(chunkTracker.getDimensionId())); // position
                        spawnPosition.write(Type.FLOAT, 0F); // angle
                        spawnPosition.send(BedrockProtocol.class);
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19_3.CLIENT_STATUS, ServerboundBedrockPackets.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    final int action = wrapper.read(Type.VAR_INT); // action

                    if (action != 0) { // PERFORM_RESPAWN
                        wrapper.cancel();
                        return;
                    }
                    final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

                    wrapper.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // position
                    wrapper.write(Type.UNSIGNED_BYTE, (short) 2); // state | 2 = CLIENT_READY
                    wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, entityTracker.getClientPlayer().runtimeId()); // runtime entity id
                });
            }
        });
    }

}