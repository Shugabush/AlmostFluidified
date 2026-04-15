package com.shugabrush.raintegration;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.UnifyTag;
import com.shugabrush.raintegration.unification.FluidUnifyTag;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Config(id = MoreUnification.MOD_ID, filename = "almostunified/more-unification")
public class ConfigHolder
{

    public static ConfigHolder instance;

    public static void init()
    {
        if (instance == null)
        {
            instance = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.json())
                    .getConfigInstance();
            instance.fluidConfigs.init();
        }
    }

    @Configurable
    public FluidConfigs fluidConfigs = new FluidConfigs();

    public class FluidConfigs
    {

        public void init()
        {
            for (PriorityOverride priority : priorityOverrides)
            {
                try
                {
                    priorityOverrideMap.put(ResourceLocation.parse(priority.modId), priority.fluidTag);
                }
                catch (Exception e)
                {
                    MoreUnification.LOGGER.error(e.getMessage(), e);
                }
            }
        }

        @Nullable
        private Set< UnifyTag< Fluid>> bakedTagsCache;

        @Configurable
        @Configurable.Comment("Mod priorities. Same as almost unified except for fluids, not items and blocks.")
        public String[] modPriorities =
        {
                "minecraft", "mekanism", "gtceu"
        };

        public class PriorityOverride
        {
            @Configurable
            public String modId = "";

            @Configurable
            public String fluidTag = "";
        }

        @Configurable
        @Configurable.Comment("Priority overrides. Same as almost unified except for fluids, not items and blocks.")
        public PriorityOverride[] priorityOverrides =
        {
                new PriorityOverride()
        };
        Map< ResourceLocation, String> priorityOverrideMap = new HashMap<>();

        public String getPriorityOverride(ResourceLocation location)
        {
            return priorityOverrideMap.get(location);
        }

        @Configurable
        @Configurable.Comment("Possible fluid tag formats for each fluid")
        public String[] fluidTags =
        {
                "minecraft:{fluid}", "forge:{fluid}"
        };

        @Configurable
        @Configurable.Comment("Fluid tags to ignore")
        public String[] ignoredTags = {};

        @Configurable
        @Configurable.Comment("All fluids that you want unified")
        public String[] fluids =
        {
                "water", "lava", "milk", "oxygen", "hydrogen", "chlorine", "steam", "helium"
        };

        public Set< UnifyTag< Fluid>> bakeAndValidateTags(Map< ResourceLocation, Collection< Holder< Fluid>>> tags)
        {
            return bakeTags(tags::containsKey);
        }

        private Set< UnifyTag< Fluid>> bakeTags(Predicate< ResourceLocation> tagValidator)
        {
            if (bakedTagsCache != null)
            {
                return bakedTagsCache;
            }

            Set< UnifyTag< Fluid>> result = new HashSet<>();
            Set< UnifyTag< Fluid>> wrongTags = new HashSet<>();

            for (String tag : fluidTags)
            {
                for (String fluid : fluids)
                {
                    String replace = tag.replace("{fluid}", fluid);
                    ResourceLocation asRL = ResourceLocation.tryParse(replace);
                    if (asRL == null)
                    {
                        MoreUnification.LOGGER.warn("Could not bake tag <{}> with fluid <{}>", tag, fluid);
                        continue;
                    }

                    boolean ignore = false;
                    UnifyTag< Fluid> f = FluidUnifyTag.fluid(asRL);
                    for (String ignoredTag : ignoredTags)
                    {
                        if (ignoredTag == f.toString())
                        {
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore)
                        continue;

                    if (!tagValidator.test(asRL))
                    {
                        wrongTags.add(f);
                        continue;
                    }

                    result.add(f);
                }
            }

            if (!wrongTags.isEmpty())
            {
                MoreUnification.LOGGER.warn(
                        "The following tags are invalid and will be ignored: {}",
                        wrongTags.stream().map(UnifyTag::location).collect(Collectors.toList()));
            }

            bakedTagsCache = result;
            return result;
        }
    }
}
