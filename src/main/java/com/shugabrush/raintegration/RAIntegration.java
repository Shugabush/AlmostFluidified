package com.shugabrush.raintegration;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.almostreliable.unified.config.Config;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.shugabrush.raintegration.unification.FluidReplacementData;
import com.shugabrush.raintegration.unification.RAIntegrationRuntime;
import com.shugabrush.raintegration.unification.recipe.unifier.FluidRecipeHandlerFactory;
import com.shugabrush.raintegration.unification.utils.FluidTagOwnerships;
import com.shugabrush.raintegration.unification.utils.FluidTagReloadHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import javax.annotation.Nullable;

@Mod(RAIntegration.MOD_ID)
@SuppressWarnings("removal")
public class RAIntegration
{

    public static final String MOD_ID = "raintegration";
    public static final Logger LOGGER = LogManager.getLogger();

    // A whitelist of properties to check for unification for. If any recipes don't work, check this list and see if the
    // element's property name is in it.
    static final Set< String> propertyWhitelist = Set.of("tag", "fluid_tag", "fluid", "value", "input", "output",
            "inputs",
            "outputs", "fluidInput", "fluidOutput", "inputFluid", "outputFluid", "content", "result", "results",
            "extraInput", "extraInputs");

    static final Set< String> propertyBlacklist = Set.of("item", "items", "itemInput", "itemOutput", "itemInputs",
            "itemOutputs");

    // Resource location is the tag, the fluid list represents all fluids that have that tag
    private static Map< ResourceLocation, Collection< Fluid>> fluidCollections = new HashMap<>();

    // Resource location is the tag, the fluid is the unified fluid for that fluid tag
    public static Map< ResourceLocation, Fluid> unifiedFluids = new HashMap<>();

    @Nullable
    private static RAIntegrationRuntime RUNTIME;

    @Nullable
    private static FluidUnifyConfig unifyConfig;

    public RAIntegration()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Most other events are fired on Forge's bus.
        // If we want to use annotations to register event listeners,
        // we need to register our object like this!
        MinecraftForge.EVENT_BUS.register(this);

        DeferredRegister< Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID);

        ConfigHolder.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        unifyConfig = Config.load("fluids", new FluidUnifyConfig.Serializer());
        event.enqueueWork(() ->
        {

        });
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {}

    /**
     * Create a ResourceLocation in the format "modid:path"
     *
     * @param path
     * @return ResourceLocation with the namespace of your mod
     */
    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void onTagLoaderReload(Map< ResourceLocation, Collection< Holder< Fluid>>> tags)
    {
        FluidRecipeHandlerFactory recipeHandlerFactory = new FluidRecipeHandlerFactory();
        PlatformForge.INSTANCE.bindRecipeHandlers(recipeHandlerFactory);

        FluidTagReloadHandler.applyCustomTags(unifyConfig);

        FluidTagOwnerships tagOwnerships = new FluidTagOwnerships(
                unifyConfig.bakeAndValidateTags(tags),
                unifyConfig.getTagOwnerships());
        tagOwnerships.applyOwnerships(tags);

        FluidReplacementData replacementData = FluidReplacementData.load(tags, unifyConfig, tagOwnerships);

        RUNTIME = new RAIntegrationRuntime(
                unifyConfig,
                replacementData.filteredTagMap(),
                replacementData.replacementMap(),
                recipeHandlerFactory);
    }

    public static void onRecipeManagerReload(Map< ResourceLocation, JsonElement> recipes)
    {
        Preconditions.checkNotNull(RUNTIME, "RAIntegrationRuntime was not loaded correctly");
        RUNTIME.run(recipes, false);
    }
}
