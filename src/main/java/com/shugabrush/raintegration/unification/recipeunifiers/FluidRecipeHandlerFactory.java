package com.shugabrush.raintegration.unification.recipeunifiers;

import com.shugabrush.raintegration.unification.FluidRecipeContext;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FluidRecipeHandlerFactory
{
    private Map<ResourceLocation, FluidRecipeUnifier> transformersByType = new HashMap<>();
    private Map<String, FluidRecipeUnifier> transformersByModId = new HashMap<>();

    public void fillUnifier(FluidRecipeUnifierBuilder builder, FluidRecipeContext context)
    {
        
    }
}
