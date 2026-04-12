package com.shugabrush.raintegration.mixin.industrialforegoing;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.povstalec.sgjourney.common.compatibility.jei.CrystallizerRecipeCategory;
import net.povstalec.sgjourney.common.recipe.CrystallizerRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CrystallizerRecipeCategory.class, remap = false)
public class UnifySGJourneyCrystallizerNaquadah
{
    @ModifyVariable(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/povstalec/sgjourney/common/recipe/CrystallizerRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"), index = 2)
    private CrystallizerRecipe getUnifiedRecipe(CrystallizerRecipe recipe)
    {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        ingredients.forEach(ingredient ->
        {

        });
        return recipe;
    }
}
