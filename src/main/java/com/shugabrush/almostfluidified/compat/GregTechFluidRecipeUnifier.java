package com.shugabrush.almostfluidified.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifier;
import com.shugabrush.almostfluidified.api.FluidRecipeUnifierBuilder;
import com.shugabrush.almostfluidified.unification.FluidRecipeContext;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

public class GregTechFluidRecipeUnifier implements FluidRecipeUnifier
{

    @Override
    public void collectUnifier(FluidRecipeUnifierBuilder builder)
    {
        List.of(
                "inputs")
                .forEach(key -> builder.put(key,
                        (json, ctx) -> createContentReplacement(json, ctx, ctx::createIngredientReplacement)));
        List.of(
                "outputs").forEach(
                        key -> builder.put(
                                key,
                                (json, ctx) -> createContentReplacement(
                                        json,
                                        ctx,
                                        element -> ctx.createResultReplacement(
                                                element,
                                                true,
                                                "fluid",
                                                "value"))));
    }

    @Nullable
    private JsonElement createContentReplacement(@Nullable JsonElement json, FluidRecipeContext ctx,
                                                 Function< JsonElement, JsonElement> elementTransformer)
    {
        if (json instanceof JsonObject jsonObject &&
                jsonObject.get("fluid") instanceof JsonArray jsonArray)
        {
            JsonArray result = new JsonArray();
            boolean changed = false;

            for (JsonElement element : jsonArray)
            {
                if (element instanceof JsonObject elementObject)
                {
                    JsonElement replacement = elementTransformer.apply(elementObject.get("content"));
                    if (replacement != null)
                    {
                        elementObject.add("content", replacement);
                        changed = true;
                    }
                    result.add(elementObject);
                }
            }

            if (changed)
            {
                jsonObject.add("fluid", result);
                return jsonObject;
            }
        }

        return null;
    }
}
