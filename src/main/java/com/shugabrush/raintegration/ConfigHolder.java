package com.shugabrush.raintegration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.unification.FluidUnification;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = RAIntegration.MOD_ID)
public class ConfigHolder {

    public static ConfigHolder instance;

    public static void init() {
        if (instance == null) {
            instance = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
        }
        instance.machineConfigs.init();
    }

    @Configurable
    public MachineConfigs machineConfigs = new MachineConfigs();

    public static class MachineConfigs {

        public void init() {}

        @Configurable
        @Configurable.Comment({ "The fluid that's outputted by steam boilers.",
                "Default: gtceu:steam" })
        public String boilerFluidOutput = "gtceu:steam";
        private Fluid boilerFluid;

        public Fluid getBoilerFluid() {
            if (boilerFluid == null) {
                // Doing this in init() is before Fluids are properly registered, so we'll have to check each time here.
                try {
                    String[] boilerResourcePath = boilerFluidOutput.split(":");
                    boilerFluid = FluidUnification
                            .getFluid(new ResourceLocation(boilerResourcePath[0], boilerResourcePath[1]));
                } catch (Exception e) {
                    boilerFluid = FluidUnification.getFluid(new ResourceLocation("gtceu", "steam"));
                }
            }
            return boilerFluid;
        }
    }
}
