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

import com.almostreliable.unified.config.Config;
import com.google.gson.*;
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

    static final Set< String> propertyWhitelist = Set.of("tag", "fluid", "value", "input", "output", "inputs",
            "outputs", "fluidInput", "fluidOutput", "content");

    // Resource location is the tag, the fluid list represents all fluids that have that tag
    private static Map< ResourceLocation, List< Fluid>> fluidCollections = new HashMap<>();

    // Resource location is the tag, the fluid is the unified fluid for that fluid tag
    public static Map< ResourceLocation, Fluid> unifiedFluids = new HashMap<>();

    @Nullable
    private static FluidUnifyConfig unifyConfig;

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

    public static void onTagLoaderReload(Map< ResourceLocation, Collection< Holder< Fluid>>> tags)
    {
        unifyConfig = Config.load("fluids", new FluidUnifyConfig.Serializer());

        Set< ResourceLocation> bakedTags = unifyConfig.bakeAndValidateTags(tags);
        bakedTags.forEach(location ->
        {
            Collection< Holder< Fluid>> fluidHolders = tags.get(location);
            if (fluidHolders != null)
            {
                List< Fluid> fluids = new ArrayList<>();
                fluidHolders.forEach(holder ->
                {
                    fluids.add(holder.get());
                });

                fluidCollections.put(location, fluids);
                for (Fluid fluid : fluids)
                {
                    ResourceLocation fluidLocation = BuiltInRegistries.FLUID.getKey(fluid);
                    String fluidLocationStr = fluidLocation.toString();

                    // Flowing fluids generally aren't part of recipes so they don't need to be unified
                    if (fluidLocationStr.contains("flowing"))
                        continue;

                    for (String priority : unifyConfig.getModPriorities())
                    {
                        if (fluidLocationStr.split(":")[0].equals(priority) && !unifiedFluids.containsKey(location))
                        {
                            unifiedFluids.put(location, fluid);
                        }
                    }
                }
            }
        });
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

    public static JsonElement unifyFluidRecipe(JsonElement element)
    {
        if (element == null)
            return null;

        JsonElement copyElement = element.deepCopy();
        if (copyElement instanceof JsonPrimitive primitive)
        {
            String primitiveString = primitive.toString();
            try
            {
                primitiveString = primitiveString.substring(primitiveString.indexOf("\"") + 1,
                        primitiveString.lastIndexOf("\""));
                String unifiedPrimitiveString = getReplacementForFluid(primitiveString);
                return JsonParser.parseString("\"" + unifiedPrimitiveString + "\"").getAsJsonPrimitive();
            }
            catch (Exception e)
            {
                String unifiedPrimitiveString = getReplacementForFluid(primitiveString);
                return JsonParser.parseString(unifiedPrimitiveString).getAsJsonPrimitive();
            }
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
                        // Note: this can run multiple times per element,
                        // Might be worth checking if the propertyElement is a primitive first
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

    public static String getReplacementForFluid(String originalFluid)
    {
        for (Map.Entry< ResourceLocation, List< Fluid>> entry : fluidCollections.entrySet())
        {
            ResourceLocation tag = entry.getKey();
            List< Fluid> fluids = entry.getValue();
            Fluid unifiedFluid = unifiedFluids.get(tag);
            String unifiedFluidStr = BuiltInRegistries.FLUID.getKey(unifiedFluid).toString();
            if (originalFluid.contains(tag.toString()))
            {
                return originalFluid.replace(tag.toString(), unifiedFluidStr);
            }
            for (Fluid fluid : fluids)
            {
                String fluidStr = BuiltInRegistries.FLUID.getKey(fluid).toString();
                if (fluid != unifiedFluid && originalFluid.contains(fluidStr))
                {
                    return originalFluid.replace(fluidStr, unifiedFluidStr);
                }
            }
        }
        return originalFluid;
    }
}
