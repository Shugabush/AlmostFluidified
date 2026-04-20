package com.shugabrush.raintegration;

import com.shugabrush.raintegration.compat.ThermalFluidRecipeUnifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.ModConstants;
import com.shugabrush.raintegration.compat.GregTechFluidRecipeUnifier;
import com.shugabrush.raintegration.compat.IndustrialForegoingFluidRecipeUnifier;
import com.shugabrush.raintegration.compat.MekanismFluidRecipeUnifier;
import com.shugabrush.raintegration.unification.recipe.unifier.FluidRecipeHandlerFactory;

public class PlatformForge implements Platform
{

    public static final PlatformForge INSTANCE = new PlatformForge();

    @Override
    public AlmostUnifiedPlatform.Platform getPlatform()
    {
        return AlmostUnifiedPlatform.Platform.FORGE;
    }

    @Override
    public boolean isModLoaded(String modId)
    {
        if (ModList.get() == null)
        {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient()
    {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public void bindRecipeHandlers(FluidRecipeHandlerFactory factory)
    {
        factory.registerForMod(ModConstants.GREGTECH_MODERN, new GregTechFluidRecipeUnifier());
        factory.registerForMod(ModConstants.MEKANISM, new MekanismFluidRecipeUnifier());
        factory.registerForMod("industrialforegoing", new IndustrialForegoingFluidRecipeUnifier());
        factory.registerForMod("thermal", new ThermalFluidRecipeUnifier());
    }
}
