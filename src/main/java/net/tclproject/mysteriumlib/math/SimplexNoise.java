package net.tclproject.mysteriumlib.math;

import java.util.Random;

public class SimplexNoise {

    private static final SimplexNoise.Grad[] grad3 = new SimplexNoise.Grad[] { new SimplexNoise.Grad(1.0D, 1.0D, 0.0D),
        new SimplexNoise.Grad(-1.0D, 1.0D, 0.0D), new SimplexNoise.Grad(1.0D, -1.0D, 0.0D),
        new SimplexNoise.Grad(-1.0D, -1.0D, 0.0D), new SimplexNoise.Grad(1.0D, 0.0D, 1.0D),
        new SimplexNoise.Grad(-1.0D, 0.0D, 1.0D), new SimplexNoise.Grad(1.0D, 0.0D, -1.0D),
        new SimplexNoise.Grad(-1.0D, 0.0D, -1.0D), new SimplexNoise.Grad(0.0D, 1.0D, 1.0D),
        new SimplexNoise.Grad(0.0D, -1.0D, 1.0D), new SimplexNoise.Grad(0.0D, 1.0D, -1.0D),
        new SimplexNoise.Grad(0.0D, -1.0D, -1.0D) };
    private static final SimplexNoise.Grad[] grad4 = new SimplexNoise.Grad[] {
        new SimplexNoise.Grad(0.0D, 1.0D, 1.0D, 1.0D), new SimplexNoise.Grad(0.0D, 1.0D, 1.0D, -1.0D),
        new SimplexNoise.Grad(0.0D, 1.0D, -1.0D, 1.0D), new SimplexNoise.Grad(0.0D, 1.0D, -1.0D, -1.0D),
        new SimplexNoise.Grad(0.0D, -1.0D, 1.0D, 1.0D), new SimplexNoise.Grad(0.0D, -1.0D, 1.0D, -1.0D),
        new SimplexNoise.Grad(0.0D, -1.0D, -1.0D, 1.0D), new SimplexNoise.Grad(0.0D, -1.0D, -1.0D, -1.0D),
        new SimplexNoise.Grad(1.0D, 0.0D, 1.0D, 1.0D), new SimplexNoise.Grad(1.0D, 0.0D, 1.0D, -1.0D),
        new SimplexNoise.Grad(1.0D, 0.0D, -1.0D, 1.0D), new SimplexNoise.Grad(1.0D, 0.0D, -1.0D, -1.0D),
        new SimplexNoise.Grad(-1.0D, 0.0D, 1.0D, 1.0D), new SimplexNoise.Grad(-1.0D, 0.0D, 1.0D, -1.0D),
        new SimplexNoise.Grad(-1.0D, 0.0D, -1.0D, 1.0D), new SimplexNoise.Grad(-1.0D, 0.0D, -1.0D, -1.0D),
        new SimplexNoise.Grad(1.0D, 1.0D, 0.0D, 1.0D), new SimplexNoise.Grad(1.0D, 1.0D, 0.0D, -1.0D),
        new SimplexNoise.Grad(1.0D, -1.0D, 0.0D, 1.0D), new SimplexNoise.Grad(1.0D, -1.0D, 0.0D, -1.0D),
        new SimplexNoise.Grad(-1.0D, 1.0D, 0.0D, 1.0D), new SimplexNoise.Grad(-1.0D, 1.0D, 0.0D, -1.0D),
        new SimplexNoise.Grad(-1.0D, -1.0D, 0.0D, 1.0D), new SimplexNoise.Grad(-1.0D, -1.0D, 0.0D, -1.0D),
        new SimplexNoise.Grad(1.0D, 1.0D, 1.0D, 0.0D), new SimplexNoise.Grad(1.0D, 1.0D, -1.0D, 0.0D),
        new SimplexNoise.Grad(1.0D, -1.0D, 1.0D, 0.0D), new SimplexNoise.Grad(1.0D, -1.0D, -1.0D, 0.0D),
        new SimplexNoise.Grad(-1.0D, 1.0D, 1.0D, 0.0D), new SimplexNoise.Grad(-1.0D, 1.0D, -1.0D, 0.0D),
        new SimplexNoise.Grad(-1.0D, -1.0D, 1.0D, 0.0D), new SimplexNoise.Grad(-1.0D, -1.0D, -1.0D, 0.0D) };
    private static final double F2 = 0.5D * (Math.sqrt(3.0D) - 1.0D);
    private static final double G2 = (3.0D - Math.sqrt(3.0D)) / 6.0D;
    private static final double F4 = (Math.sqrt(5.0D) - 1.0D) / 4.0D;
    private static final double G4 = (5.0D - Math.sqrt(5.0D)) / 20.0D;
    protected final short[] doubledPermutationTable;
    protected final short[] variatedPermutationTable;
    protected final Random random;

    private static int fastfloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double dot(SimplexNoise.Grad g, double x, double y) {
        return g.x * x + g.y * y;
    }

