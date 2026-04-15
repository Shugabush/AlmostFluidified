package com.shugabrush.raintegration;

import com.google.gson.stream.JsonReader;
import com.shugabrush.raintegration.unification.FluidUnification;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringReader;
import java.util.*;

@Mod(RAIntegration.MOD_ID)
@SuppressWarnings("removal")
public class RAIntegration
{

    public static final String MOD_ID = "raintegration";
    public static final Logger LOGGER = LogManager.getLogger();

    public RAIntegration()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Most other events are fired on Forge's bus.
        // If we want to use annotations to register event listeners,
        // we need to register our object like this!
        MinecraftForge.EVENT_BUS.register(this);

        ConfigHolder.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {

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


    public static final Map<String, String> fluids = Map.of("forge:steam", "mekanism:steam", "forge:oxygen", "mekanism:oxygen", "forge:hydrogen", "mekanism:hydrogen");


    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes)
    {
        long startTime = System.nanoTime();
        recipes.forEach((location, recipe) -> {
            JsonElement unifiedRecipe = unifyFluidRecipe(recipe);
        });
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        LOGGER.info("Fluids unified for recipes in {} ms", durationMs);
    }

    static final Set<String> propertyWhitelist = Set.of("tag", "fluid", "input", "output");

    public static JsonElement unifyFluidRecipe(JsonElement element)
    {
        if (element == null) return null;
        JsonElement copyElement = element.deepCopy();
        if (copyElement instanceof JsonPrimitive primitive) {
            String primitiveString = FluidUnification.getUnifiedFluidString(primitive.toString());
            JsonPrimitive unifiedPrimitive = JsonParser.parseString(primitiveString).getAsJsonPrimitive();
            LOGGER.info("{} vs {}", primitiveString, unifiedPrimitive);
        } else if (copyElement instanceof JsonArray array) {
            for (int i = 0; i < array.size(); i++) {
                JsonElement arrayElement = array.get(i);
                array.set(i, unifyFluidRecipe(arrayElement));
            }
        } else if (copyElement instanceof JsonObject object) {
            Set<String> objectProperties = object.deepCopy().keySet();

            Iterator<String> propertyIterator = objectProperties.iterator();
            while (propertyIterator.hasNext()) {
                String currentProperty = propertyIterator.next();
                if (propertyWhitelist.contains(currentProperty)) {
                    JsonElement propertyElement = object.get(currentProperty);
                    JsonElement unifiedPropertyElement = unifyFluidRecipe(propertyElement);
                    if (propertyElement instanceof JsonPrimitive propertyPrimitive) {
                        LOGGER.info("{}:{}", currentProperty, propertyPrimitive.toString());
                    }
                    if (!propertyElement.equals(unifiedPropertyElement)) {
                        object.remove(currentProperty);
                        object.add(currentProperty, unifiedPropertyElement);
                    }
                }
                propertyIterator.remove();
            }
        }

        return copyElement;
    }
}
