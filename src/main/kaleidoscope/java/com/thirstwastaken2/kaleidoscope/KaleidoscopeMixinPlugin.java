package com.thirstwastaken2.kaleidoscope;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies {@code thirstwastaken2.kaleidoscope.mixins.json} only where Kaleidoscope Cookery is a build
 * the integration supports, and each mixin only where its own target is still there, so a renamed teapot
 * does not take the stockpot down with it.
 */
public final class KaleidoscopeMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        // Asked here as well as per mixin, so the warning about an unsupported build is logged at startup
        // whatever the config lists.
        KaleidoscopePresence.isSupported();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return KaleidoscopePresence.hasTarget(targetClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
