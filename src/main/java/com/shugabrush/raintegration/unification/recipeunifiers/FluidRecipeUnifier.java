package com.shugabrush.raintegration.unification.recipeunifiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.RAIntegration;

import java.util.Set;

public class FluidRecipeUnifier
{
    protected Set<String> lookupKeys;
    protected Set<String> primitiveLookupKeys;

    public FluidRecipeUnifier()
    {
        lookupKeys = Set.of("inputs", "outputs", "fluidInputs", "fluidOutputs");
        primitiveLookupKeys = Set.of("tag", "fluid", "fluidInput", "fluidOutput");
    }

    public void unifyFluidRecipe(JsonElement element)
    {
        if (element == null)
            return;

        if (element instanceof JsonArray array)
        {
            for (JsonElement arrayElement : array)
            {
                unifyFluidRecipe(arrayElement);
            }
        }
        else if (element instanceof JsonObject object)
        {
            for (String key : lookupKeys)
            {
                JsonElement keyElement = object.get(key);
                if (keyElement != null)
                {
                    unifyFluidRecipe(keyElement);
                }
            }
            for (String key : primitiveLookupKeys)
            {
                if (object.get(key) instanceof JsonPrimitive primitive)
                {
                    JsonPrimitive unifiedPrimitive = unifyJsonPrimitive(primitive);
                    RAIntegration.LOGGER.info(unifiedPrimitive.toString());
                }
            }
        }
    }

    protected JsonPrimitive unifyJsonPrimitive(JsonPrimitive primitive)
    {
        return RAIntegration.createContentReplacement(primitive);
    }

    protected String getReplacementForFluid(String originalFluid)
    {
        return RAIntegration.getReplacementForFluid(originalFluid);
    }
}
