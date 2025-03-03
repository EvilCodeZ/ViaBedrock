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

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.protocol.model.ResourcePack;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.UUID;

public class ResourcePackRewriter {

    public static ResourcePack.Content bedrockToJava(final ResourcePacksStorage bedrockPacks, final UUID[] resourcePackIds, final UUID[] behaviorPackIds) {
        final ResourcePack.Content javaContent = new ResourcePack.Content();

        for (UUID id : resourcePackIds) {
            if (bedrockPacks.hasPack(id)) {
                final ResourcePack pack = bedrockPacks.getPack(id);

                convertGlyphSheets(pack.content(), javaContent);
            }
        }

        javaContent.putJson("pack.mcmeta", createPackManifest());

        return javaContent;
    }

    private static JsonObject createPackManifest() {
        final JsonObject root = new JsonObject();
        final JsonObject pack = new JsonObject();
        root.add("pack", pack);
        pack.addProperty("pack_format", 13);
        pack.addProperty("description", "ViaBedrock Resource Pack");
        return root;
    }

    private static void convertGlyphSheets(final ResourcePack.Content bedrockContent, final ResourcePack.Content javaContent) {
        final int glyphsPerRow = 16;
        final int glyphsPerColumn = 16;

        final JsonArray providers = new JsonArray();

        for (int i = 0; i < 0xFF; i++) {
            final String pageName = "glyph_" + String.format("%1$02X", i) + ".png";
            final String bedrockPath = "font/" + pageName;
            if (!bedrockContent.containsKey(bedrockPath)) {
                continue;
            }

            final String javaPath = "assets/viabedrock/textures/font/" + pageName.toLowerCase(Locale.ROOT);
            final BufferedImage image = bedrockContent.getImage(bedrockPath);
            javaContent.putImage(javaPath, image);

            final int glyphHeight = image.getHeight() / glyphsPerColumn;

            final JsonObject glyphPage = new JsonObject();
            providers.add(glyphPage);
            glyphPage.addProperty("type", "bitmap");
            glyphPage.addProperty("file", "viabedrock:font/" + pageName.toLowerCase(Locale.ROOT));
            glyphPage.addProperty("ascent", glyphHeight / 2 + 5);
            glyphPage.addProperty("height", glyphHeight);
            final JsonArray chars = new JsonArray();
            glyphPage.add("chars", chars);
            for (int c = 0; c < glyphsPerColumn; c++) {
                final StringBuilder row = new StringBuilder();
                for (int r = 0; r < glyphsPerRow; r++) {
                    final int idx = c * glyphsPerColumn + r;
                    row.append((char) (i << 8 | idx));
                }
                chars.add(row.toString());
            }
        }
        if (providers.size() == 0) {
            return;
        }

        final JsonObject root = new JsonObject();
        root.add("providers", providers);
        javaContent.putJson("assets/minecraft/font/default.json", root);
    }

}
