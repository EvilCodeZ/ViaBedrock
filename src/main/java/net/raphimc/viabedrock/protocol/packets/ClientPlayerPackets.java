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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerActionTypes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.RespawnState;
import net.raphimc.viabedrock.protocol.data.enums.java.ClientStatus;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ClientPlayerPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESPAWN, ClientboundPackets1_19_4.PLAYER_POSITION, wrapper -> {
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final short state = wrapper.read(Type.UNSIGNED_BYTE); // state
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id

            if (state != RespawnState.SERVER_READY) {
                wrapper.cancel();
                return;
            }

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            clientPlayer.setPosition(position);

            if (!clientPlayer.isInitiallySpawned()) {
                clientPlayer.setRespawning(true);
            } else {
                clientPlayer.sendPlayerActionPacketToServer(PlayerActionTypes.RESPAWN, -1);
                clientPlayer.closeDownloadingTerrainScreen();
            }

            clientPlayer.writePlayerPositionPacketToClient(wrapper, true, true);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAYER_ACTION, null, wrapper -> {
            wrapper.cancel();
            wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
            final int action = wrapper.read(BedrockTypes.VAR_INT); // action
            wrapper.read(BedrockTypes.BLOCK_POSITION); // block position
            wrapper.read(BedrockTypes.BLOCK_POSITION); // result position
            wrapper.read(BedrockTypes.VAR_INT); // face

            final ClientPlayerEntity clientPlayer = wrapper.user().get(EntityTracker.class).getClientPlayer();
            if (action == PlayerActionTypes.DIMENSION_CHANGE_SUCCESS && clientPlayer.isChangingDimension()) {
                clientPlayer.closeDownloadingTerrainScreen();
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CORRECT_PLAYER_MOVE_PREDICTION, null, wrapper -> {
            wrapper.cancel();
            BedrockProtocol.kickForIllegalState(wrapper.user(), "Received CorrectPlayerMovePrediction packet, but the client does not support movement corrections.");
        });

        protocol.registerServerbound(ServerboundPackets1_19_4.CLIENT_STATUS, ServerboundBedrockPackets.RESPAWN, wrapper -> {
            final int action = wrapper.read(Type.VAR_INT); // action

            if (action != ClientStatus.PERFORM_RESPAWN) {
                wrapper.cancel();
                return;
            }
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);

            wrapper.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // position
            wrapper.write(Type.UNSIGNED_BYTE, RespawnState.CLIENT_READY); // state
            wrapper.write(BedrockTypes.UNSIGNED_VAR_LONG, entityTracker.getClientPlayer().runtimeId()); // runtime entity id
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.PLAYER_MOVEMENT, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.PLAYER_POSITION, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.PLAYER_POSITION_AND_ROTATION, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.FLOAT), wrapper.read(Type.FLOAT), wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.PLAYER_ROTATION, ServerboundBedrockPackets.MOVE_PLAYER, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().updatePlayerPosition(wrapper, wrapper.read(Type.FLOAT), wrapper.read(Type.FLOAT), wrapper.read(Type.BOOLEAN));
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.TELEPORT_CONFIRM, null, wrapper -> {
            wrapper.cancel();
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            entityTracker.getClientPlayer().confirmTeleport(wrapper.read(Type.VAR_INT));
        });
    }

}
