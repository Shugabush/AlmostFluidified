package com.shugabrush.raintegration.mixin.almostunified;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.recipe.RecipeContextImpl;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.MoreUnification;
import com.shugabrush.raintegration.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = RecipeContextImpl.class, remap = false)
public class RecipeContextImplMixin
{
    @Shadow
    private ReplacementMap replacementMap;

    @Overwrite
    @Nullable
    public JsonElement createIngredientReplacement(@Nullable JsonElement element) {
        return this.createIngredientReplacement(element, "value", "base", "ingredient", "fluid", "tag");
    }

    @Shadow
    public JsonElement createIngredientReplacement(@Nullable JsonElement element, String... lookupKeys)
    {
        return null;
    }

    @Overwrite
    @Nullable
    public JsonElement createResultReplacement(@Nullable JsonElement element) {
        return createResultReplacement(element, true, RecipeConstants.ITEM, "fluid", "tag");
    }

    @Shadow
    public JsonElement createResultReplacement(@Nullable JsonElement element, boolean tagLookup, String... lookupKeys)
    {
        return null;
    }

    @Inject(method = "tryCreateIngredientReplacement", at = @At("HEAD"))
    private void tryCreateFluidIngredientReplacement(JsonElement element, String[] lookupKeys, CallbackInfo ci) {
        if (element instanceof JsonObject object && element.toString().contains("\"fluid\":")) {
            JsonElement fluid = object.get("fluid");
            if (fluid instanceof JsonPrimitive primitive) {
                ResourceLocation fluidLocation = ResourceLocation.tryParse(primitive.getAsString());
                UnifyTag<Fluid> tag = MoreUnification.getPreferredTagForFluid(fluidLocation);
                if (tag != null) {
                    object.remove("fluid");
                    object.addProperty("tag", tag.location().toString());
                }
            }
        }
    }
    @Inject(method = "tryCreateResultReplacement", at = @At("RETURN"), cancellable = true)
    private void tryCreateFluidResultReplacement(JsonElement element, boolean tagLookup, String[] lookupKeys, CallbackInfoReturnable<JsonElement> cir)
    {
        if (element.toString().contains("\"fluid\":"))
        {
            if (element instanceof JsonObject object)
            {
                JsonElement value = object.get("value");
                if (value instanceof JsonArray valueObject)
                {
                    valueObject.forEach(valueElement ->
                    {
                        if (valueElement instanceof JsonObject valueElementObject)
                        {
                            JsonElement fluid = valueElementObject.get("fluid");
                            if (fluid instanceof JsonPrimitive primitive)
                            {
                                ResourceLocation newFluid = BuiltInRegistries.FLUID.getKey(MoreUnification.getReplacementForFluid(new ResourceLocation(primitive.getAsString())));
                                if (newFluid != null)
                                {
                                    valueElementObject.remove("fluid");
                                    valueElementObject.addProperty("fluid", newFluid.toString());
                                    cir.setReturnValue(element);
                                }
                            }

                            if (tagLookup)
                            {
                                JsonElement tag = valueElementObject.get("tag");
                                if (tag instanceof JsonPrimitive primitive)
                                {
                                    ResourceLocation fluidLocation = MoreUnification.getPreferredFluidForTag(Utils.toFluidTag(primitive.getAsString()), ($) -> true);
                                    if (fluidLocation != null)
                                    {
                                        valueElementObject.remove("tag");
                                        valueElementObject.addProperty("fluid", fluidLocation.toString());
                                        cir.setReturnValue(element);
                                    }
                                }
                            }
                        }
                    });
                }
            }

        }
    }

    @Shadow private void tryCreateIngredientReplacement(@Nullable JsonElement element, String... lookupKeys) {}

    @Shadow
    private UnifyTag<Item> getPreferredTagForItem(ResourceLocation item)
    {
        return null;
    }

}
