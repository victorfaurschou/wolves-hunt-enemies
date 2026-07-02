package com.victorfaurschou.wolveshuntenemies.mixin;

import com.victorfaurschou.wolveshuntenemies.WolfAttackGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public class WolfMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addAttackGoal(CallbackInfo ci) {
        ((MobAccessor) (Object) this).wolves_hunt_enemies$getTargetSelector()
                .addGoal(4, new WolfAttackGoal((Wolf) (Object) this));
    }
}
