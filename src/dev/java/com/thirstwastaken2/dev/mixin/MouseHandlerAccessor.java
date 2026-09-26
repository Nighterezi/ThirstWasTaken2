package com.thirstwastaken2.dev.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Writes the position the game believes the mouse is at, for the agent's virtual pointer. Every
 * version reads a screen's {@code mouseX} and {@code mouseY} out of these two fields, some through
 * {@code xpos()} and some straight from the field, so the fields are the one place that reaches
 * both. They are in window pixels. See {@link com.thirstwastaken2.dev.agent.thirst.Pointer}.
 */
@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("xpos")
    void thirst$setXpos(double x);

    @Accessor("ypos")
    void thirst$setYpos(double y);
}
