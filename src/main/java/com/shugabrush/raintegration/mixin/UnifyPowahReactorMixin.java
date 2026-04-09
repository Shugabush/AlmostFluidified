package com.shugabrush.raintegration.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import org.spongepowered.asm.mixin.Mixin;
import owmii.powah.block.reactor.ReactorTile;

@Mixin(value = ReactorTile.class, remap = false)
public class UnifyPowahReactorMixin {

}
