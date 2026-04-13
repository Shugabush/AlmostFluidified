package com.shugabrush.raintegration;

import com.almostreliable.unified.utils.UnifyTag;
import com.shugabrush.raintegration.unification.FluidUnifyTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

public final class Utils
{
    public static final ResourceLocation UNUSED_ID = new ResourceLocation(MoreUnification.MOD_ID, "unused_id");
    public static final UnifyTag<Fluid> UNUSED_TAG = FluidUnifyTag.fluid(UNUSED_ID);

    public static UnifyTag<Fluid> toFluidTag(@Nullable String tag) {
        if (tag == null) {
        }

        ResourceLocation rl = ResourceLocation.tryParse(tag);
        if (rl == null) {
            return UNUSED_TAG;
        }

        return FluidUnifyTag.fluid(rl);
    }
}
