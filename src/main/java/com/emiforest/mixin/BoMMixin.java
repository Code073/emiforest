package com.emiforest.mixin;

import com.emiforest.forest.ForestManager;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoM.class, remap = false)
public class BoMMixin {

    @Inject(method = "setGoal", at = @At("TAIL"), remap = false)
    private static void afterSetGoal(EmiRecipe recipe, CallbackInfo ci) {
        ForestManager.addTree(BoM.tree);
    }
}


