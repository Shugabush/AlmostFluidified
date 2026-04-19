package com.shugabrush.raintegration.unification.recipe.unifier;

import com.shugabrush.raintegration.api.FluidRecipeUnifier;
import com.shugabrush.raintegration.api.FluidRecipeUnifierBuilder;

import java.util.Set;

public class GenericFluidRecipeUnifier implements FluidRecipeUnifier
{

    public static final GenericFluidRecipeUnifier INSTANCE = new GenericFluidRecipeUnifier();
    private static final Set< String> INPUT_KEYS = Set.of(
            "input",
            "inputs",
            "ingredient",
            "ingredients",
            "inputFluids",
            "fluidInputs");
    private static final Set< String> OUTPUT_KEYS = Set.of(
            "output",
            "outputs",
            "result",
            "results",
            "outputFluids",
            "fluidOutputs");

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        for (String inputKey : INPUT_KEYS)
        {
            builder.put(inputKey, (json, ctx) -> ctx.createIngredientReplacement(json));
        }

        for (String outputKey : OUTPUT_KEYS)
        {
            builder.put(outputKey, (json, ctx) -> ctx.createResultReplacement(json));
        }
    }
}
