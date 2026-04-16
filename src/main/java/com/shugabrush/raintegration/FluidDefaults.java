package com.shugabrush.raintegration;

import java.util.List;

public class FluidDefaults
{

    public static final List< String> FLUIDS = List.of(
            "hydrogen",
            "oxygen",
            "steam",
            "chlorine",
            "ethylene");

    public static List< String> getModPriorities()
    {
        return List.of(
                "kubejs",
                "crafttweaker",
                "mekanism",
                "gtceu",
                "create",
                "thermal",
                "immersiveengineering");
    }

    public static List< String> getTags()
    {
        return List.of("forge:{fluid}");
    }
}
