package com.shugabrush.raintegration.unification.recipeunifiers;

import java.util.Set;

public class IndustrialForegoingFluidRecipeUnifier extends FluidRecipeUnifier
{
    public IndustrialForegoingFluidRecipeUnifier()
    {
        lookupKeys = Set.of("inputFluid", "outputFluid");
    }
}
