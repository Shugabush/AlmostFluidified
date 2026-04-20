package com.shugabrush.raintegration.unification;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.unification.utils.FluidUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class FluidRecipeContext
{

    private final FluidReplacementMap replacementMap;
    private final JsonObject originalRecipe;

    @Nullable
    public FluidRecipeContext(JsonObject json, FluidReplacementMap replacementMap)
    {
        this.originalRecipe = json;
        this.replacementMap = replacementMap;
    }

    @Nullable
    public ResourceLocation getReplacementForFluid(@Nullable ResourceLocation fluid)
    {
        if (fluid == null)
            return null;

        return replacementMap.getReplacementForFluid(fluid);
    }

    @Nullable
    public ResourceLocation getPreferredFluidForTag(@Nullable UnifyTag< Fluid> tag, Predicate< ResourceLocation> filter)
    {
        if (tag == null)
            return null;

        return replacementMap.getPreferredFluidForTag(tag, filter);
    }

    @Nullable
    public UnifyTag< Fluid> getPreferredTagForFluid(@Nullable ResourceLocation fluid)
    {
        if (fluid == null)
            return null;

        return replacementMap.getPreferredTagForFluid(fluid);
    }

    @Nullable
    public JsonElement createIngredientReplacement(@Nullable JsonElement element)
    {
        return createIngredientReplacement(
                element,
                "value",
                "ingredient");
    }

    public JsonElement createIngredientReplacement(@Nullable JsonElement element, Function<JsonElement, JsonElement> primitiveMethod, String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = primitiveMethod.apply(element.deepCopy());
        return element.equals(copy) ? null : copy;
    }

    @Nullable
    public JsonElement createIngredientReplacement(@Nullable JsonElement element, String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        tryCreateIngredientReplacement(copy, lookupKeys);
        return element.equals(copy) ? null : copy;
    }

    private void tryCreateIngredientReplacement(@Nullable JsonElement element, String... lookupKeys)
    {
        if (element instanceof JsonArray array)
        {
            for (JsonElement e : array)
            {
                tryCreateIngredientReplacement(e, lookupKeys);
            }
        }

        if (element instanceof JsonObject object)
        {
            for (String key : lookupKeys)
            {
                tryCreateIngredientReplacement(object.get(key), lookupKeys);
            }

            if (object.get("tag") instanceof JsonPrimitive primitive)
            {
                UnifyTag< Fluid> tag = FluidUtils.toFluidTag(primitive.getAsString());
                var ownerTag = replacementMap.getTagOwnerships().getOwnerByTag(tag);
                if (ownerTag != null)
                {
                    object.addProperty("tag", ownerTag.location().toString());
                }
            }

            if (object.get("fluid") instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluid = ResourceLocation.tryParse(primitive.getAsString());
                UnifyTag< Fluid> tag = getPreferredTagForFluid(fluid);
                if (tag != null)
                {
                    object.remove("fluid");
                    object.addProperty("tag", tag.location().toString());
                }
            }
        }
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element)
    {
        return createResultReplacement(element, true, "fluid");
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup, String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        JsonElement result = tryCreateResultReplacement(copy, tagLookup, lookupKeys);
        return element.equals(result) ? null : result;
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup, Function<JsonElement, JsonElement> primitiveMethod, String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        JsonElement result = tryCreateResultReplacement(copy, tagLookup, primitiveMethod, lookupKeys);
        return element.equals(result) ? null : result;
    }

    @Nullable
    private JsonElement tryCreateResultReplacement(JsonElement element, boolean tagLookup, String... lookupKeys)
    {
        // Default primitive method
        Function<JsonElement, JsonElement> primitiveMethod = (primitive) ->
        {
            ResourceLocation fluid = ResourceLocation.tryParse(primitive.getAsString());
            ResourceLocation replacement = getReplacementForFluid(fluid);
            if (replacement != null)
            {
                return new JsonPrimitive(replacement.toString());
            }
            return null;
        };
        return tryCreateResultReplacement(element, tagLookup, primitiveMethod, lookupKeys);
    }

    @Nullable
    private JsonElement tryCreateResultReplacement(JsonElement element, boolean tagLookup, Function<JsonElement, JsonElement> primitiveMethod, String... lookupKeys)
    {
        if (element instanceof JsonPrimitive primitive)
        {
            return primitiveMethod.apply(primitive);
        }

        if (element instanceof JsonArray array &&
                JsonUtils.replaceOn(array, j -> tryCreateResultReplacement(j, tagLookup, primitiveMethod, lookupKeys)))
        {
            return element;
        }

        if (element instanceof JsonObject object)
        {
            for (String key : lookupKeys)
            {
                if (JsonUtils.replaceOn(object, key, j -> tryCreateResultReplacement(j, tagLookup, primitiveMethod, lookupKeys)))
                {
                    return element;
                }
            }

            // when tags are used as outputs, replace them with the preferred fluid
            if (tagLookup && object.get("tag") instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluid = getPreferredFluidForTag(FluidUtils.toFluidTag(primitive.getAsString()),
                        $ -> true);
                if (fluid != null)
                {
                    object.remove("tag");
                    object.addProperty("fluid", fluid.toString());
                }
                return element;
            }
        }

        return null;
    }

    public ResourceLocation getType()
    {
        String type = originalRecipe.get("type").getAsString();
        return new ResourceLocation(type);
    }

    public boolean hasProperty(String property)
    {
        return originalRecipe.has(property);
    }
}
