package com.shugabrush.raintegration;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidUnifyConfig extends Config
{
    public FluidUnifyConfig(
            List<String> modPriorities,
            List<String> unbakedTags,
            List<String> fluids,
            Map<ResourceLocation, String> priorityOverrides
    )
    {
        this.modPriorities = modPriorities;
        this.unbakedTags = unbakedTags;
        this.fluids = fluids;
        this.priorityOverrides = priorityOverrides;
    }

    private List<String> modPriorities;
    private List<String> unbakedTags;
    private List<String> fluids;
    private Map<ResourceLocation, String> priorityOverrides;

    @Nullable
    private Set<ResourceLocation> bakedTagsCache;

    public List<String> getModPriorities()
    {
        return Collections.unmodifiableList(modPriorities);
    }

    public Map<ResourceLocation, String> getPriorityOverrides()
    {
        return Collections.unmodifiableMap(priorityOverrides);
    }

    public Set<ResourceLocation> bakeTags()
    {
        return bakeTags($ -> true);
    }

    public Set<ResourceLocation> bakeAndValidateTags(Map<ResourceLocation, Collection<Holder<Fluid>>> tags)
    {
        return bakeTags(tags::containsKey);
    }

    private Set<ResourceLocation> bakeTags(Predicate<ResourceLocation> tagValidator)
    {
        if (bakedTagsCache != null)
        {
            return bakedTagsCache;
        }

        Set<ResourceLocation> result = new HashSet<>();
        Set<ResourceLocation> wrongTags = new HashSet<>();

        for (String tag : unbakedTags)
        {
            for (String fluid : fluids)
            {
                String replace = tag.replace("{fluid}", fluid);
                ResourceLocation asRL = ResourceLocation.tryParse(replace);
                if (asRL == null)
                {
                    RAIntegration.LOGGER.warn("Could not bake tag <{}> with material <{}>", tag, fluid);
                    continue;
                }

                if (!tagValidator.test(asRL))
                {
                    wrongTags.add(asRL);
                    continue;
                }
                result.add(asRL);
            }
        }
        if (!wrongTags.isEmpty())
        {
            RAIntegration.LOGGER.warn("The following tags are invalid and will be ignored: {}",
                    new ArrayList<>(wrongTags));
        }

        bakedTagsCache = result;
        return result;
    }

    public static class Serializer extends Config.Serializer<FluidUnifyConfig>
    {

        public static final String MOD_PRIORITIES = "modPriorities";
        public static final String TAGS = "tags";
        public static final String FLUIDS = "fluids";
        public static final String PRIORITY_OVERRIDES = "priorityOverrides";

        @Override
        public FluidUnifyConfig deserialize(JsonObject json)
        {
            List<String> modPriorities = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)), FluidDefaults.getModPriorities());
            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), FluidDefaults.getTags());
            List<String> fluids = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(FLUIDS)), FluidDefaults.FLUIDS);

            Map<ResourceLocation, String> priorityOverrides = safeGet(
                    () -> JsonUtils.deserializeMap(
                            json,
                            PRIORITY_OVERRIDES,
                            e-> new ResourceLocation(e.getKey()),
                            e -> e.getValue().getAsString()
                    ),
                    new HashMap<>()
            );

            return new FluidUnifyConfig(
                    modPriorities,
                    tags,
                    fluids,
                    priorityOverrides
            );
        }

        @Override
        public JsonObject serialize(FluidUnifyConfig config)
        {
            JsonObject json = new JsonObject();
            json.add(MOD_PRIORITIES, JsonUtils.toArray(config.modPriorities));
            json.add(TAGS, JsonUtils.toArray(config.unbakedTags));
            json.add(FLUIDS, JsonUtils.toArray(config.fluids));
            JsonObject priorityOverrides = new JsonObject();
            config.priorityOverrides.forEach((tag, mod) ->
            {
                priorityOverrides.addProperty(tag.toString(), mod);
            });
            json.add(PRIORITY_OVERRIDES, priorityOverrides);

            return json;
        }
    }
}
