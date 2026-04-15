package com.shugabrush.raintegration.mixin.almostunified;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import com.almostreliable.unified.recipe.RecipeContextImpl;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.MoreUnification;
import com.shugabrush.raintegration.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = RecipeContextImpl.class, remap = false)
public class RecipeContextImplMixin
{
    @Inject(method = "tryCreateIngredientReplacement", at = @At("HEAD"))
    private void tryCreateFluidIngredientReplacement(JsonElement element, String[] lookupKeys, CallbackInfo ci)
    {
        if (element instanceof JsonObject object && element.toString().contains("\"fluid\":"))
        {
            JsonElement fluid = object.get("fluid");
            if (fluid instanceof JsonPrimitive primitive)
            {
                ResourceLocation fluidLocation = ResourceLocation.tryParse(primitive.getAsString());
                ResourceLocation unifiedFluid = BuiltInRegistries.FLUID
                        .getKey(MoreUnification.getReplacementForFluid(fluidLocation));
                if (!unifiedFluid.toString().equals(fluidLocation.toString()))
                {
                    object.remove("fluid");
                    object.addProperty("fluid", unifiedFluid.toString());
                }
            }
        }
    }

    // After the method has returned, modify the return value to account for fluid unification
    @Inject(method = "tryCreateResultReplacement", at = @At("RETURN"), cancellable = true)
    private void tryCreateFluidResultReplacement(JsonElement element, boolean tagLookup, String[] lookupKeys,
                                                 CallbackInfoReturnable< JsonElement> cir)
    {
        if (element.toString().contains("fluid"))
        {
            // Make sure element is modified according to the original return value
            if (cir.getReturnValue() != null)
            {
                element = cir.getReturnValue();
            }
            if (element instanceof JsonObject object)
            {
                JsonElement value = object.get("value");
                if (value instanceof JsonArray valueObject)
                {
                    JsonElement finalElement = element;
                    valueObject.forEach(valueElement ->
                    {
                        if (valueElement instanceof JsonObject valueElementObject)
                        {
                            JsonElement fluid = valueElementObject.get("fluid");
                            if (fluid instanceof JsonPrimitive primitive)
                            {
                                ResourceLocation oldFluid = new ResourceLocation(primitive.getAsString());
                                ResourceLocation newFluid = BuiltInRegistries.FLUID.getKey(MoreUnification
                                        .getReplacementForFluid(oldFluid));
                                if (newFluid.toString() != oldFluid.toString())
                                {
                                    valueElementObject.remove("fluid");
                                    valueElementObject.addProperty("fluid", newFluid.toString());
                                    cir.setReturnValue(finalElement);
                                }
                            }

                            if (tagLookup)
                            {
                                JsonElement tag = valueElementObject.get("tag");
                                if (tag instanceof JsonPrimitive primitive)
                                {
                                    ResourceLocation fluidLocation = MoreUnification.getPreferredFluidForTag(
                                            Utils.toFluidTag(primitive.getAsString()), ($) -> true);
                                    if (fluidLocation != null)
                                    {
                                        valueElementObject.remove("tag");
                                        valueElementObject.addProperty("fluid", fluidLocation.toString());
                                        cir.setReturnValue(finalElement);
                                    }
                                }
                            }
                        }
                    });
                }
            }

        }
    }

    @Shadow
    private void tryCreateIngredientReplacement(@Nullable JsonElement element, String... lookupKeys)
    {}

    @Shadow
    private UnifyTag< Item> getPreferredTagForItem(ResourceLocation item)
    {
        return null;
    }
}
