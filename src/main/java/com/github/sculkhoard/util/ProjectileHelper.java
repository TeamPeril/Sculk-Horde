package com.github.sculkhoard.util;

public class ProjectileHelper {

    public static double computeY(double xyDist, double dy, double vel, double y0, double ybase, double height)
    {
        final double gravity = 0.05;
        final double drag = 0.99;
        double d0, d1;
        double v0, v1;
        double vlen;
        double p0, p1;
        double plast = -Double.MAX_VALUE;
        double yp = 0.2;
        double ys = 0.1;
        int flag = 0;

        d0 = xyDist;
        d1 = dy;

        for (int i = 0; i < 10; ++i)
        {
            v0 = d0;
            v1 = d1 + d0 * yp;
            vlen = Math.sqrt(v0 * v0 + v1 * v1);
            v0 = v0 / vlen * vel;
            v1 = v1 / vlen * vel;

            p0 = 0.0;
            p1 = y0;
            while (p0 < xyDist)
            {
                p0 += v0;
                p1 += v1;
                if (v1 < 0.0 && p1 < ybase)
                    break;
                v0 *= drag;
                v1 = v1 * drag - gravity;
            }

            p1 -= ybase;
            if (p1 < 0.0) // arrow fell short
            {
                flag |= 1;
                if (flag == 3)
                    ys *= 0.5;
                else if (p0 < plast) // getting worse
                    break;
                yp += ys;
            }
            else if (p1 > height) // arrow passed over head
            {
                flag |= 2;
                if (flag == 3)
                    ys *= 0.5;
                yp -= ys;
            }
            else
                break;
            plast = p0;
        }

        return dy + xyDist * yp;
    }
}
