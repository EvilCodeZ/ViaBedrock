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
package net.raphimc.viabedrock.protocol.task;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

public class KeepAliveTask implements Runnable {

    public static final long INTERNAL_ID = 999; // ID which the server can't possibly send

    @Override
    public void run() {
        for (UserConnection info : Via.getManager().getConnectionManager().getConnections()) {
            if (info.getProtocolInfo().getState().equals(State.PLAY) && info.getProtocolInfo().getPipeline().contains(BedrockProtocol.class)) {
                try {
                    final PacketWrapper keepAlive = PacketWrapper.create(ClientboundPackets1_19_4.KEEP_ALIVE, info);
                    keepAlive.write(Type.LONG, INTERNAL_ID); // id
                    keepAlive.send(BedrockProtocol.class);
                } catch (Throwable e) {
                    BedrockProtocol.kickForIllegalState(info, "Error sending keep alive packet. See console for details.", e);
                }
            }
        }
    }

}
