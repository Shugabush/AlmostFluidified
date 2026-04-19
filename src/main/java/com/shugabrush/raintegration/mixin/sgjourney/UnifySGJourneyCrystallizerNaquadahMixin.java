package com.shugabrush.raintegration.mixin.sgjourney;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.povstalec.sgjourney.common.compatibility.jei.CrystallizerRecipeCategory;
import net.povstalec.sgjourney.common.recipe.CrystallizerRecipe;

import com.shugabrush.raintegration.unification.utils.ItemUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CrystallizerRecipeCategory.class, remap = false)
public class UnifySGJourneyCrystallizerNaquadahMixin
{

    @ModifyVariable(
                    method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/povstalec/sgjourney/common/recipe/CrystallizerRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
                    at = @At("HEAD"),
                    index = 2)
    private CrystallizerRecipe getUnifiedRecipe(CrystallizerRecipe recipe)
    {
        NonNullList< Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.stream().count(); i++)
        {
            ItemStack[] items = ingredients.get(i).getItems();
            for (int j = 0; j < items.length; j++)
            {
                ItemStack item = new ItemStack(ItemUnification.getItem(items[j]));
                recipe.getIngredients().get(i).getItems()[j] = item;
            }
        }
        return recipe;
    }

    // @ModifyArg(
    // method =
    // "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/povstalec/sgjourney/common/recipe/CrystallizerRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
    // at = @At(
    // value = "INVOKE",
    // target =
    // "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addFluidStack(Lnet/minecraft/world/level/material/Fluid;J)Lmezz/jei/api/gui/builder/IIngredientAcceptor;"),
    // index = 0)
    // private Fluid getUnifiedFluidStack(Fluid originalFluid)
    // {
    // return MoreUnification.getReplacementForFluid(originalFluid);
    // }
}
