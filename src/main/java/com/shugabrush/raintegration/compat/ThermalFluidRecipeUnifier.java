package com.shugabrush.raintegration.compat;

import com.shugabrush.raintegration.api.FluidRecipeUnifier;
import com.shugabrush.raintegration.api.FluidRecipeUnifierBuilder;

import java.util.List;

public class ThermalFluidRecipeUnifier implements FluidRecipeUnifier
{
    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        List.of("ingredient", "ingredients")
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(json, "fluid", "fluid_tag", true)));

        List.of("result")
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createResultReplacement(json, "fluid", "fluid_tag", true)));
    }
}
