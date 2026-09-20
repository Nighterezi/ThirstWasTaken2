package com.thirstwastaken2.supplementaries;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies {@code thirstwastaken2.supplementaries.mixins.json} only where its target is installed:
 * Moonlight Lib for the soft fluid mixins, Supplementaries itself for the cauldron one.
 */
public final class SupplementariesMixinPlugin implements IMixinConfigPlugin {
    /** The mixins whose target is Supplementaries' own; the rest are Moonlight's. */
    private static final Set<String> SUPPLEMENTARIES_MIXINS = Set.of(
            "FaucetBehaviorsManagerMixin",
            "LiquidBlockInteractionMixin",
            "SoftFluidTankViewMixin",
            "WaterCauldronInteractionMixin");

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String name = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        return SUPPLEMENTARIES_MIXINS.contains(name)
                ? SupplementariesPresence.hasSupplementaries()
                : SupplementariesPresence.hasMoonlight();
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
