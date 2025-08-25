package com.itsisaacio.natureSMP.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class Particles {
    final static double pi = Math.PI;

    public static void circle(Location location, double index, float width, float height, int iterator, double spin, Predicate<Integer> alternate)
    {
        AtomicInteger everyOther = new AtomicInteger(0);
        double rad = (height <= 0) ? width : Math.sin(index) * width;
        double y = Math.cos(index) * Math.abs(height);

        for (double a = spin; a < (pi * 2) + spin; a += (pi / (iterator / 2d)) + spin) {
            double x = Math.cos(a) * rad;
            double z = Math.sin(a) * rad;

            location.add(x, y, z);
            alternate.test(everyOther.get());
            everyOther.getAndIncrement();
            location.subtract(x, y, z);
        }
    }

    public static void height(Location location, float width, float height, int iterator, float cutoff, double spin, Predicate<Integer> alternate)
    {
        for (double index = 0; index < pi; index += pi / iterator) {
            if (cutoff > 0 && (index >= pi - cutoff || index <= cutoff))
                continue;

            circle(location, index, width, height, iterator, spin, alternate);
        }
    }

    public static void sphere(Location location, float width, float height, int iterator, float cutoff, double spin, Predicate<Integer> alternate)
    {
        if (height <= 0) circle(location, 0, width, height, iterator, spin, alternate);
        else height(location, width, height, iterator, cutoff, spin, alternate);
    }

    public static void line(Location start, Location end, int pointsPerLine, int particleCount,
                                      double offsetX, double offsetY, double offsetZ, Particle particle, float data,
                                      @Nullable Predicate<Location> operationPerPoint, Object extraData) {
        double d = start.distance(end) / pointsPerLine;

        for (int i = 0; i < pointsPerLine; i++) {
            Location l = start.clone();
            Vector direction = end.toVector().subtract(start.toVector()).normalize();
            Vector v = direction.multiply(i * d);
            l.add(v.getX(), v.getY(), v.getZ());

            if (operationPerPoint == null) {
                start.getWorld().spawnParticle(particle, l, particleCount, offsetX, offsetY, offsetZ, data, extraData);
                continue;
            }

            if (operationPerPoint.test(l)) {
                start.getWorld().spawnParticle(particle, l, particleCount, offsetX, offsetY, offsetZ, data, extraData);
            }
        }
    }
}
