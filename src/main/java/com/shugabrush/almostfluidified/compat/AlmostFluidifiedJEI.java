package com.shugabrush.almostfluidified.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.utils.Utils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.Collection;

@JeiPlugin
public class AlmostFluidifiedJEI implements IModPlugin
{

    @Override
    public ResourceLocation getPluginUid()
    {
        return Utils.getRL(ModConstants.JEI);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei)
    {
        Collection< FluidStack> fluids = FluidHideHelper.getStacksToHide();
        Collection< ItemStack> buckets = FluidHideHelper.getBucketStacksToHide();
        if (!fluids.isEmpty())
        {
            jei.getIngredientManager().removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK, fluids);
        }
        if (!buckets.isEmpty())
        {
            jei.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, buckets);
        }
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration)
    {}
}
