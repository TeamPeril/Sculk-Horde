package com.github.sculkhorde.util;

public class ColorConverter {

    public static int hexToRGB(String hex) {
        // Remove the hash at the beginning if it's there
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Parse the hex string to an integer
        int color = Integer.parseInt(hex, 16);

        return color;
    }
}
