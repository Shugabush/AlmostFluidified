package com.shugabrush.raintegration;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.shugabrush.raintegration.unification.FluidReplacementData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Map;

@Mod(MoreUnification.MOD_ID)
@SuppressWarnings("removal")
public class MoreUnification {

    public static final String MOD_ID = "raintegration";
    public static final Logger LOGGER = LogManager.getLogger();

    private static FluidReplacementData fluidReplacementData;

    public MoreUnification() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Most other events are fired on Forge's bus.
        // If we want to use annotations to register event listeners,
        // we need to register our object like this!
        MinecraftForge.EVENT_BUS.register(this);

        ConfigHolder.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {}

    /**
     * Create a ResourceLocation in the format "modid:path"
     *
     * @param path
     * @return ResourceLocation with the namespace of your mod
     */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void onTagLoaderReload(Map<ResourceLocation, Collection<Holder<Fluid>>> tags) {
        fluidReplacementData = FluidReplacementData.load(tags);
    }

    public static Fluid getReplacementForFluid(ResourceLocation fluid) {
        return BuiltInRegistries.FLUID.get(fluidReplacementData.replacementMap().getReplacementForFluid(fluid));
    }

    public static Fluid getReplacementForFluid(Fluid fluid) {
        return getReplacementForFluid(BuiltInRegistries.FLUID.getKey(fluid));
    }
}
