package com.shugabrush.raintegration.unification.recipeunifiers;

import java.util.Set;

public class IndustrialForegoingFluidRecipeUnifier extends FluidUnifier
{

    public IndustrialForegoingFluidRecipeUnifier()
    {
        lookupKeys = Set.of();
        primitiveLookupKeys = Set.of("input", "output", "inputFluid", "outputFluid");
    }
}
