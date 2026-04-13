package com.shugabrush.raintegration.mixin.almostunified;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.recipe.RecipeContextImpl;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.MoreUnification;
import com.shugabrush.raintegration.Utils;
import com.shugabrush.raintegration.unification.FluidUnifyTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeContextImpl.class, remap = false)
public class RecipeUnifierBuilderImplMixin
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

    @Inject(method = "tryCreateIngredientReplacement", at = @At("TAIL"))
    void tryCreateFluidIngredientReplacement(JsonElement element, String[] lookupKeys, CallbackInfo ci)
    {
        if (element instanceof JsonObject object)
        {
            var fluidObj = object.getAsJsonObject("fluid");
            if (fluidObj != null)
            {
                var valueTag = fluidObj.getAsJsonArray("value");
                if (valueTag != null)
                {
                    var tagObj = valueTag.get(0).getAsJsonObject().get("tag");
                    if (tagObj != null)
                    {
                        UnifyTag<Fluid> fluidTag = FluidUnifyTag.fluid(ResourceLocation.tryParse(tagObj.getAsString()));
                        ResourceLocation fluid = MoreUnification.getPreferredFluidForTag(fluidTag, (r) -> true);
                        if (fluid != null)
                        {
                            object.remove("tag");
                            object.addProperty("fluid", fluid.toString());
                            return;
                        }
                    }
                }
                ResourceLocation fluid = ResourceLocation.tryParse(fluidObj.getAsString());
                UnifyTag<Fluid> tag = MoreUnification.getPreferredTagForFluid(fluid);
                if (tag != null)
                {
                    object.remove("fluid");
                    object.addProperty(RecipeConstants.TAG, tag.location().toString());
                }
            }
        }
    }

    @Inject(method = "tryCreateResultReplacement", at = @At("TAIL"))
    void tryCreateFluidResultReplacement(JsonElement element, boolean tagLookup, String[] lookupKeys, CallbackInfoReturnable<JsonElement> cir)
    {
        if (element instanceof JsonObject object)
        {
            // When tags are used as outputs, replace them with the preferred fluid
            if (tagLookup && object.get(RecipeConstants.TAG) instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluid = MoreUnification.getPreferredFluidForTag(Utils.toFluidTag(primitive.getAsString()), $ -> true);
                if (fluid != null)
                {
                    object.remove(RecipeConstants.TAG);
                    object.addProperty("fluid", fluid.toString());
                    cir.setReturnValue(element);
                }
            }
        }
    }

}
