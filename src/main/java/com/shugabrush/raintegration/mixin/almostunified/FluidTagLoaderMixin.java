package com.shugabrush.raintegration.mixin.almostunified;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.Utils;
import com.shugabrush.raintegration.RAIntegration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class FluidTagLoaderMixin
{

    @Shadow
    @Final
    private String directory;

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private <T> void onCreateLoadResult(Map< ResourceLocation, List< TagLoader.EntryWithSource>> map,
                                        CallbackInfoReturnable< Map< ResourceLocation, Collection< T>>> cir)
    {
        if (directory.equals("tags/fluids"))
        {
            try
            {
                Map< ResourceLocation, Collection< Holder< Fluid>>> tags = Utils.cast(cir.getReturnValue());
                RAIntegration.initFluidTags(tags);
            }
            catch (Exception e)
            {
                RAIntegration.LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
