package com.thirstwastaken2.effect;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ThirstEffects {
    /**
     * Thirst's counterpart to vanilla's Hunger effect: a dry mouth that drains the thirst bar faster,
     * from bad water and sea water. The drain itself is charged by {@code ThirstManager.tickPlayer}.
     * Its particles are the tan of dry sand: the red of the tongue in its icon read as healing.
     */
    public static final Holder<MobEffect> PARCHED = register("parched", 0xD4B483);

    private ThirstEffects() { }

    /**
     * Builds and registers the effects, in this class's static initializer, the same way
     * {@code ThirstItems.register} does. Nothing may read a field here before it runs.
     */
    public static void register() { }

    private static Holder<MobEffect> register(String name, int color) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, ThirstWasTaken2.id(name),
                new MarkerEffect(MobEffectCategory.HARMFUL, color));
    }

    /**
     * An effect that does nothing on its own tick. What it does is read by whoever it concerns, because
     * {@code applyEffectTick} changed its signature after 1.21.1 and overriding it would fork every node.
     * The constructor is protected in vanilla, hence the subclass.
     */
    private static final class MarkerEffect extends MobEffect {
        MarkerEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
