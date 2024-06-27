package net.tclproject.mysteriumlib.math;

import java.util.Iterator;

import net.minecraft.world.ChunkCoordIntPair;

import joptsimple.internal.Objects;

public class SpiralPatternGenerator implements Iterable {

    protected final ChunkCoordIntPair center;
    protected final int rangeMax;

    public SpiralPatternGenerator(ChunkCoordIntPair center) {
        this(center, Integer.MAX_VALUE);
    }

    public SpiralPatternGenerator(ChunkCoordIntPair center, int rangeMax) {
        Objects.ensureNotNull(center);
        if (rangeMax < 0) {
            throw new IllegalArgumentException("The maximum range must be more than or equal to 0.");
        } else {
            this.center = center;
            this.rangeMax = rangeMax;
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {

            private int currentX;
            private int currentZ;
            protected SpiralPatternGenerator.SpiralDirection currentDirection;
            private int currentRange = 0;

            @Override
            public boolean hasNext() {
                return this.currentRange <= SpiralPatternGenerator.this.rangeMax;
            }

            @Override
            public ChunkCoordIntPair next() {
                if (this.currentRange == 0) {
                    ++this.currentRange;
                    this.currentX = SpiralPatternGenerator.this.center.chunkXPos - this.currentRange;
                    this.currentZ = SpiralPatternGenerator.this.center.chunkZPos - this.currentRange;
                    this.currentDirection = SpiralPatternGenerator.SpiralDirection.right;
                    return SpiralPatternGenerator.this.center;
                } else {
                    ChunkCoordIntPair result = new ChunkCoordIntPair(this.currentX, this.currentZ);
                    this.step();
                    return result;
                }
            }

            protected void step() {
                switch (this.currentDirection) {
                    case right:
                        if (this.currentX < SpiralPatternGenerator.this.center.chunkXPos + this.currentRange) {
                            ++this.currentX;
                        } else {
                            this.currentDirection = SpiralPatternGenerator.SpiralDirection.down;
                            this.step();
                        }
                    case down:
                        if (this.currentZ < SpiralPatternGenerator.this.center.chunkZPos + this.currentRange) {
                            ++this.currentZ;
                        } else {
                            this.currentDirection = SpiralPatternGenerator.SpiralDirection.left;
                            this.step();
                        }
                    case left:
                        if (this.currentX > SpiralPatternGenerator.this.center.chunkXPos - this.currentRange) {
                            --this.currentX;
                        } else {
                            this.currentDirection = SpiralPatternGenerator.SpiralDirection.up;
                            this.step();
                        }
                    case up:
                        if (this.currentZ - 1 > SpiralPatternGenerator.this.center.chunkZPos - this.currentRange) {
                            --this.currentZ;
                        } else {
                            this.currentDirection = SpiralPatternGenerator.SpiralDirection.right;
                            ++this.currentRange;
                            this.currentX = SpiralPatternGenerator.this.center.chunkXPos - this.currentRange;
                            this.currentZ = SpiralPatternGenerator.this.center.chunkZPos - this.currentRange;
                        }
                    default:
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported at this moment in time.");
            }
        };
    }

    public ChunkCoordIntPair getCenter() {
        return this.center;
    }

    public int getRangeMax() {
        return this.rangeMax;
    }

    private static enum SpiralDirection {
        right,
        down,
        left,
        up;
    }
}
