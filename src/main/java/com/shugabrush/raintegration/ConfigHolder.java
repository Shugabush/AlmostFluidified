package com.shugabrush.raintegration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.unification.FluidUnification;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = RAIntegration.MOD_ID)
public class ConfigHolder
{
    public static ConfigHolder instance;

    public static void init()
    {
        if (instance == null)
        {
            instance = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
        }
    }

    @Configurable
    public FluidConfigs fluidConfigs = new FluidConfigs();

    public static class FluidConfigs
{

        @Configurable
        @Configurable.Comment({ "The fluid that's outputted by steam boilers.",
                "Default: gtceu:steam" })
        public String boilerFluidOutput = "gtceu:steam";
        private Fluid boilerFluid;

        @Configurable
        @Configurable.Comment({ "The experience to use for experience unification",
                "Default: industrialforegoing:essence" })
        public String experience = "industrialforegoing:essence";
        private Fluid experienceFluid;
        @Configurable
        @Configurable.Comment({ "The fluid that SGJourney's crystallizer uses.",
            "Default: sgjourney:liquid_naquadah"})
        public String crystallizerFluidInput = "sgjourney:liquid_naquadah";
        private Fluid crystallizerFluid;

        @Configurable
        @Configurable.Comment({ "The fluid that SGJourney's advanced crystallizer uses.",
            "Default: sgjourney:liquid_naquadah"})
        public String advancedCrystallizerFluidInput = "sgjourney:heavy_liquid_naquadah";
        private Fluid advancedCrystallizerFluid;

        public Fluid getBoilerFluid()
        {
            if (boilerFluid == null)
            {
                // Doing this in init() is before Fluids are properly registered, so we'll have to check each time here.
                try
                {
                    boilerFluid = FluidUnification
                            .getFluid(new ResourceLocation(boilerFluidOutput));
                } catch (Exception e)
                {
                    boilerFluid = FluidUnification.getFluid(new ResourceLocation("gtceu:steam"));
                }
            }
            return boilerFluid;
        }

        public Fluid getExperienceFluid()
        {
            if (experienceFluid == null)
            {
                experienceFluid = FluidUnification.getFluid(new ResourceLocation(experience));
            }
            return experienceFluid;
        }

        public Fluid getCrystallizerFluid()
        {
            if (crystallizerFluid == null)
            {
                crystallizerFluid = FluidUnification.getFluid(new ResourceLocation(crystallizerFluidInput));
            }
            return crystallizerFluid;
        }

        public Fluid getAdvancedCrystallizerFluid()
        {
            if (advancedCrystallizerFluid == null)
            {
                advancedCrystallizerFluid = FluidUnification.getFluid(new ResourceLocation(advancedCrystallizerFluidInput));
            }
            return advancedCrystallizerFluid;
        }
    }
}
