package com.shugabrush.raintegration.mixin.industrialforegoing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.ModList;

import com.almostreliable.unified.AlmostUnified;
import com.buuz135.industrial.plugin.jei.JEICustomPlugin;
import com.buuz135.industrial.plugin.jei.machineproduce.MachineProduceWrapper;
import com.shugabrush.raintegration.unification.ItemUnification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(value = JEICustomPlugin.class, remap = false)
public class UnifyIndustrialForegoingJEI {

    @ModifyArg(method = "registerRecipes",
               at = @At(value = "INVOKE",
                        target = "Lmezz/jei/api/registration/IRecipeRegistration;addRecipes(Lmezz/jei/api/recipe/RecipeType;Ljava/util/List;)V"),
               index = 1)
    <T> List<T> getWrappers(List<T> wrappers) {
        if (!ModList.get().isLoaded("almostunified")) return wrappers;

        for (int i = 0; i < wrappers.size(); ++i) {
            try {
                MachineProduceWrapper wrapper = (MachineProduceWrapper)wrappers.get(i);
                Ingredient outputItems = wrapper.getOutputItem();
                if (outputItems != null) {
                    ItemStack[] items = outputItems.getItems();
                    for (int j = 0; j < items.length; j++) {
                        ItemStack item = items[j];
                        ResourceLocation rubberLocation = new ResourceLocation("industrialforegoing:dryrubber");
                        if (item.is(ItemUnification.getItem(rubberLocation))) {
                            item = new ItemStack(ItemUnification.getItem(AlmostUnified.getRuntime().getReplacementMap()
                                    .get().getReplacementForItem(rubberLocation)));
                        }
                        if (item != null) {
                            wrapper.getOutputItem().getItems()[j] = item;
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        return wrappers;
    }
}
