package com.shugabrush.raintegration.mixin.sophisticatedcore;

import net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeWrapper;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = XpPumpUpgradeWrapper.class, remap = false)
public class XpPumpUpgradeWrapperMixin
{

    // @ModifyArg(
    // method =
    // "tryFillTankWithPlayerExperience(Lnet/minecraft/world/entity/player/Player;Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageFluidHandler;IZ)V",
    // at = @At(
    // value = "INVOKE",
    // target =
    // "Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageFluidHandler;fill(Lnet/minecraft/tags/TagKey;ILnet/minecraft/world/level/material/Fluid;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;Z)I"),
    // index = 2)
    // private Fluid unifiedXp(Fluid originalFluid)
    // {
    // return MoreUnification.getReplacementForFluid(originalFluid);
    // }
}
