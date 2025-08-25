package com.itsisaacio.natureSMP.ui;

import net.kyori.adventure.util.ARGBLike;
import org.jetbrains.annotations.Range;

public class NoShadow implements ARGBLike {
    @Override
    public @Range(from = 0L, to = 255L) int red() {
        return 0;
    }

    @Override
    public @Range(from = 0L, to = 255L) int green() {
        return 0;
    }

    @Override
    public @Range(from = 0L, to = 255L) int blue() {
        return 0;
    }

    @Override
    public @Range(from = 0L, to = 255L) int alpha() {
        return 0;
    }
}
