package com.shugabrush.raintegration.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = NotifiableFluidTank.class, remap = false)
public class UnifyFluidTanksMixin {
//    @Shadow
//    @Final
//    protected boolean allowSameFluids;
//
//    @Shadow
//    @Final
//    protected CustomFluidTank[] storages;
//
//    /**
//     * @author Shugabrush
//     * @reason Unify certain fluids
//     */
//    @Overwrite
//    public int fillInternal(FluidStack resource, IFluidHandler.FluidAction action) {
//
//
//        if (resource.isEmpty()) return 0;
//        var copied = resource.copy();
//        CustomFluidTank existingStorage = null;
//        if (!allowSameFluids) {
//            for (var storage : storages) {
//                if (!storage.getFluid().isEmpty() && storage.getFluid().isFluidEqual(resource)) {
//                    existingStorage = storage;
//                    break;
//                }
//            }
//        }
//        if (existingStorage == null) {
//            for (var storage : storages) {
//                var filled = storage.fill(copied.copy(), action);
//                if (filled > 0) {
//                    copied.shrink(filled);
//                    if (!allowSameFluids) {
//                        break;
//                    }
//                }
//                if (copied.isEmpty()) break;
//            }
//        } else {
//            copied.shrink(existingStorage.fill(copied.copy(), action));
//        }
//        return resource.getAmount() - copied.getAmount();
//    }
}
