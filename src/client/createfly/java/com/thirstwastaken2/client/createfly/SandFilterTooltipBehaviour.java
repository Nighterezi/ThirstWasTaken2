package com.thirstwastaken2.client.createfly;

import com.thirstwastaken2.createfly.SandFilterBlockEntity;
import com.thirstwastaken2.createfly.WaterFluids;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * What the Sand Filter holds, seen through Engineer's Goggles: each tank with its grade, the way the
 * original mod showed it.
 */
final class SandFilterTooltipBehaviour extends TooltipBehaviour<SandFilterBlockEntity> implements IHaveGoggleInformation {
    /** Create Fly counts fluid in droplets. */
    private static final double DROPLETS_PER_MB = 81;

    SandFilterTooltipBehaviour(SandFilterBlockEntity filter) {
        super(filter);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);

        boolean unfiltered = tank(tooltip, mb, "unfiltered", blockEntity.input());
        boolean filtered = tank(tooltip, mb, "filtered", blockEntity.output());
        if (!unfiltered && !filtered) {
            CreateLang.translate("gui.goggles.fluid_container.capacity")
                    .add(CreateLang.number(SandFilterBlockEntity.CAPACITY / DROPLETS_PER_MB).add(mb).style(ChatFormatting.GOLD))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }
        return true;
    }

    private static boolean tank(List<Component> tooltip, LangBuilder mb, String label, SmartFluidTankBehaviour tank) {
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        if (fluid.isEmpty()) return false;

        CreateLang.builder()
                .add(Component.translatable("gui.thirstwastaken2.sand_filter." + label))
                .text(": ")
                .add(CreateLang.fluidName(fluid))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        if (WaterFluids.isWater(fluid)) {
            WaterQuality quality = WaterFluids.quality(fluid);
            Component line = quality instanceof WaterQuality.Fresh fresh
                    ? WaterPurity.tooltip(fresh.purity()) : WaterPurity.saltTooltip();
            CreateLang.builder().add(line).forGoggles(tooltip, 1);
        }
        CreateLang.builder()
                .add(CreateLang.number(fluid.getAmount() / DROPLETS_PER_MB).add(mb).style(ChatFormatting.GOLD))
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number(tank.getPrimaryHandler().getMaxAmountPerStack() / DROPLETS_PER_MB).add(mb)
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }
}
