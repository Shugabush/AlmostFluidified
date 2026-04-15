package com.shugabrush.raintegration.unification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import com.almostreliable.unified.AlmostUnified;

public class ItemUnification
{

    public static Item getItem(ResourceLocation resourceLocation)
    {
        if (ModList.get().isLoaded("almostunified"))
        {
            ResourceLocation unifiedResourceLocation = AlmostUnified.getRuntime().getReplacementMap().get()
                    .getReplacementForItem(resourceLocation);
            if (unifiedResourceLocation != null)
            {
                return BuiltInRegistries.ITEM.get(unifiedResourceLocation);
            }
        }
        return BuiltInRegistries.ITEM.get(resourceLocation);
    }

    public static Item getItem(String resourceLocation)
    {
        return getItem(new ResourceLocation(resourceLocation));
    }

    public static Item getItem(Item originalItem)
    {
        return getItem(getItemLocation(originalItem));
    }

    public static Item getItem(ItemStack originalItem)
    {
        return getItem(originalItem.getItem());
    }

    public static ResourceLocation getItemLocation(Item item)
    {
        return item.builtInRegistryHolder().unwrapKey().get().location();
    }

    public static ResourceLocation getItemLocation(ItemStack item)
    {
        return item.getItemHolder().unwrapKey().get().location();
    }
}
