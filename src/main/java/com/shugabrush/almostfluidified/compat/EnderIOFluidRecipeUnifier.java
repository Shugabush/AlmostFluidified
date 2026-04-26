package com.shugabrush.almostfluidified.compat;

import com.shugabrush.almostfluidified.api.FluidRecipeUnifier;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifierBuilder;

import java.util.List;

public class EnderIOFluidRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        List.of("fluid")
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(json)));
    }
}
