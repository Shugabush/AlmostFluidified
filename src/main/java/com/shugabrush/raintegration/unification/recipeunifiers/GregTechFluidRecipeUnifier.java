package com.shugabrush.raintegration.unification.recipeunifiers;

import java.util.Set;

public class GregTechFluidRecipeUnifier extends FluidRecipeUnifier
{
    public GregTechFluidRecipeUnifier()
    {
        lookupKeys = Set.of("value", "content", "inputs", "outputs");
        primitiveLookupKeys = Set.of("fluid", "tag");
    }
}
