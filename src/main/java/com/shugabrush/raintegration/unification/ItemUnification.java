package com.shugabrush.raintegration.unification;

import com.almostreliable.unified.utils.TagMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.unified.AlmostUnified;

public class ItemUnification
{
    public static Item getItem(ResourceLocation resourceLocation)
    {
        ResourceLocation unifiedResourceLocation = AlmostUnified.getRuntime().getReplacementMap().get().getReplacementForItem(resourceLocation);
        if (unifiedResourceLocation != null)
        {
            return BuiltInRegistries.ITEM.get(unifiedResourceLocation);
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
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
