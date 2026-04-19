package com.shugabrush.raintegration.unification.recipe.unifier;

import net.minecraft.resources.ResourceLocation;

import com.shugabrush.raintegration.api.FluidRecipeUnifier;
import com.shugabrush.raintegration.api.FluidRecipeUnifierBuilder;
import com.shugabrush.raintegration.unification.FluidRecipeContext;

import java.util.HashMap;
import java.util.Map;

public class FluidRecipeHandlerFactory
{

    private Map< ResourceLocation, FluidRecipeUnifier> transformersByType = new HashMap<>();
    private Map< String, FluidRecipeUnifier> transformersByModId = new HashMap<>();

    public void fillUnifier(FluidRecipeUnifierBuilder builder, FluidRecipeContext context)
    {
        GenericFluidRecipeUnifier.INSTANCE.collectUnifier(builder);

        ResourceLocation type = context.getType();
        FluidRecipeUnifier byMod = transformersByModId.get(type.getNamespace());
        if (byMod != null)
        {
            byMod.collectUnifier(builder);
        }

        FluidRecipeUnifier byType = transformersByType.get(type);
        if (byType != null)
        {
            byType.collectUnifier(builder);
        }
    }

    public void registerForType(ResourceLocation type, FluidRecipeUnifier transformer)
    {
        transformersByType.put(type, transformer);
    }

    public void registerForMod(String mod, FluidRecipeUnifier transformer)
    {
        transformersByModId.put(mod, transformer);
    }
}
