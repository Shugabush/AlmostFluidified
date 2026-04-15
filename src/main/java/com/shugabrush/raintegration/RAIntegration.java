package com.shugabrush.raintegration;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.*;
import com.shugabrush.raintegration.unification.FluidUnification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod(RAIntegration.MOD_ID)
@SuppressWarnings("removal")
public class RAIntegration
{

    public static final String MOD_ID = "raintegration";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final Map< String, String> fluids = Map.of("forge:steam", "gtceu:steam", "forge:oxygen",
            "gtceu:oxygen", "forge:hydrogen", "gtceu:hydrogen");

    private static Map< ResourceLocation, Collection< Holder< Fluid>>> fluidTags = new HashMap<>();

    private static Map<Fluid, ResourceLocation> tagFluids = new HashMap<>();

    public RAIntegration()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        modEventBus.addListener(EventPriority.LOWEST, this::afterRegister);

        // Most other events are fired on Forge's bus.
        // If we want to use annotations to register event listeners,
        // we need to register our object like this!
        MinecraftForge.EVENT_BUS.register(this);

        DeferredRegister< Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID);

        ConfigHolder.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {

        });
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {}

    private void afterRegister(FMLCommonSetupEvent event)
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

    public static void initFluidTags(Map< ResourceLocation, Collection< Holder< Fluid>>> tags)
    {
        fluidTags = tags;
        for (String priorityFluid : ConfigHolder.instance.fluidConfigs.priorityFluids)
        {
            fluidTags.forEach((location, holder) ->
            {
                for (Holder<Fluid> fluidHolder : holder)
                {
                    ResourceLocation fluidLocation = BuiltInRegistries.FLUID.getKey(fluidHolder.get());
                    if (fluidLocation.toString() == priorityFluid)
                    {
                        tagFluids.put(fluidHolder.get(), fluidLocation);
                    }
                }
            });
        }
    }

    public static Collection< Holder< Fluid>> getFluids(ResourceLocation resourceLocation)
    {
        return fluidTags.get(resourceLocation);
    }

    public static void onRecipeManagerReload(Map< ResourceLocation, JsonElement> recipes)
    {
        long startTime = System.nanoTime();
        recipes.forEach((location, recipe) ->
        {
            JsonElement unifiedRecipe = unifyFluidRecipe(recipe);
            recipes.put(location, unifiedRecipe);
        });
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        LOGGER.info("Fluids unified for recipes in {} ms", durationMs);
    }

    static final Set< String> propertyWhitelist = Set.of("tag", "fluid", "value", "input", "output", "inputs",
            "outputs", "fluidInput", "fluidOutput", "content");

    public static JsonElement unifyFluidRecipe(JsonElement element)
    {
        if (element == null)
            return null;

        JsonElement copyElement = element.deepCopy();
        if (copyElement instanceof JsonPrimitive primitive)
        {
            String primitiveString = FluidUnification.getUnifiedFluidString(primitive.toString());
            JsonPrimitive unifiedPrimitive = JsonParser.parseString(primitiveString).getAsJsonPrimitive();
            return unifiedPrimitive;
        }
        else if (copyElement instanceof JsonArray array)
        {
            for (int i = 0; i < array.size(); i++)
            {
                JsonElement arrayElement = array.get(i);
                array.set(i, unifyFluidRecipe(arrayElement));
            }
        }
        else if (copyElement instanceof JsonObject object)
        {
            Set< String> objectProperties = object.deepCopy().keySet();

            Iterator< String> propertyIterator = objectProperties.iterator();
            while (propertyIterator.hasNext())
            {
                String currentProperty = propertyIterator.next();
                if (propertyWhitelist.contains(currentProperty))
                {
                    JsonElement propertyElement = object.get(currentProperty);
                    JsonElement unifiedPropertyElement = unifyFluidRecipe(propertyElement);
                    if (!propertyElement.equals(unifiedPropertyElement))
                    {
                        object.remove(currentProperty);
                        if (currentProperty.equals("tag"))
                        {
                            currentProperty = "fluid";
                        }
                        object.add(currentProperty, unifiedPropertyElement);
                    }
                }
                propertyIterator.remove();
            }
        }

        return copyElement;
    }
}
