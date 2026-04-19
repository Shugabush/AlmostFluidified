package com.shugabrush.raintegration.compat;

import com.shugabrush.raintegration.api.FluidRecipeUnifier;
import com.shugabrush.raintegration.api.FluidRecipeUnifierBuilder;

import java.util.List;

public class MekanismFluidRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        List.of("fluidInput")
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(json)));

        List.of("fluidOutput")
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createResultReplacement(json)));
    }
}
