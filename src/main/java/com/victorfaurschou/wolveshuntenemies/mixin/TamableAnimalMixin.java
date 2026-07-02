package com.victorfaurschou.wolveshuntenemies.mixin;

import com.victorfaurschou.wolveshuntenemies.WolvesHuntEnemiesConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {

    @Inject(method = "tryToTeleportToOwner", at = @At("HEAD"), cancellable = true)
    private void onTryToTeleportToOwner(CallbackInfo ci) {
        if (!WolvesHuntEnemiesConfig.pathBack) return;
        if (!((Object) this instanceof Wolf wolf) || !wolf.isTame()) return;

        LivingEntity owner = wolf.getOwner();
        if (owner == null) return;

        wolf.setTarget(null);
        if (wolf.getNavigation().moveTo(owner, 1.0)) {
            ci.cancel();
        }
    }
}
