package com.shugabrush.raintegration;

import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shugabrush.raintegration.unification.FluidReplacementData;
import com.shugabrush.raintegration.unification.FluidUnifyTag;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

@Mod(MoreUnification.MOD_ID)
@SuppressWarnings("removal")
public class MoreUnification
{

    public static final String MOD_ID = "raintegration";
    public static final Logger LOGGER = LogManager.getLogger();

    private static FluidReplacementData fluidReplacementData;

    public MoreUnification()
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
        event.enqueueWork(() ->
        {});
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
        fluidReplacementData = FluidReplacementData.load(tags);
    }

    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes)
    {
        LOGGER.info("Recipe Count: " + recipes.size());
    }

    public static Fluid getReplacementForFluid(ResourceLocation fluid)
    {
        return BuiltInRegistries.FLUID.get(fluidReplacementData.replacementMap().getReplacementForFluid(fluid));
    }

    public static Fluid getReplacementForFluid(Fluid fluid)
    {
        return getReplacementForFluid(BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static UnifyTag<Fluid> getPreferredTagForFluid(ResourceLocation fluid)
    {
        return fluidReplacementData.replacementMap().getPreferredTagForFluid(fluid);
    }

    public static ResourceLocation getPreferredFluidForTag(UnifyTag<Fluid> tag, Predicate<ResourceLocation> fluidFilter)
    {
        return fluidReplacementData.replacementMap().getPreferredFluidForTag(tag, fluidFilter);
    }

    public static JsonObject tryCreateContentReplacement(@Nullable JsonElement element)
    {
        if (element instanceof JsonObject object && element.toString().contains("fluid"))
        {
            return tryCreateContentReplacement(object);
        }
        return null;
    }

    public static JsonObject tryCreateContentReplacement(@Nullable JsonObject object)
    {
        if (object == null) return null;

        var fluidJson = object.get("fluid");
        if (fluidJson instanceof JsonArray fluidArray)
        {
            fluidArray = tryCreateContentReplacement(fluidArray);
            object.remove("fluid");
            object.add("fluid", fluidArray);
        }
        else if (fluidJson instanceof JsonObject fluidObj)
        {
            LOGGER.info("Here");
        }
        return object;
    }

    // This function is for debugging purposes only. It will slow down the final build.
    public static void writeAllRecipes(Map<ResourceLocation, JsonElement> recipes)
    {
        recipes.forEach((location, recipe) ->
        {
            try
            {
                Path path = Path.of("json-recipes/" + location.getPath() + ".json");
                Path currentFolder = Path.of(path.getName(0).toString() + "/" + location.getNamespace());
                if (!Files.exists(currentFolder))
                {
                    Files.createDirectory(currentFolder);
                }
                for (int i = 1; i < path.getNameCount() - 1; i++)
                {
                    currentFolder = Path.of(currentFolder.toString() + "/" + path.getName(i).toString());
                    if (!Files.exists(currentFolder))
                    {
                        Files.createDirectory(currentFolder);
                    }
                }

                char[] jsonCharArray = recipe.toString().toCharArray();
                String jsonString = "";

                for (char jsonChar : jsonCharArray)
                {
                    if (jsonChar == '{' || jsonChar == '}')
                    {
                        jsonString += "\n";
                        jsonString += jsonChar;
                        jsonString += "\n";
                    }
                    else
                    {
                        jsonString += jsonChar;
                    }
                }

                Files.writeString(Path.of("json-recipes/" + location.getNamespace() + "/" + location.getPath() + ".json"), jsonString);
            }
            catch (IOException e)
            {
                LOGGER.error(e);
            }
        });
    }

    private static JsonArray tryCreateContentReplacement(JsonArray array)
    {
        array.forEach(element ->
        {
            if (element instanceof JsonObject obj)
            {
                if (obj.get("content") instanceof JsonObject contentObj)
                {
                    if (contentObj.get("value") instanceof JsonArray valueArray)
                    {
                        valueArray.forEach(valueElement ->
                        {
                            if (valueElement instanceof JsonObject valueObj)
                            {
                                var tag = valueObj.get("tag");
                                if (tag != null && isValidFluid(tag.toString()))
                                {
                                    UnifyTag<Fluid> fluidTag = FluidUnifyTag.fluid(ResourceLocation.tryParse(tag.getAsString()));
                                    ResourceLocation fluidLocation = MoreUnification.getPreferredFluidForTag(fluidTag, r -> true);
                                    if (fluidLocation != null)
                                    {
                                        valueObj.remove("tag");
                                        valueObj.addProperty("fluid", fluidLocation.toString());
                                    }
                                }
                                else
                                {
                                    var fluid = valueObj.get("Fluid");
                                    if (fluid != null && isValidFluid(fluid.toString()))
                                    {
                                        ResourceLocation oldFluidLocation = ResourceLocation.tryParse(fluid.getAsString());
                                        ResourceLocation fluidLocation = BuiltInRegistries.FLUID.getKey(MoreUnification.getReplacementForFluid(oldFluidLocation));

                                        if (oldFluidLocation != fluidLocation)
                                        {
                                            valueObj.remove("fluid");
                                            valueObj.addProperty("fluid", fluidLocation.toString());
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        return array;
    }

    public static boolean isValidFluid(String fluid)
    {
        for (String f : ConfigHolder.instance.fluidConfigs.fluids)
        {
            if (f == fluid) return true;
        }
        return false;
    }
}
