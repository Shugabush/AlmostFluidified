package com.shugabrush.raintegration.mixin.almostunified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.almostreliable.unified.compat.GregTechModernRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@Mixin(value = GregTechModernRecipeUnifier.class, remap = false)
public class GregTechModernRecipeUnifierMixin
{
    @Nullable
    @Overwrite(remap = false)
    private JsonElement createContentReplacement(@Nullable JsonElement json, RecipeContext ctx, Function<JsonElement, JsonElement> elementTransformer) {
        if (json instanceof JsonObject jsonObject) {
            boolean changed = false;
            if (jsonObject.get(RecipeConstants.ITEM) instanceof JsonArray jsonArray)
            {
                JsonArray result = new JsonArray();

                for (JsonElement element : jsonArray) {
                    if (element instanceof JsonObject elementObject) {
                        JsonElement replacement = elementTransformer.apply(elementObject.get("content"));
                        if (replacement != null) {
                            elementObject.add("content", replacement);
                            changed = true;
                        }
                        result.add(elementObject);
                    }
                }

                if (changed) {
                    jsonObject.add(RecipeConstants.ITEM, result);
                }
            }
            if (jsonObject.get("fluid") instanceof JsonArray jsonArray)
            {
                JsonArray result = new JsonArray();

                for (JsonElement element : jsonArray) {
                    if (element instanceof JsonObject elementObject) {
                        JsonElement replacement = elementTransformer.apply(elementObject.get("content"));
                        if (replacement != null) {
                            elementObject.add("content", replacement);
                            changed = true;
                        }
                        result.add(elementObject);
                    }
                }

                if (changed) {
                    jsonObject.add("fluid", result);
                }
            }
            if (changed)
            {
                return jsonObject;
            }
        }

        return null;
    }

    @Overwrite(remap = false)
    public void collectUnifier(RecipeUnifierBuilder builder) {
        List.of(
                RecipeConstants.INPUTS,
                RecipeConstants.TICK_INPUTS
        ).forEach(key ->
                builder.put(key, (json, ctx) -> createContentReplacement(json, ctx, ctx::createIngredientReplacement))
        );

        List.of(
                RecipeConstants.OUTPUTS,
                RecipeConstants.TICK_OUTPUTS
        ).forEach(key ->
                builder.put(
                        key,
                        (json, ctx) -> createContentReplacement(
                                json,
                                ctx,
                                element -> ctx.createResultReplacement(
                                        element,
                                        true,
                                        RecipeConstants.ITEM,
                                        RecipeConstants.INGREDIENT
                                )
                        )
                )
        );
    }
}
