package com.shugabrush.almostfluidified.mixin.powah;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.shugabrush.almostfluidified.unification.utils.ItemUnification;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.registry.fuel.FuelRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import owmii.powah.api.PowahAPI;
import owmii.powah.block.reactor.ReactorBlock;
import owmii.powah.block.reactor.ReactorTile;
import owmii.powah.lib.block.AbstractEnergyProvider;
import owmii.powah.lib.block.IInventoryHolder;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.util.Ticker;

@Mixin(value = ReactorTile.class, remap = false)
public class ReactorTileMixin extends AbstractEnergyProvider< ReactorBlock> implements IInventoryHolder
{

    @Shadow
    public final Ticker fuel = new Ticker(1000);

    @Shadow
    private int baseTemp;

    public ReactorTileMixin(BlockEntityType< ?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public int getSlotLimit(int i)
    {
        return 64;
    }

    private boolean isUnifiedUraninite(Item item)
    {
        return item == ItemUnification.getItem("powah:uraninite") ||
                item == ItemUnification.getItem("powah:uraninite_block");
    }

    private boolean isUnifiedUraninite(ItemStack stack)
    {
        return isUnifiedUraninite(stack.getItem());
    }

    // Allow items with the configured tag to be inserted into the fuel slot
    @Override
    public boolean canInsert(int slot, ItemStack stack)
    {
        if (slot == 1)
        {
            return isUnifiedUraninite(stack);
        }
        else if (slot == 2)
        {
            return FuelRegistry.get(stack) > 0 && !ItemStackHooks.hasCraftingRemainingItem(stack);
        }
        else if (slot == 3)
        {
            return stack.getItem() == Items.REDSTONE || stack.getItem() == Items.REDSTONE_BLOCK;
        }
        else if (slot == 4)
        {
            Pair< Integer, Integer> coolant = PowahAPI.getSolidCoolant(stack.getItem());
            return coolant.getLeft() > 0 && coolant.getRight() < 2;
        }
        else
        {
            return Energy.chargeable(stack);
        }
    }

    @Override
    public boolean canExtract(int i, ItemStack itemStack)
    {
        return true;
    }

    /**
     * @author Shugabrush
     * @reason Process the configured fuel item
     */
    @Overwrite
    private boolean processFuel(Level world)
    {
        boolean flag = false;
        if (this.fuel.getTicks() <= 900)
        {
            ItemStack stack = this.inv.getStackInSlot(1);
            if (isUnifiedUraninite(stack))
            {
                this.fuel.add(100);
                this.baseTemp = 700;
                stack.shrink(1);
                flag = true;
            }
        }

        if (this.fuel.isEmpty())
        {
            this.baseTemp = 0;
        }
        return flag;
    }
}
