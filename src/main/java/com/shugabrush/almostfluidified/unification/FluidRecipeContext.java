package com.shugabrush.almostfluidified.unification;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.almostfluidified.unification.utils.FluidUtils;

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
    public ResourceLocation getReplacementForBucket(@Nullable ResourceLocation bucket)
    {
        if (bucket == null)
            return null;

        return replacementMap.getReplacementForBucket(bucket);
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
                true,
                "value",
                "ingredient");
    }

    @Nullable
    public JsonElement createIngredientReplacement(@Nullable JsonElement element, boolean tagLookup)
    {
        return createIngredientReplacement(
                element,
                tagLookup,
                "value",
                "ingredient");
    }

    public JsonElement createIngredientReplacement(@Nullable JsonElement element,
                                                   Function< JsonElement, JsonElement> primitiveMethod)
    {
        if (element == null)
            return null;

        JsonElement copy = primitiveMethod.apply(element.deepCopy());
        return element.equals(copy) ? null : copy;
    }

    @Nullable
    public JsonElement createIngredientReplacement(@Nullable JsonElement element, boolean tagLookup,
                                                   String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        tryCreateIngredientReplacement(copy, tagLookup, "fluid", "tag", lookupKeys);
        return element.equals(copy) ? null : copy;
    }

    public JsonElement createIngredientReplacement(@Nullable JsonElement element, String fluidKey, String tagKey,
                                                   boolean tagLookup, String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        tryCreateIngredientReplacement(copy, tagLookup, fluidKey, tagKey, lookupKeys);
        return element.equals(copy) ? null : copy;
    }

    private void tryCreateIngredientReplacement(@Nullable JsonElement element, boolean tagLookup, String fluidKey,
                                                String tagKey, String... lookupKeys)
    {
        if (element == null)
            return;
        if (element instanceof JsonArray array)
        {
            for (JsonElement e : array)
            {
                tryCreateIngredientReplacement(e, tagLookup, fluidKey, tagKey, lookupKeys);
            }
        }
        else if (element instanceof JsonObject object)
        {
            for (String key : lookupKeys)
            {
                tryCreateIngredientReplacement(object.get(key), tagLookup, fluidKey, tagKey, lookupKeys);
            }

            if (tagLookup && object.get(tagKey) instanceof JsonPrimitive primitive)
            {
                UnifyTag< Fluid> tag = FluidUtils.toFluidTag(primitive.getAsString());
                UnifyTag< Fluid> ownerTag = replacementMap.getTagOwnerships().getOwnerByTag(tag);
                if (ownerTag != null)
                {
                    object.addProperty(tagKey, ownerTag.location().toString());
                }
            }

            if (object.get(fluidKey) instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluid = ResourceLocation.tryParse(primitive.getAsString());
                UnifyTag< Fluid> tag = getPreferredTagForFluid(fluid);
                if (tag != null)
                {
                    object.remove(fluidKey);
                    object.addProperty(tagKey, tag.location().toString());
                }
            }
            else
            {
                tryCreateBucketReplacement(object);
            }
        }
    }

    public void tryCreateBucketReplacement(JsonObject object)
    {
        if (object.get(RecipeConstants.ITEM) instanceof JsonPrimitive primitive)
        {
            // Replace bucket if applicable
            ResourceLocation bucket = getReplacementForBucket(ResourceLocation.tryParse(primitive.getAsString()));
            if (bucket != null)
            {
                object.addProperty(RecipeConstants.ITEM, bucket.toString());
            }
        }
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element)
    {
        return createResultReplacement(element, true, "fluid");
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup)
    {
        return createResultReplacement(element, tagLookup, "fluid");
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, String fluidKey, String tagKey,
                                               boolean tagLookup)
    {
        return createResultReplacement(element, tagLookup, fluidKey, tagKey, "fluid");
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup, String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        JsonElement result = tryCreateResultReplacement(copy, tagLookup, "fluid", "tag", lookupKeys);
        return element.equals(result) ? null : result;
    }

    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup,
                                               Function< JsonElement, JsonElement> primitiveMethod,
                                               String... lookupKeys)
    {
        if (element == null)
            return null;

        JsonElement copy = element.deepCopy();
        JsonElement result = tryCreateResultReplacement(copy, tagLookup, "fluid", "tag", primitiveMethod, lookupKeys);
        return element.equals(result) ? null : result;
    }

    @Nullable
    private JsonElement tryCreateResultReplacement(JsonElement element, boolean tagLookup, String fluidKey,
                                                   String tagKey, String... lookupKeys)
    {
        // Default primitive method
        Function< JsonElement, JsonElement> primitiveMethod = (primitive) ->
        {
            ResourceLocation fluid = ResourceLocation.tryParse(primitive.getAsString());
            ResourceLocation replacement = getReplacementForFluid(fluid);
            if (replacement != null)
            {
                return new JsonPrimitive(replacement.toString());
            }
            return null;
        };
        return tryCreateResultReplacement(element, tagLookup, fluidKey, tagKey, primitiveMethod, lookupKeys);
    }

    @Nullable
    private JsonElement tryCreateResultReplacement(JsonElement element, boolean tagLookup,
                                                   String fluidKey,
                                                   String tagKey,
                                                   Function< JsonElement, JsonElement> primitiveMethod,
                                                   String... lookupKeys)
    {
        if (element instanceof JsonPrimitive primitive)
        {
            return primitiveMethod.apply(primitive);
        }

        if (element instanceof JsonArray array &&
                JsonUtils.replaceOn(array,
                        j -> tryCreateResultReplacement(j, tagLookup, fluidKey, tagKey, primitiveMethod, lookupKeys)))
        {
            return element;
        }

        if (element instanceof JsonObject object)
        {
            for (String key : lookupKeys)
            {
                if (JsonUtils.replaceOn(object, key,
                        j -> tryCreateResultReplacement(j, tagLookup, fluidKey, tagKey, primitiveMethod, lookupKeys)))
                {
                    return element;
                }
            }

            // when tags are used as outputs, replace them with the preferred fluid
            if (tagLookup && object.get(tagKey) instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluid = getPreferredFluidForTag(FluidUtils.toFluidTag(primitive.getAsString()),
                        $ -> true);
                if (fluid != null)
                {
                    object.remove(tagKey);
                    object.addProperty(fluidKey, fluid.toString());
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
