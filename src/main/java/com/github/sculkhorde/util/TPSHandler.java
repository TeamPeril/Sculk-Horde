package com.github.sculkhorde.util;

public class TPSHandler {

    private static long lastTime = System.currentTimeMillis();
    private static int tickCount = 0;
    private static int tps = 0;

    public static double getTps() {
        return tps;
    }

    public static void onServerTick()
    {
        tickCount++;
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTime;

        if (timeDiff >= 1000) {
            tps = (int) (tickCount / (timeDiff / 1000.0));
            tickCount = 0;
            lastTime = currentTime;
        }
    }

    public static boolean isTPSBelowPerformanceThreshold()
    {
        return getTps() < 15;
    }
}
