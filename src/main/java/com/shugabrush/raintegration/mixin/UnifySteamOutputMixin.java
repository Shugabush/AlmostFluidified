package com.shugabrush.raintegration.mixin;

import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;

import net.minecraftforge.fluids.FluidStack;

import mekanism.common.registries.MekanismFluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = SteamBoilerMachine.class, remap = false)
public abstract class UnifySteamOutputMixin {

    /**
     * @return
     * @author Shugabrush
     * @reason Change some of GregTech's Fluid access to Mekanism's fluids
     */
    @ModifyArg(method = "updateCurrentTemperature",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;fillInternal(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)I"),
               index = 0)
    private FluidStack unifySteam(FluidStack stack) {
        return new FluidStack(MekanismFluids.STEAM.getFluid(), stack.getAmount());
    }
}
