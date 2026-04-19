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

    public FluidRecipeUnifier()
    {
        lookupKeys = Set.of("tag", "fluid", "inputs", "outputs", "fluidInput", "fluidInputs", "fluidOutput", "fluidOutputs");
    }

    public void unifyFluidRecipe(JsonElement element)
    {
        if (element == null)
            return;

        if (element instanceof JsonPrimitive primitive)
        {
            unifyJsonPrimitive(primitive);
        }
        else if (element instanceof JsonArray array)
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
        }
    }

    protected JsonPrimitive unifyJsonPrimitive(JsonPrimitive primitive)
    {
        RAIntegration.LOGGER.info(primitive.toString());
        return RAIntegration.createContentReplacement(primitive);
    }

    protected String getReplacementForFluid(String originalFluid)
    {
        return RAIntegration.getReplacementForFluid(originalFluid);
    }
}
