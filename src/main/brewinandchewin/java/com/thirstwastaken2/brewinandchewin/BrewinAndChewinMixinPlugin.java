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
    /**
     * The methods each mixin injects into, by simple name. A mixin is applied only where its target
     * declares all of them, so a method renamed upstream skips that mixin rather than failing it.
     * {@code KegBottleMixin} names a lambda by its synthetic name, the likeliest of all to move.
     */
    private static final Map<String, List<String>> NEEDS_METHODS = Map.of(
            "KegBlockEntityMixin", List.of("fluidExtract"),
            "KegBottleMixin", List.of("fluidExtract", "lambda$getPouringRecipe$4"),
            "KegFermentingMixin", List.of("canFerment"),
            "KegPouringRecipeMixin", List.of("getFluid"));

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> methods = NEEDS_METHODS.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        if (methods == null) return BrewinAndChewinPresence.hasTarget(targetClassName);
        for (String method : methods) {
            if (!BrewinAndChewinPresence.hasMethod(targetClassName, method)) return false;
        }
        return true;
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
