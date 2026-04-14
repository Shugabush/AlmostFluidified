package com.shugabrush.raintegration.mixin.almostunified;

import com.almostreliable.unified.recipe.RecipeContextImpl;
import com.google.gson.JsonElement;
import com.shugabrush.raintegration.MoreUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RecipeContextImpl.class, remap = false)
public class RecipeContextImplMixin
{
    @ModifyArg(method = "createIngredientReplacement(Lcom/google/gson/JsonElement;[Ljava/lang/String;)Lcom/google/gson/JsonElement;",
            at = @At(value = "INVOKE", target = "Lcom/almostreliable/unified/recipe/RecipeContextImpl;tryCreateIngredientReplacement(Lcom/google/gson/JsonElement;[Ljava/lang/String;)V"), index = 1)
    String[] getIngredientLookupKeys(String[] lookupKeys)
    {
        String[] newLookupKeys = new String[lookupKeys.length + 1];
        for (int i = 0; i < lookupKeys.length; i++)
        {
            newLookupKeys[i] = lookupKeys[i];
        }
        // Add fluid to the lookup keys
        newLookupKeys[lookupKeys.length] = "fluid";
        return newLookupKeys;
    }

    @ModifyArg(method = "createResultReplacement(Lcom/google/gson/JsonElement;Z[Ljava/lang/String;)Lcom/google/gson/JsonElement;",
            at = @At(value = "INVOKE", target = "Lcom/almostreliable/unified/recipe/RecipeContextImpl;tryCreateResultReplacement(Lcom/google/gson/JsonElement;Z[Ljava/lang/String;)Lcom/google/gson/JsonElement;"), index = 2)
    String[] getResultLookupKeys(String[] lookupKeys)
    {
        String[] newLookupKeys = new String[lookupKeys.length + 1];
        for (int i = 0; i < lookupKeys.length; i++)
        {
            newLookupKeys[i] = lookupKeys[i];
        }
        // Add fluid to the lookup keys
        newLookupKeys[lookupKeys.length] = "fluid";
        return newLookupKeys;
    }

    @ModifyVariable(method = "tryCreateIngredientReplacement", at = @At("HEAD"))
    JsonElement tryCreateFluidIngredientReplacement(JsonElement element)
    {
        return MoreUnification.tryCreateContentReplacement(element);
    }

    @ModifyVariable(method = "tryCreateResultReplacement", at = @At("HEAD"))
    JsonElement tryCreateFluidResultReplacement(JsonElement element)
    {
        return MoreUnification.tryCreateContentReplacement(element);
    }

}
