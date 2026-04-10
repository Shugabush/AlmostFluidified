package com.shugabrush.raintegration.mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Material.class, remap = false)
public class UnifyGregTechMaterialsMixin {
//    /**
//     * @author Shugabrush
//     * @reason Change GregTech Fluid access to Mekanism's fluids
//     */
//    @Overwrite()
//    public Fluid getFluid(@NotNull FluidStorageKey key) {
//
//    }
}
