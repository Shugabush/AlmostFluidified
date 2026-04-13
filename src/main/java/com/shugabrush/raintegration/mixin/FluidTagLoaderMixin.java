package com.shugabrush.raintegration.mixin;

import com.almostreliable.unified.utils.Utils;
import com.shugabrush.raintegration.MoreUnification;
import com.shugabrush.raintegration.unification.FluidTagReloadHandler;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.material.Fluid;
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
    @Shadow @Final private String directory;

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private <T> void onCreateLoadResult(Map<ResourceLocation, List<TagLoader.EntryWithSource>> builders, CallbackInfoReturnable<Map<ResourceLocation, Collection<T>>> cir)
    {
        if (directory.equals("tags/fluids"))
        {
            try
            {
                Map<ResourceLocation, Collection<Holder<Fluid>>> tags = Utils.cast(cir.getReturnValue());
                FluidTagReloadHandler.initFluidTags(tags);
                FluidTagReloadHandler.run();
            }
            catch (Exception e)
            {
                MoreUnification.LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
