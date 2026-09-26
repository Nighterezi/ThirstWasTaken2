package com.thirstwastaken2.culturaldelights;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies {@code thirstwastaken2.culturaldelights.mixins.json} only when Cultural Delights 0.18 is
 * installed, and each mixin only where its target still declares the methods it injects into, so a
 * method renamed upstream skips that mixin rather than failing it.
 */
public final class CulturalDelightsMixinPlugin implements IMixinConfigPlugin {
    /** The methods each mixin injects into, by simple name. */
    private static final Map<String, List<String>> NEEDS_METHODS = Map.of(
            "VatSeaWaterMixin", List.of("hasRecipe"));

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!CulturalDelightsPresence.isPresent()) return false;
        List<String> methods = NEEDS_METHODS.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        if (methods == null) return true;
        for (String method : methods) {
            if (!CulturalDelightsPresence.hasMethod(targetClassName, method)) return false;
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
