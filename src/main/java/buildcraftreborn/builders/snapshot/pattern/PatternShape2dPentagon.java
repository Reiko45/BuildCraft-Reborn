/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot.pattern;

import buildcraftreborn.api.core.render.ISprite;

import buildcraftreborn.builders.BCBuildersSprites;

public class PatternShape2dPentagon extends PatternShape2d {
    private static final double DIST_HORIZONTAL = StrictMath.sin(Math.toRadians(108 - 90));
    private static final double DIST_VERTICAL;

    static {
        double cos54 = StrictMath.cos(Math.toRadians(108 / 2));
        double cos18 = StrictMath.cos(Math.toRadians(108 - 90));
        DIST_VERTICAL = cos54 / cos18;
    }

    public PatternShape2dPentagon() {
        super("2d_pentagon");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_PENTAGON;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        int halfA = maxA / 2;
        int indentA = (int) Math.round(maxA * DIST_HORIZONTAL);
        int indentB = (int) Math.round(maxB * DIST_VERTICAL);
        list.moveTo(indentA, 0);
        list.lineTo(maxA - indentA, 0);
        list.lineFrom(maxA, indentB);
        list.lineTo(maxA - halfA, maxB);
        list.moveTo(halfA, maxB);
        list.lineFrom(0, indentB);
        list.lineTo(indentA, 0);
        list.setFillPoint(halfA, maxB / 2);
    }
}
