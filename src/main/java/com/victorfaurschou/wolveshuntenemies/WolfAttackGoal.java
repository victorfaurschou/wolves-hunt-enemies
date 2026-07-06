package com.victorfaurschou.wolveshuntenemies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class WolfAttackGoal extends TargetGoal {

    private static final int SCAN_INTERVAL = 10;
    private static final int STUCK_CHECK_INTERVAL = 20;
    private static final int STUCK_MAX_FAILURES = 4;
    private static final double ATTACK_REACH_SQ = 9.0;
    private static final double MAX_OBSTRUCTION = 1.0;
    private static final int MIN_CHASE_MARGIN = 6;

    private final Wolf wolf;
    private @Nullable LivingEntity attackTarget;
    private int scanCooldown;

    private int stuckTimer;
    private int stuckChecks;
    private double prevDistSqToTarget;

    public WolfAttackGoal(Wolf wolf) {
        super(wolf, false);
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (--scanCooldown > 0) return false;
        scanCooldown = reducedTickDelay(SCAN_INTERVAL);

        if (!wolf.isTame() || wolf.isOrderedToSit()) return false;
        LivingEntity owner = wolf.getOwner();
        if (owner == null) return false;

        int r = WolvesHuntEnemiesConfig.attackRadius;
        int leash = WolvesHuntEnemiesConfig.chaseRadius;
        if (r + MIN_CHASE_MARGIN > leash) return false;

        AABB box = new AABB(
                owner.getX() - r, owner.getY() - r, owner.getZ() - r,
                owner.getX() + r, owner.getY() + r, owner.getZ() + r
        );

        List<Monster> candidates = wolf.level().getEntitiesOfClass(
                Monster.class, box,
                e -> e.isAlive() && WolvesHuntEnemiesConfig.isMobEnabled(e) && hasClearPath(wolf, e)
        );

        if (candidates.isEmpty()) return false;

        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(owner)));

        int wolfIndex = 0;
        if (WolvesHuntEnemiesConfig.packSpread) {
            UUID ownerUUID = owner.getUUID();
            AABB packBox = new AABB(
                    owner.getX() - 256, owner.getY() - 256, owner.getZ() - 256,
                    owner.getX() + 256, owner.getY() + 256, owner.getZ() + 256);
            List<Wolf> packmates = new ArrayList<>(wolf.level().getEntitiesOfClass(Wolf.class, packBox,
                    w -> w.isTame() && w.getOwner() != null && ownerUUID.equals(w.getOwner().getUUID())));
            if (!packmates.contains(wolf)) packmates.add(wolf);
            packmates.sort(Comparator.comparingInt(Entity::getId));
            wolfIndex = packmates.indexOf(wolf);
        }

        attackTarget = candidates.get(wolfIndex % (WolvesHuntEnemiesConfig.packSpread ? candidates.size() : 1));
        return true;
    }

    @Override
    public void start() {
        mob.setTarget(attackTarget);
        stuckTimer = STUCK_CHECK_INTERVAL;
        stuckChecks = 0;
        prevDistSqToTarget = wolf.distanceToSqr(attackTarget);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (wolf.isOrderedToSit()) return false;
        LivingEntity owner = wolf.getOwner();
        if (owner == null) return false;
        int leash = WolvesHuntEnemiesConfig.chaseRadius;
        if (owner.distanceToSqr(target) > (double) leash * leash) return false;

        if (--stuckTimer <= 0) {
            stuckTimer = STUCK_CHECK_INTERVAL;
            double distSq = wolf.distanceToSqr(target);
            if (distSq <= ATTACK_REACH_SQ || distSq < prevDistSqToTarget) {
                stuckChecks = 0;
            } else if (++stuckChecks >= STUCK_MAX_FAILURES) {
                return false;
            }
            prevDistSqToTarget = distSq;
        }

        return true;
    }

    private static boolean hasClearPath(Wolf wolf, Monster target) {
        Level level = wolf.level();
        Vec3 from = wolf.getEyePosition();
        Vec3 to = target.getEyePosition();
        double dist = from.distanceTo(to);
        if (dist < 1.0e-4) return true;

        int steps = Math.max(1, (int) Math.ceil(dist / 0.5));
        double stepLen = dist / steps;
        double obstructed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.lerp(to, (double) i / steps);
            pos.set(p.x, p.y, p.z);
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty()) {
                obstructed += stepLen;
                if (obstructed > MAX_OBSTRUCTION) return false;
            }
        }
        return true;
    }
}
