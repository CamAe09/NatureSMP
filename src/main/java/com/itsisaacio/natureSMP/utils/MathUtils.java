package com.itsisaacio.natureSMP.utils;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class MathUtils {
    public static final Random random = new Random();

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static Vector getVelocityVector(Vector vector, Player player, float side, float forward) {
        vector.setX(0.0);
        vector.setZ(0.0);

        Vector mot = new Vector(forward * -1.0, 0, side);

        if (mot.length() > 0.0) {
            mot.rotateAroundY(Math.toRadians(player.getLocation().getYaw() * -1.0F + 90.0F));
            mot.normalize().multiply(0.25F);
        }

        return mot.add(vector);
    }
    public static boolean percent(int chance)
    {
        return random.nextInt(1, 101) <= Math.clamp(chance, 0, 100);
    }
}
