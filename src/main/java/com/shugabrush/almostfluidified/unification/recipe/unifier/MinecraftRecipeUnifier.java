package com.shugabrush.almostfluidified.unification.recipe.unifier;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifier;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifierBuilder;
import com.shugabrush.almostfluidified.unification.FluidRecipeContext;

import java.util.Map;
import java.util.Set;

public class MinecraftRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        builder.put("key", this::createReplacement);
        builder.put("result", this::createReplacement);
    }

    private JsonElement createReplacement(JsonElement json, FluidRecipeContext context)
    {
        if (!json.toString().contains("bucket"))
            return null;

        JsonElement copyJson = json.deepCopy();

        boolean changed = false;

        if (copyJson instanceof JsonArray array)
        {
            for (JsonElement arrayElement : array)
            {
                JsonElement unifiedArrayElement = createReplacement(arrayElement, context);
                if (arrayElement != unifiedArrayElement)
                {
                    return unifiedArrayElement;
                }
            }
        }
        else if (copyJson instanceof JsonObject object)
        {
            Set< Map.Entry< String, JsonElement>> properties = object.entrySet();
            for (Map.Entry< String, JsonElement> entry : properties)
            {
                String entryKey = entry.getKey();
                JsonElement entryElement = entry.getValue();
                if (entryElement instanceof JsonPrimitive primitive)
                {
                    // Replace bucket if applicable
                    ResourceLocation bucket = context
                            .getReplacementForBucket(ResourceLocation.tryParse(primitive.getAsString()));
                    if (bucket != null)
                    {
                        changed = true;
                        object.addProperty(entryKey, bucket.toString());
                    }
                }
                else
                {
                    JsonElement unifiedEntryElement = createReplacement(entryElement, context);
                    if (unifiedEntryElement != null && unifiedEntryElement != entryElement)
                    {
                        changed = true;
                        object.add(entryKey, unifiedEntryElement);
                    }
                }
            }
        }

        if (changed)
            return copyJson;

        return null;
    }
}
