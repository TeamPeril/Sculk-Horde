package com.github.sculkhorde.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class ColorUtil {

    public static String sculkBaseColor1 = "034150";
    public static String sculkBaseColor2 = "062E37";
    public static String sculkBaseColor3 = "04252D";
    public static String sculkBaseColor4 = "002A2A";
    public static String sculkBaseColor5 = "122225";
    public static String sculkBaseColor6 = "122225";
    public static String sculkLightColor1 = "29DFEB";
    public static String sculkLightColor2 = "0BB4AA";
    public static String sculkLightColor3 = "009295";
    public static String sculkLightColor4 = "1B6864";
    public static String sculkLightColor5 = "037286";
    public static String sculkLightColor6 = "0A5C70";
    public static String sculkBoneColor1 = "D1D6B6";
    public static String sculkBoneColor2 = "BBC39B";
    public static String sculkBoneColor3 = "A2AF86";
    public static String sculkBoneColor4 = "819988";
    public static String sculkBoneColor5 = "6E757B";
    public static String sculkBoneColor6 = "40576C";
    public static String sculkAcidColor1 = "84FF35";
    public static String sculkAcidColor2 = "5FF21C";
    public static String sculkAcidColor3 = "10D010";
    public static String sculkAcidColor4 = "0EAD20";
    public static String sculkAcidColor5 = "0A8C2E";
    public static String sculkAcidColor6 = "055430";


    public static String getRandomHexAcidColor(RandomSource rng)
    {
        int index = rng.nextInt(7);
        switch (index)
        {
            case 0:
                return sculkAcidColor1;
            case 1:
                return sculkAcidColor2;
            case 2:
                return sculkAcidColor3;
            case 3:
                return sculkAcidColor4;
            case 4:
                return sculkAcidColor5;
            case 5:
                return sculkAcidColor6;
            default:
                return sculkAcidColor1;
        }
    }

    public static int hexToRGB(String hex) {
        // Remove the hash at the beginning if it's there
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Parse the hex string to an integer
        int color = Integer.parseInt(hex, 16);

        return color;
    }
  // all vec3 must die, death to vec3
    // public static Vector3f hexToVector3F(String hex)
   // {
  //      return Vec3.fromRGB24(hexToRGB(hex)).toVector3f();
   // }
}
