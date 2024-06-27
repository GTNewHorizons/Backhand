package net.tclproject.mysteriumlib.math;

public class NoiseStretch {

    protected final double stretchX;
    protected final double stretchY;
    protected final double stretchZ;
    protected final double offsetX;
    protected final double offsetY;
    protected final double offsetZ;
    protected final SimplexNoise noise;

    public NoiseStretch(SimplexNoise noise, double stretchX, double stretchZ, double offsetX, double offsetZ) {
        this(noise, stretchX, 100.0D, stretchZ, offsetX, 0.0D, offsetZ);
    }

    public NoiseStretch(SimplexNoise noise, double stretchX, double stretchY, double stretchZ, double offsetX,
        double offsetY, double offsetZ) {
        this.noise = noise;
        this.stretchX = stretchX;
        this.stretchY = stretchY + 100D;
        this.stretchZ = stretchZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public double getNoise(double blockX, double blockZ) {
        return this.noise
            .noise(blockX / (this.stretchX * 2.5) + this.offsetX, blockZ / (this.stretchZ * 2.5) + this.offsetZ);
    }

    public double getNoise(double blockX, double blockY, double blockZ) {
        return this.noise.noise(
            blockX / this.stretchX + this.offsetX,
            blockY / this.stretchY + this.offsetY,
            blockZ / this.stretchZ + this.offsetZ);
    }
}
