package com.shugabrush.raintegration.unification;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class FluidRecipeContext
{
    private final FluidReplacementMap replacementMap;
    private final JsonObject originalRecipe;

    public FluidRecipeContext(JsonObject json, FluidReplacementMap replacementMap)
    {
        this.originalRecipe = json;
        this.replacementMap = replacementMap;
    }

    public ResourceLocation getReplacementForFluid(ResourceLocation fluid)
    {
        if (fluid == null) return null;

        return replacementMap.getReplacementForFluid(fluid);
    }

    public ResourceLocation getType()
    {
        String type = originalRecipe.get("type").getAsString();
        return new ResourceLocation(type);
    }

    public boolean hasProperty(String property)
    {
        return originalRecipe.has(property);
    }
}
