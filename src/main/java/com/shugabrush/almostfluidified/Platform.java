package com.shugabrush.almostfluidified;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.shugabrush.almostfluidified.unification.recipe.unifier.FluidRecipeHandlerFactory;

public interface Platform
{

    AlmostUnifiedPlatform.Platform getPlatform();

    boolean isModLoaded(String modId);

    boolean isClient();

    void bindRecipeHandlers(FluidRecipeHandlerFactory factory);
}
