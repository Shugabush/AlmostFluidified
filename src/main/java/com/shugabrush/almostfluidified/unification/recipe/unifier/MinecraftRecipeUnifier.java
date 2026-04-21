package com.shugabrush.almostfluidified.unification.recipe.unifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifier;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifierBuilder;

import java.util.Map;
import java.util.Set;

public class MinecraftRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        builder.put("key", (json, ctx) -> createReplacement(json));
        builder.put("result", (json, ctx) -> createReplacement(json));
    }

    private JsonElement createReplacement(JsonElement json)
    {
        String jsonStr = json.toString();
        if (!jsonStr.contains("bucket")) return null;

        if (json instanceof JsonObject object)
        {
            Set<Map.Entry<String, JsonElement>> properties = object.entrySet();
            for (Map.Entry<String, JsonElement> entry : properties)
            {
                JsonElement entryElement = entry.getValue();
                JsonElement unifiedEntryElement = createReplacement(entryElement);
                if (unifiedEntryElement != null && entryElement != unifiedEntryElement)
                {
                    return unifiedEntryElement;
                }
            }
        }

        return null;
    }
}
