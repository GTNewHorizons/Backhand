package net.tclproject.mysteriumlib.math;

public class LocalityHelper {

    public static int locality(int x, int y, int seed, int width) {
        return localitySingle(x, y, seed, width) * 7 + localitySingle(x, y, seed, width / 2) * 4
            + localitySingle(x, y, seed, width / 4);
    }

    public static int localitySingle(int x, int y, int seed, int width) {
        int qa = localityAxis(x, seed, width);
        int qb = localityAxis(y, seed, width);
        return Math.abs(qa - 6) > Math.abs(qb - 6) ? qa : qb;
    }

    public static int localityAxis(int coordinate, int seed, int width) {
        int q = Math.abs(coordinate) + Math.abs(seed);
        int q1 = q / width % width;
        q %= width;
        int q2 = (q1 + 1) * 21 % 13;
        q1 = q1 * 21 % 13;
        return (q2 - q1) * q / width + q1;
    }
}
