package com.thirstwastaken2.supplementaries;

/**
 * The mark {@code SoftFluidMixin} puts on the {@code moonlight:water} registry entry, and the only way
 * the integration tells water apart when it has an entry rather than a stack.
 *
 * <p>Water cannot be asked what it is at that point. A soft fluid's account of itself is the vanilla
 * fluids it stands for, and water's are the {@code c:water} tag, which is not bound yet while a world
 * is loading. So it is looked up once by registry key, where {@link SoftFluidQuality#adoptWater} says,
 * and marked.
 *
 * <p>The mark is on the entry rather than in a field of this mod's own because there is more than one
 * entry: a client and the server it is playing on each load the registry for themselves, and both need
 * to know which of their own two objects is water.
 */
public interface WaterSoftFluid {
    void thirst$markWater();

    boolean thirst$isWater();
}
