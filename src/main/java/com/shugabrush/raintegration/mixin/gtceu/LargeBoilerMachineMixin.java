package com.shugabrush.raintegration.mixin.gtceu;

import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;

import net.minecraftforge.fluids.FluidStack;

import com.shugabrush.raintegration.MoreUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = LargeBoilerMachine.class, remap = false)
public class LargeBoilerMachineMixin
{

    // Make multiblock steam boilers produce Configured Liquid Steam
    @ModifyArg(
               method = "updateCurrentTemperature",
               at = @At(
                        value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;of(Lnet/minecraftforge/fluids/FluidStack;)Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;"),
               index = 0)
    private FluidStack unifySteam(FluidStack stack)
    {
        return new FluidStack(MoreUnification.getReplacementForFluid(stack.getFluid()), stack.getAmount());
    }
}
