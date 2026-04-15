package com.shugabrush.raintegration.mixin.sgjourney;

import com.shugabrush.raintegration.ConfigHolder;
import com.shugabrush.raintegration.unification.ItemUnification;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.ModList;
import net.povstalec.sgjourney.common.compatibility.jei.CrystallizerRecipeCategory;
import net.povstalec.sgjourney.common.recipe.CrystallizerRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CrystallizerRecipeCategory.class, remap = false)
public class CrystallizerRecipeCategoryMixin
{
    // Almost Unified doesn't unify crystallizer recipes,
    // So I'm doing it here
    @ModifyVariable(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/povstalec/sgjourney/common/recipe/CrystallizerRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"), index = 2)
    private CrystallizerRecipe getUnifiedRecipe(CrystallizerRecipe recipe)
    {
        if (ModList.get().isLoaded("almostunified"))
        {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int i = 0; i < ingredients.stream().count(); i++) {
                ItemStack[] items = ingredients.get(i).getItems();
                for (int j = 0; j < items.length; j++)
                {
                    ItemStack item = new ItemStack(ItemUnification.getItem(items[j]));
                    recipe.getIngredients().get(i).getItems()[j] = item;
                }
            }
        }
        return recipe;
    }

    // Get the crystallizer fluid that will be accepted
    @ModifyArg(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/povstalec/sgjourney/common/recipe/CrystallizerRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addFluidStack(Lnet/minecraft/world/level/material/Fluid;J)Lmezz/jei/api/gui/builder/IIngredientAcceptor;"), index = 0)
    private Fluid getUnifiedFluidStack(Fluid originalFluid)
    {
        return ConfigHolder.instance.fluidConfigs.getCrystallizerFluid();
    }
}
