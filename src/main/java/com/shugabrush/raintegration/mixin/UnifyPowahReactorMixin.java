package com.shugabrush.raintegration.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.shugabrush.raintegration.ConfigHolder;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.registry.fuel.FuelRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import owmii.powah.api.PowahAPI;
import owmii.powah.block.reactor.ReactorTile;
import owmii.powah.item.Itms;
import owmii.powah.lib.logistics.energy.Energy;

@Mixin(value = ReactorTile.class, remap = false)
public class UnifyPowahReactorMixin {

    /**
     * @author Shugabrush
     * @reason Allow any uraninite dust to be inserted into the fuel slot
     */
    @Overwrite
    public boolean canInsert(int slot, ItemStack stack) {
        if (slot == 1) {
            return stack.getTags().toList().contains(ConfigHolder.instance.machineConfigs.getPowahReactorFuel());
        } else if (slot == 2) {
            return FuelRegistry.get(stack) > 0 && !ItemStackHooks.hasCraftingRemainingItem(stack);
        } else if (slot == 3) {
            return stack.getItem() == Items.REDSTONE || stack.getItem() == Items.REDSTONE_BLOCK;
        } else if (slot == 4) {
            Pair<Integer, Integer> coolant = PowahAPI.getSolidCoolant(stack.getItem());
            return coolant.getLeft() > 0 && coolant.getRight() < 2;
        } else
            return Energy.chargeable(stack);
    }

    // If the item has the configured uraninite tag, return Powah's uraninite
    // so it works with the processFuel() method
    @Redirect(method = "processFuel",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private Item unifiedUraninite(ItemStack originalValue) {
        if (originalValue.getTags().toList().contains(ConfigHolder.instance.machineConfigs.getPowahReactorFuel())) {
            return Itms.URANINITE.get();
        }
        return originalValue.getItem();
    }
}
