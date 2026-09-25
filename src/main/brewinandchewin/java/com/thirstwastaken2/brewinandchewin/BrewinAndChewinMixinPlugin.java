package com.thirstwastaken2.brewinandchewin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies {@code thirstwastaken2.brewinandchewin.mixins.json} only where Brewin' and Chewin' is
 * installed, and each mixin only where its own target, and the method it needs, is still there.
 */
public final class BrewinAndChewinMixinPlugin implements IMixinConfigPlugin {
    /** Mixins on one method of their target, by simple name, with that method. */
    private static final Map<String, String> NEEDS_METHOD = Map.of(
            "KegBlockEntityMixin", "fluidExtract",
            "KegPouringRecipeMixin", "getFluid");

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String method = NEEDS_METHOD.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        return method == null
                ? BrewinAndChewinPresence.hasTarget(targetClassName)
                : BrewinAndChewinPresence.hasMethod(targetClassName, method);
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
