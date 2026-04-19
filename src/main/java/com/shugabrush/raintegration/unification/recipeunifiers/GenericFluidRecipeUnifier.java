package com.shugabrush.raintegration.unification.recipeunifiers;

import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

import java.util.Set;

public class GenericFluidRecipeUnifier implements FluidRecipeUnifier
{
    public static final GenericRecipeUnifier INSTANCE = new GenericRecipeUnifier();
    private static final Set<String> INPUT_KEYS = Set.of(
            "input",
            "inputs",
            "ingredient",
            "ingredients",
            "inputFluids",
            "fluidInputs"
    );
    private static final Set<String> OUTPUT_KEYS = Set.of(
            "output",
            "outputs",
            "result",
            "results",
            "outputFluids",
            "fluidOutputs"
    );

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
