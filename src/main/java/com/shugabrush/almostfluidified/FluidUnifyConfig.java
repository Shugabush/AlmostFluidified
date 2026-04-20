package com.shugabrush.almostfluidified;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonObject;
import com.shugabrush.almostfluidified.unification.utils.FluidUnifyTag;

import java.util.*;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class FluidUnifyConfig extends Config
{

    private final List< String> modPriorities;
    private final List< String> unbakedTags;
    private final List< String> fluids;
    private final Map< ResourceLocation, String> priorityOverrides;
    private final Map< ResourceLocation, Set< ResourceLocation>> customTags;
    private final Map< ResourceLocation, Set< ResourceLocation>> tagOwnerships;

    public FluidUnifyConfig(
                            List< String> modPriorities,
                            List< String> unbakedTags,
                            List< String> fluids,
                            Map< ResourceLocation, String> priorityOverrides,
                            Map< ResourceLocation, Set< ResourceLocation>> customTags,
                            Map< ResourceLocation, Set< ResourceLocation>> tagOwnerships)
    {
        this.modPriorities = modPriorities;
        this.unbakedTags = unbakedTags;
        this.fluids = fluids;
        this.customTags = customTags;
        this.priorityOverrides = priorityOverrides;
        this.tagOwnerships = tagOwnerships;
    }

    @Nullable
    private Set< UnifyTag< Fluid>> bakedTagsCache;

    public List< String> getModPriorities()
    {
        return Collections.unmodifiableList(modPriorities);
    }

    public Map< ResourceLocation, String> getPriorityOverrides()
    {
        return Collections.unmodifiableMap(priorityOverrides);
    }

    public Map< ResourceLocation, Set< ResourceLocation>> getCustomTags()
    {
        return Collections.unmodifiableMap(customTags);
    }

    public Map< ResourceLocation, Set< ResourceLocation>> getTagOwnerships()
    {
        return Collections.unmodifiableMap(tagOwnerships);
    }

    public Set< UnifyTag< Fluid>> bakeTags()
    {
        return bakeTags($ -> true);
    }

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

        for (String tag : unbakedTags)
        {
            for (String fluid : fluids)
            {
                String replace = tag.replace("{fluid}", fluid);
                ResourceLocation asRL = ResourceLocation.tryParse(replace);
                if (asRL == null)
                {
                    AlmostFluidified.LOGGER.warn("Could not bake tag <{}> with material <{}>", tag, fluid);
                    continue;
                }

                UnifyTag< Fluid> t = FluidUnifyTag.fluid(asRL);

                if (!tagValidator.test(asRL))
                {
                    wrongTags.add(t);
                    continue;
                }
                result.add(t);
            }
        }
        if (!wrongTags.isEmpty())
        {
            AlmostFluidified.LOGGER.warn("The following tags are invalid and will be ignored: {}",
                    new ArrayList<>(wrongTags));
        }

        bakedTagsCache = result;
        return result;
    }

    public static class Serializer extends Config.Serializer< FluidUnifyConfig>
    {

        public static final String MOD_PRIORITIES = "modPriorities";
        public static final String TAGS = "tags";
        public static final String FLUIDS = "fluids";
        public static final String PRIORITY_OVERRIDES = "priorityOverrides";
        public static final String CUSTOM_TAGS = "customTags";
        public static final String TAG_OWNERSHIPS = "tagOwnerships";

        @Override
        public FluidUnifyConfig deserialize(JsonObject json)
        {
            List< String> modPriorities = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    FluidDefaults.getModPriorities());
            List< String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), FluidDefaults.getTags());
            List< String> fluids = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(FLUIDS)), FluidDefaults.FLUIDS);

            Map< ResourceLocation, String> priorityOverrides = safeGet(
                    () -> JsonUtils.deserializeMap(
                            json,
                            PRIORITY_OVERRIDES,
                            e -> new ResourceLocation(e.getKey()),
                            e -> e.getValue().getAsString()),
                    new HashMap<>());

            Map< ResourceLocation, Set< ResourceLocation>> tagOwnerships = safeGet(
                    () -> JsonUtils.deserializeMapSet(
                            json,
                            TAG_OWNERSHIPS,
                            e -> new ResourceLocation(e.getKey()),
                            ResourceLocation::new),
                    new HashMap<>());

            Map< ResourceLocation, Set< ResourceLocation>> customTags = safeGet(
                    () -> JsonUtils.deserializeMapSet(
                            json,
                            CUSTOM_TAGS,
                            e -> new ResourceLocation(e.getKey()),
                            ResourceLocation::new),
                    new HashMap<>());

            return new FluidUnifyConfig(
                    modPriorities,
                    tags,
                    fluids,
                    priorityOverrides,
                    customTags,
                    tagOwnerships);
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
            JsonObject customTags = new JsonObject();
            config.customTags.forEach((parent, child) ->
            {
                customTags.add(parent.toString(),
                        JsonUtils.toArray(child.stream().map(ResourceLocation::toString).toList()));
            });
            json.add(CUSTOM_TAGS, customTags);
            JsonObject tagOwnerships = new JsonObject();
            config.tagOwnerships.forEach((parent, child) ->
            {
                tagOwnerships.add(parent.toString(),
                        JsonUtils.toArray(child.stream().map(ResourceLocation::toString).toList()));
            });
            json.add(TAG_OWNERSHIPS, tagOwnerships);

            return json;
        }
    }
}
