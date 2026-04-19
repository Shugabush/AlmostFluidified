package com.shugabrush.raintegration.unification.recipe;

import java.util.Set;

public class IndustrialForegoingFluidRecipeUnifier extends FluidUnifier
{

    public IndustrialForegoingFluidRecipeUnifier()
    {
        lookupKeys = Set.of();
        primitiveLookupKeys = Set.of("input", "output", "inputFluid", "outputFluid");
    }
}