    private static double dot(SimplexNoise.Grad g, double x, double y, double z) {
        return g.x * x + g.y * y + g.z * z;
    }

    private static double dot(SimplexNoise.Grad g, double x, double y, double z, double w) {
        return g.x * x + g.y * y + g.z * z + g.w * w;
    }

    public SimplexNoise(Random random) {
        byte[] bytes = new byte[1024];
        this.random = random;
        random.nextBytes(bytes);
        this.doubledPermutationTable = new short[bytes.length * 2];
        this.variatedPermutationTable = new short[this.doubledPermutationTable.length];

        for (int i = 0; i < bytes.length; ++i) {
            short value = (short) (bytes[i] & 255);
            this.doubledPermutationTable[i] = value;
            this.variatedPermutationTable[i] = (short) (value % 12);
        }

        System.arraycopy(this.doubledPermutationTable, 0, this.doubledPermutationTable, bytes.length, bytes.length);
        System.arraycopy(this.variatedPermutationTable, 0, this.variatedPermutationTable, bytes.length, bytes.length);
    }

    public Random getRandom() {
        return this.random;
    }

    public NoiseStretch generateNoiseStretcher(double stretchX, double stretchZ, double offsetX, double offsetZ) {
        return new NoiseStretch(this, stretchX, stretchZ, offsetX, offsetZ);
    }

    public NoiseStretch generateNoiseStretcher(double stretchX, double stretchY, double stretchZ, double offsetX,
        double offsetY, double offsetZ) {
        return new NoiseStretch(this, stretchX, stretchY, stretchZ, offsetX, offsetY, offsetZ);
    }

    public double noise(double xin, double yin) {
        double s = (xin + yin) * F2;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        byte i1;
        byte j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0D + 2.0D * G2;
        double y2 = y0 - 1.0D + 2.0D * G2;
        int ii = i & 1023;
        int jj = j & 1023;
        int gi0 = this.variatedPermutationTable[ii + this.doubledPermutationTable[jj]];
        int gi1 = this.variatedPermutationTable[ii + i1 + this.doubledPermutationTable[jj + j1]];
        int gi2 = this.variatedPermutationTable[ii + 1 + this.doubledPermutationTable[jj + 1]];
        double t0 = 0.5D - x0 * x0 - y0 * y0;
        double n0;
        if (t0 < 0.0D) {
            n0 = 0.0D;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0);
        }

        double t1 = 0.5D - x1 * x1 - y1 * y1;
        double n1;
        if (t1 < 0.0D) {
            n1 = 0.0D;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
        }

        double t2 = 0.5D - x2 * x2 - y2 * y2;
        double n2;
        if (t2 < 0.0D) {
            n2 = 0.0D;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
        }

