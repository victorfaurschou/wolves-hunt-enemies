package com.victorfaurschou.wolveshuntenemies.mixin;

import com.victorfaurschou.wolveshuntenemies.WolvesHuntEnemiesConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {

    private static final int STUCK_CHECKS_THRESHOLD = 6;
    private static final double STUCK_MOVE_THRESHOLD_SQ = 0.25;

    @Unique
    private Vec3 wolves_hunt_enemies$lastPos;
    @Unique
    private int wolves_hunt_enemies$stuckChecks;

    @Inject(method = "tryToTeleportToOwner", at = @At("HEAD"), cancellable = true)
    private void onTryToTeleportToOwner(CallbackInfo ci) {
        if (!WolvesHuntEnemiesConfig.pathBack) return;
        if (!((Object) this instanceof Wolf wolf) || !wolf.isTame()) return;

        LivingEntity owner = wolf.getOwner();
        if (owner == null) return;

        wolf.setTarget(null);

        Vec3 pos = wolf.position();
        boolean madeProgress = wolves_hunt_enemies$lastPos == null
                || pos.distanceToSqr(wolves_hunt_enemies$lastPos) >= STUCK_MOVE_THRESHOLD_SQ;
        wolves_hunt_enemies$lastPos = pos;

        if (madeProgress) {
            wolves_hunt_enemies$stuckChecks = 0;
        } else if (++wolves_hunt_enemies$stuckChecks >= STUCK_CHECKS_THRESHOLD) {
            wolves_hunt_enemies$stuckChecks = 0;
            return;
        }

        if (wolf.getNavigation().moveTo(owner, 1.0)) {
            ci.cancel();
        }
    }
}
