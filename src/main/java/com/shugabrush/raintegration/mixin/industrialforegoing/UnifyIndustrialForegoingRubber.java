package com.shugabrush.raintegration.mixin.industrialforegoing;

import net.minecraft.world.item.ItemStack;

import com.buuz135.industrial.block.core.tile.LatexProcessingUnitTile;
import com.hrznstudio.titanium.component.fluid.SidedFluidTankComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import com.shugabrush.raintegration.unification.ItemUnification;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = LatexProcessingUnitTile.class, remap = false)
public class UnifyIndustrialForegoingRubber
{

    @Shadow
    private static int AMOUNT_LATEX = 750;

    @Shadow
    private static int AMOUNT_WATER = 500;

    @Shadow
    private SidedFluidTankComponent< LatexProcessingUnitTile> latex;

    @Shadow
    private SidedFluidTankComponent< LatexProcessingUnitTile> water;

    @Shadow
    private SidedInventoryComponent< LatexProcessingUnitTile> output;

    @ModifyArg(
               method = "lambda$onFinish$2",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItem(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"),
               index = 1)
    private @NotNull ItemStack v(@NotNull ItemStack originalItem)
    {
        return new ItemStack(ItemUnification.getItem(originalItem));
    }
}
