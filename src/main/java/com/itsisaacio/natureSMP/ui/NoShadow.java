package com.itsisaacio.natureSMP.ui;

import org.jetbrains.annotations.Range;

public class NoShadow {
    public @Range(from = 0L, to = 255L) int red() {
        return 0;
    }

    public @Range(from = 0L, to = 255L) int green() {
        return 0;
    }

    public @Range(from = 0L, to = 255L) int blue() {
        return 0;
    }

    public @Range(from = 0L, to = 255L) int alpha() {
        return 0;
    }
}