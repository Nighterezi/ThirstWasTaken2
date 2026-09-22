package com.thirstwastaken2.config;

/** How bad water makes a player ill. See docs/dev/mechanics/WATER-SICKNESS.md. */
public enum SicknessPreset {
    /** One roll per drink from the difficulty's {@link SicknessTable}: Upset Stomach or Poisoning. */
    REALISTIC,
    /** The roll before the sickness rework: Nausea and Poison by grade alone, the same on every difficulty. */
    CLASSIC
}
