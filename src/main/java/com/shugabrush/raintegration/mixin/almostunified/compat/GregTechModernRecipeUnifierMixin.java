package com.shugabrush.raintegration.mixin.almostunified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.compat.GregTechModernRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(value = GregTechModernRecipeUnifier.class, remap = false)
public class GregTechModernRecipeUnifierMixin
{

    /**
     * @author Shugabrush
     * @reason Implement fluid unification
     */
    @Nullable
    @Overwrite(remap = false)
    private JsonElement createContentReplacement(@Nullable JsonElement json, RecipeContext ctx,
                                                 Function< JsonElement, JsonElement> elementTransformer)
    {
        if (json instanceof JsonObject jsonObject)
        {
            boolean changed = false;
            if (jsonObject.get(RecipeConstants.ITEM) instanceof JsonArray jsonArray)
            {
                JsonArray result = new JsonArray();

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
                    jsonObject.add(RecipeConstants.ITEM, result);
                }
            }
            if (jsonObject.get("fluid") instanceof JsonArray jsonArray)
            {
                JsonArray result = new JsonArray();

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
                }
            }
            if (changed)
            {
                return jsonObject;
            }
        }

        return null;
    }
}
