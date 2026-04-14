package com.shugabrush.raintegration.mixin.almostunified.compat;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.compat.GregTechModernRecipeUnifier;
import com.google.gson.JsonElement;
import com.shugabrush.raintegration.MoreUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Function;

@Mixin(value = GregTechModernRecipeUnifier.class, remap = false)
public class GregTechModernRecipeUnifierMixin
{
    boolean fluidChanged;

    @ModifyVariable(method = "createContentReplacement", at = @At(value = "HEAD"), index = 1)
    private JsonElement createFluidContentReplacement(@Nullable JsonElement json)
    {
        fluidChanged = false;
        if (json != null && json.toString().contains("fluid"))
        {
            fluidChanged = true;
            return MoreUnification.tryCreateContentReplacement(json);
        }
        return json;
    }

    @Inject(method = "createContentReplacement", at = @At(value = "TAIL"), cancellable = false)
    private void replace(JsonElement json, RecipeContext ctx, Function<JsonElement, JsonElement> elementTransformer, CallbackInfoReturnable<JsonElement> cir)
    {
        if (fluidChanged)
        {
            cir.setReturnValue(json);
        }
    }
}
