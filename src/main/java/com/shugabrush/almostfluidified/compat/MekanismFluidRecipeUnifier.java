package com.shugabrush.almostfluidified.compat;

import com.shugabrush.almostfluidified.api.FluidRecipeUnifier;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifierBuilder;

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
