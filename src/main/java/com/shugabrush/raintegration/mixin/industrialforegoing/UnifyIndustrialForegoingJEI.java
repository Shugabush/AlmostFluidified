package com.shugabrush.raintegration.mixin.industrialforegoing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import com.buuz135.industrial.plugin.jei.JEICustomPlugin;
import com.buuz135.industrial.plugin.jei.machineproduce.MachineProduceWrapper;
import com.shugabrush.raintegration.MoreUnification;
import com.shugabrush.raintegration.unification.ItemUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(value = JEICustomPlugin.class, remap = false)
public class UnifyIndustrialForegoingJEI
{

    @ModifyArg(
               method = "registerRecipes",
               at = @At(
                        value = "INVOKE",
                        target = "Lmezz/jei/api/registration/IRecipeRegistration;addRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/List;)V"),
               index = 1)
    // Make recipes display their unified output items
    <T> List< T> getRecipes(List< T> recipes)
    {
        for (int i = 0; i < recipes.size(); ++i)
        {
            T recipe = recipes.get(i);
            if (recipe instanceof MachineProduceWrapper)
            {
                MachineProduceWrapper wrapper = (MachineProduceWrapper) recipe;
                Ingredient outputItems = wrapper.getOutputItem();
                FluidStack outputFluid = wrapper.getOutputFluid();
                if (outputItems != null)
                {
                    ItemStack[] items = outputItems.getItems();
                    for (int j = 0; j < items.length; j++)
                    {
                        ItemStack unifiedItem = new ItemStack(ItemUnification.getItem(items[j]));
                        if (unifiedItem.getCount() > 0)
                        {
                            wrapper.getOutputItem().getItems()[j] = unifiedItem;
                        }
                    }
                }
                else if (outputFluid != null)
                {
                    wrapper = new MachineProduceWrapper(
                            wrapper.getBlock(),
                            new FluidStack(
                                    MoreUnification.getReplacementForFluid(outputFluid.getFluid()),
                                    outputFluid.getAmount()));
                    recipes.set(i, (T) wrapper);
                }
            }
        }
        return recipes;
    }
}
