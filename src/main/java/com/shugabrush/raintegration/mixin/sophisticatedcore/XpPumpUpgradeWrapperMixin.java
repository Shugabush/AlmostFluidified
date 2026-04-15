package com.shugabrush.raintegration.mixin.sophisticatedcore;

import net.minecraft.world.level.material.Fluid;
import net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeWrapper;

import com.shugabrush.raintegration.MoreUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = XpPumpUpgradeWrapper.class, remap = false)
<<<<<<<< HEAD:src/main/java/com/shugabrush/raintegration/mixin/sophisticatedcore/XpPumpUpgradeWrapperMixin.java
public class XpPumpUpgradeWrapperMixin
========
public class UnifySophisticatedCoreXPMixin
>>>>>>>> main:src/main/java/com/shugabrush/raintegration/mixin/sophisticatedcore/UnifySophisticatedCoreXPMixin.java
{

    @ModifyArg(
               method = "tryFillTankWithPlayerExperience(Lnet/minecraft/world/entity/player/Player;Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageFluidHandler;IZ)V",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageFluidHandler;fill(Lnet/minecraft/tags/TagKey;ILnet/minecraft/world/level/material/Fluid;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;Z)I"),
               index = 2)
    private Fluid unifiedXp(Fluid originalFluid)
    {
        return MoreUnification.getReplacementForFluid(originalFluid);
    }
}