        return 70.0D * (n0 + n1 + n2);
    }

    public double noise(double xin, double yin, double zin) {
        double s = (xin + yin + zin) * 0.3333333333333333D;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        int k = fastfloor(zin + s);
        double t = (i + j + k) * 0.16666666666666666D;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        double z0 = zin - Z0;
        byte i1;
        byte j1;
        byte k1;
        byte i2;
        byte j2;
        byte k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else if (y0 < z0) {
            i1 = 0;
            j1 = 0;
            k1 = 1;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else if (x0 < z0) {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 1;
            j2 = 1;
            k2 = 0;
        }

        double x1 = x0 - i1 + 0.16666666666666666D;
        double y1 = y0 - j1 + 0.16666666666666666D;
        double z1 = z0 - k1 + 0.16666666666666666D;
        double x2 = x0 - i2 + 0.3333333333333333D;
        double y2 = y0 - j2 + 0.3333333333333333D;
        double z2 = z0 - k2 + 0.3333333333333333D;
        double x3 = x0 - 1.0D + 0.5D;
        double y3 = y0 - 1.0D + 0.5D;
        double z3 = z0 - 1.0D + 0.5D;
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = this.variatedPermutationTable[ii
            + this.doubledPermutationTable[jj + this.doubledPermutationTable[kk]]];
        int gi1 = this.variatedPermutationTable[ii + i1
            + this.doubledPermutationTable[jj + j1 + this.doubledPermutationTable[kk + k1]]];
        int gi2 = this.variatedPermutationTable[ii + i2
            + this.doubledPermutationTable[jj + j2 + this.doubledPermutationTable[kk + k2]]];
        int gi3 = this.variatedPermutationTable[ii + 1
            + this.doubledPermutationTable[jj + 1 + this.doubledPermutationTable[kk + 1]]];
        double t0 = 0.6D - x0 * x0 - y0 * y0 - z0 * z0;
        double n0;
        if (t0 < 0.0D) {
            n0 = 0.0D;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0);
        }

        double t1 = 0.6D - x1 * x1 - y1 * y1 - z1 * z1;
        double n1;
        if (t1 < 0.0D) {
            n1 = 0.0D;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1);
        }

        double t2 = 0.6D - x2 * x2 - y2 * y2 - z2 * z2;
        double n2;
        if (t2 < 0.0D) {
            n2 = 0.0D;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2);
        }

        double t3 = 0.6D - x3 * x3 - y3 * y3 - z3 * z3;
        double n3;
        if (t3 < 0.0D) {
            n3 = 0.0D;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3);
        }

        return 32.0D * (n0 + n1 + n2 + n3);
    }

    public double noise(double x, double y, double z, double w) {
        double s = (x + y + z + w) * F4;
        int i = fastfloor(x + s);
        int j = fastfloor(y + s);
        int k = fastfloor(z + s);
        int l = fastfloor(w + s);
        double t = (i + j + k + l) * G4;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;
        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        if (x0 > y0) {
            ++rankx;
        } else {
            ++ranky;
        }

        if (x0 > z0) {
            ++rankx;
        } else {
            ++rankz;
        }

        if (x0 > w0) {
            ++rankx;
        } else {
            ++rankw;
        }

        if (y0 > z0) {
            ++ranky;
        } else {
            ++rankz;
        }

        if (y0 > w0) {
            ++ranky;
        } else {
            ++rankw;
        }

        if (z0 > w0) {
            ++rankz;
        } else {
            ++rankw;
        }

        int i1 = rankx >= 3 ? 1 : 0;
        int j1 = ranky >= 3 ? 1 : 0;
        int k1 = rankz >= 3 ? 1 : 0;
        int l1 = rankw >= 3 ? 1 : 0;
        int i2 = rankx >= 2 ? 1 : 0;
        int j2 = ranky >= 2 ? 1 : 0;
        int k2 = rankz >= 2 ? 1 : 0;
        int l2 = rankw >= 2 ? 1 : 0;
        int i3 = rankx >= 1 ? 1 : 0;
        int j3 = ranky >= 1 ? 1 : 0;
        int k3 = rankz >= 1 ? 1 : 0;
        int l3 = rankw >= 1 ? 1 : 0;
        double x1 = x0 - i1 + G4;
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;
        double x2 = x0 - i2 + 2.0D * G4;
        double y2 = y0 - j2 + 2.0D * G4;
        double z2 = z0 - k2 + 2.0D * G4;
        double w2 = w0 - l2 + 2.0D * G4;
        double x3 = x0 - i3 + 3.0D * G4;
        double y3 = y0 - j3 + 3.0D * G4;
        double z3 = z0 - k3 + 3.0D * G4;
        double w3 = w0 - l3 + 3.0D * G4;
        double x4 = x0 - 1.0D + 4.0D * G4;
        double y4 = y0 - 1.0D + 4.0D * G4;
        double z4 = z0 - 1.0D + 4.0D * G4;
        double w4 = w0 - 1.0D + 4.0D * G4;
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int ll = l & 255;
        int gi0 = this.doubledPermutationTable[ii
            + this.doubledPermutationTable[jj + this.doubledPermutationTable[kk + this.doubledPermutationTable[ll]]]]
            % 32;
        int gi1 = this.doubledPermutationTable[ii + i1
            + this.doubledPermutationTable[jj + j1
                + this.doubledPermutationTable[kk + k1 + this.doubledPermutationTable[ll + l1]]]]
            % 32;
        int gi2 = this.doubledPermutationTable[ii + i2
            + this.doubledPermutationTable[jj + j2
                + this.doubledPermutationTable[kk + k2 + this.doubledPermutationTable[ll + l2]]]]
            % 32;
        int gi3 = this.doubledPermutationTable[ii + i3
            + this.doubledPermutationTable[jj + j3
                + this.doubledPermutationTable[kk + k3 + this.doubledPermutationTable[ll + l3]]]]
            % 32;
        int gi4 = this.doubledPermutationTable[ii + 1
            + this.doubledPermutationTable[jj + 1
                + this.doubledPermutationTable[kk + 1 + this.doubledPermutationTable[ll + 1]]]]
            % 32;
        double t0 = 0.6D - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        double n0;
        if (t0 < 0.0D) {
            n0 = 0.0D;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad4[gi0], x0, y0, z0, w0);
        }

        double t1 = 0.6D - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        double n1;
        if (t1 < 0.0D) {
            n1 = 0.0D;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad4[gi1], x1, y1, z1, w1);
        }

        double t2 = 0.6D - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        double n2;
        if (t2 < 0.0D) {
            n2 = 0.0D;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad4[gi2], x2, y2, z2, w2);
        }

        double t3 = 0.6D - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        double n3;
        if (t3 < 0.0D) {
            n3 = 0.0D;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad4[gi3], x3, y3, z3, w3);
        }

        double t4 = 0.6D - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        double n4;
        if (t4 < 0.0D) {
            n4 = 0.0D;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * dot(grad4[gi4], x4, y4, z4, w4);
        }

        return 27.0D * (n0 + n1 + n2 + n3 + n4);
    }

    private static class Grad {

        double x;
        double y;
        double z;
        double w;

        Grad(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Grad(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }
}
