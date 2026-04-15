package com.shugabrush.raintegration.mixin.industrialforegoing;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import com.buuz135.industrial.module.ModuleCore;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.shugabrush.raintegration.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidTankComponent.class, remap = false)
public class FluidTankComponentMixin extends FluidTank
{

    @Shadow
    private FluidTankComponent.Action tankAction;

    public FluidTankComponentMixin(int capacity)
    {
        super(capacity);
    }

    /**
     * @author Shugabrush
     * @reason Make sure fluid tanks that would normally fill
     *         with Industrial foregoing's XP fill with the configured XP instead
     */
    @Overwrite
    public int fill(FluidStack resource, FluidAction action)
    {
        Fluid experienceFluid = ConfigHolder.instance.fluidConfigs.getExperienceFluid();
        if (experienceFluid != null && resource.getFluid() == ModuleCore.ESSENCE.getSourceFluid().get()) {
            resource = new FluidStack(experienceFluid, resource.getAmount());
        }
        return tankAction.canFill() ? super.fill(resource, action) : 0;
    }

    @Overwrite
    public int fillForced(FluidStack resource, FluidAction action)
    {
        Fluid experienceFluid = ConfigHolder.instance.fluidConfigs.getExperienceFluid();
        if (experienceFluid != null && resource.getFluid() == ModuleCore.ESSENCE.getSourceFluid().get()) {
            resource = new FluidStack(experienceFluid, resource.getAmount());
        }
        return super.fill(resource, action);
    }

    @Overwrite
    private FluidStack drainInternal(FluidStack resource, FluidAction action)
    {
        Fluid experienceFluid = ConfigHolder.instance.fluidConfigs.getExperienceFluid();
        if (experienceFluid != null && resource.getFluid() == ModuleCore.ESSENCE.getSourceFluid().get()) {
            resource = new FluidStack(experienceFluid, resource.getAmount());
        }

        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    private FluidStack drainInternal(int maxDrain, FluidAction action)
    {
        int drained = maxDrain;
        if (fluid.getAmount() < drained) {
            drained = fluid.getAmount();
        }
        FluidStack stack = new FluidStack(fluid, drained);
        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            onContentsChanged();
        }
        return stack;
    }

    @Overwrite
    public FluidStack drainForced(FluidStack resource, FluidAction action)
    {
        Fluid experienceFluid = ConfigHolder.instance.fluidConfigs.getExperienceFluid();
        if (experienceFluid != null && resource.getFluid() == ModuleCore.ESSENCE.getSourceFluid().get()) {
            resource = new FluidStack(experienceFluid, resource.getAmount());
        }

        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) {
            return FluidStack.EMPTY;
        }
        return drainForced(resource.getAmount(), action);
    }

    public FluidStack drainForced(int maxDrain, FluidAction action)
    {
        return drainInternal(maxDrain, action);
    }
}
