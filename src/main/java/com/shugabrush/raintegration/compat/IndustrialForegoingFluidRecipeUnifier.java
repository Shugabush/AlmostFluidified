package com.shugabrush.raintegration.compat;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.shugabrush.raintegration.api.FluidRecipeUnifier;
import com.shugabrush.raintegration.api.FluidRecipeUnifierBuilder;
import com.shugabrush.raintegration.unification.FluidRecipeContext;

import java.util.List;

public class IndustrialForegoingFluidRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        List.of(
                "inputFluid").forEach(
                        key -> builder.put(
                                key,
                                (json, ctx) -> ctx.createIngredientReplacement(
                                        json, object -> replaceFluid(object, ctx))));
        List.of(
                "output").forEach(
                        key -> builder.put(
                                key,
                                (json, ctx) -> ctx.createResultReplacement(
                                        json, true, primitive -> replaceFluid(primitive, ctx), key)));
    }

    // Custom method for replacing fluid output
    // The string name or tag could be inside the string property
    private JsonElement replaceFluid(JsonElement object, FluidRecipeContext context)
    {
        String objectStr = object.toString();
        int startIndex = objectStr.indexOf("FluidName") + 12;
        if (startIndex >= 0)
        {
            String fluidStr = objectStr.substring(startIndex, objectStr.length() - 4);
            ResourceLocation fluid = ResourceLocation.tryParse(fluidStr);
            if (fluid != null)
            {
                ResourceLocation unifiedFluid = context.getReplacementForFluid(fluid);
                if (unifiedFluid != null)
                {
                    String unifiedObjectStr = objectStr.replace(fluidStr, unifiedFluid.toString());
                    if (!unifiedObjectStr.equals(objectStr))
                    {
                        return JsonParser.parseString(unifiedObjectStr);
                    }
                }
            }
        }
        return null;
    }
}
