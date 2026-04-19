package com.shugabrush.raintegration.unification.recipeunifiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shugabrush.raintegration.unification.FluidRecipeContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class FluidRecipeUnifierBuilder
{
    private final Map<String, Entry<?>> consumers = new HashMap<>();

    public void forEachObject(String property, BiFunction<JsonObject, FluidRecipeContext, JsonObject> consumer)
    {
        BiFunction<JsonArray, FluidRecipeContext, JsonArray> arrayConsumer = (array, ctx) ->
        {
            for (int i = 0; i < array.size(); i++)
            {
                JsonElement element = array.get(i);
                if (element instanceof JsonObject obj)
                {
                    JsonObject result = consumer.apply(obj, ctx);
                    if (result != null)
                    {
                        array.set(i, result);
                    }
                }
            }
            return array;
        };

        put(property, JsonArray.class, arrayConsumer);
    }

    public void put(String property, BiFunction<JsonElement, FluidRecipeContext, JsonElement> consumer)
    {
        consumers.put(property, new Entry<>(JsonElement.class, consumer));
    }

    public <T extends JsonElement> void put(String property, Class<T> type, BiFunction<T, FluidRecipeContext, T> consumer)
    {
        consumers.put(property, new Entry<>(type, consumer));
    }

    public JsonObject unify(JsonObject json, FluidRecipeContext context)
    {
        JsonObject changedValues = new JsonObject();

        for (var e : json.entrySet())
        {
            Entry<?> consumer = consumers.get(e.getKey());
            if (consumer != null)
            {
                JsonElement currentElement = e.getValue();
                JsonElement transformedElement = consumer.apply(currentElement.deepCopy(), context);
                if (transformedElement != null && !transformedElement.equals(currentElement))
                {
                    changedValues.add(e.getKey(), transformedElement);
                }
            }
        }

        if (changedValues.size() == 0)
        {
            return null;
        }

        // helps to preserve the order of the elements
        JsonObject result = new JsonObject();
        for (var entry : json.entrySet())
        {
            JsonElement changedValue = changedValues.get(entry.getKey());
            if (changedValue != null)
            {
                result.add(entry.getKey(), changedValue);
            }
            else
            {
                result.add(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    public Collection<String> getKeys()
    {
        return consumers.keySet();
    }

    private record Entry<T extends JsonElement>(Class<T> expectedType, BiFunction<T, FluidRecipeContext, T> func)
    {
        @Nullable
        T apply(JsonElement json, FluidRecipeContext context)
        {
            if (expectedType.isInstance(json))
            {
                return func.apply(expectedType.cast(json), context);
            }

            return null;
        }
    }
}
