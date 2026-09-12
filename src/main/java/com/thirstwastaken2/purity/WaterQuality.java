package com.thirstwastaken2.purity;

/**
 * What a container holds.
 *
 * <p>Sealed on purpose. Salt water is a different kind of water, not a low purity tier: cooking
 * cannot improve it, one salty serving spoils a whole batch, and it never hydrates. Making it its
 * own case means every consumer - tooltips, sickness, mixing, sprites - has to say what it does
 * with salt water instead of quietly treating it as a grade.
 */
public sealed interface WaterQuality {
    /** Sea water is stateless, so one instance serves every caller. */
    WaterQuality SALT = new Salt();

    static WaterQuality fresh(int purity) {
        return new Fresh(purity);
    }

    default boolean salty() {
        return this instanceof Salt;
    }

    /** Drinkable water, graded {@code 0..3}: dirty, murky, clean, pure. */
    record Fresh(int purity) implements WaterQuality {
        public Fresh {
            purity = Math.clamp(purity, WaterPurity.MIN, WaterPurity.MAX);
        }
    }

    /** Sea water. It carries no grade, because no grade of it can be drunk. */
    record Salt() implements WaterQuality { }
}
