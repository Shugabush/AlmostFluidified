package com.shugabrush.almostfluidified.compat;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifier;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifierBuilder;
import com.shugabrush.almostfluidified.unification.FluidRecipeContext;

import java.util.List;

import javax.annotation.Nullable;

public class EnderIOFluidRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        List.of("fluid")
                .forEach(key -> builder.put(key, this::createIngredientReplacement));
    }

    private JsonElement createIngredientReplacement(@Nullable JsonElement element, FluidRecipeContext context)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        tryCreateIngredientReplacement(copy, context);
        return element.equals(copy) ? null : copy;
    }

    // Don't replace with tag, just use unified fluid
    private void tryCreateIngredientReplacement(@Nullable JsonElement element, FluidRecipeContext context)
    {
        if (element == null)
            return;
        if (element instanceof JsonArray array)
        {
            for (JsonElement e : array)
            {
                tryCreateIngredientReplacement(e, context);
            }
        }
        else if (element instanceof JsonObject object)
        {
            if (object.get("fluid") instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluid = ResourceLocation.tryParse(primitive.getAsString());
                ResourceLocation unifiedFluid = context.getReplacementForFluid(fluid);
                if (unifiedFluid != null)
                {
                    object.addProperty("fluid", unifiedFluid.toString());
                }
            }
        }
    }
}
