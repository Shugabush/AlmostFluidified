package com.shugabrush.raintegration;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.shugabrush.raintegration.unification.recipe.unifier.FluidRecipeHandlerFactory;

public interface Platform
{

    AlmostUnifiedPlatform.Platform getPlatform();

    boolean isModLoaded(String modId);

    boolean isClient();

    void bindRecipeHandlers(FluidRecipeHandlerFactory factory);
}
