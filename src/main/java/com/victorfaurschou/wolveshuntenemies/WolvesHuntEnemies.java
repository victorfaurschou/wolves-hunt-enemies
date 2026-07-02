package com.victorfaurschou.wolveshuntenemies;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WolvesHuntEnemies implements ModInitializer {

    public static final String MOD_ID = "wolves-hunt-enemies";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        WolvesHuntEnemiesConfig.load();

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            boolean pickupItems = WolvesHuntEnemiesConfig.lootPickup;
            boolean pickupXp    = WolvesHuntEnemiesConfig.experiencePickup;
            if (!pickupItems && !pickupXp) return;
            if (!(entity.level() instanceof ServerLevel serverLevel)) return;

            Wolf wolf = findResponsibleWolf(entity, source);
            if (wolf == null) return;
            if (!(wolf.getOwner() instanceof ServerPlayer player)) return;

            Vec3 pos = entity.position();
            long deathGameTime = serverLevel.getGameTime();

            // Schedule for next tick so items/XP are guaranteed to be in world state
            serverLevel.getServer().execute(() -> {
                if (player.isRemoved()) return;

                AABB box = AABB.ofSize(pos, 6, 6, 6);

                if (pickupItems) {
                    List<ItemEntity> drops = new ArrayList<>(serverLevel.getEntitiesOfClass(
                            ItemEntity.class, box, e -> !e.isRemoved() && e.hasPickUpDelay()));
                    for (ItemEntity drop : drops) {
                        ItemStack stack = drop.getItem().copy();
                        player.getInventory().add(stack);
                        if (stack.isEmpty()) {
                            drop.discard();
                        } else {
                            drop.setItem(stack);
                        }
                    }
                }

                if (pickupXp) {
                    int maxOrbAge = (int) (serverLevel.getGameTime() - deathGameTime) + 2;
                    List<ExperienceOrb> orbs = new ArrayList<>(serverLevel.getEntitiesOfClass(
                            ExperienceOrb.class, box, e -> !e.isRemoved() && e.tickCount <= maxOrbAge));
                    for (ExperienceOrb orb : orbs) {
                        player.giveExperiencePoints(orb.getValue());
                        orb.discard();
                    }
                }
            });
        });
    }

    // Checks direct attacker, indirect attacker, and last mob to hurt the entity
    // (covers kills via DoT, fire, fall damage after a wolf attack)
    private static @Nullable Wolf findResponsibleWolf(LivingEntity entity, DamageSource source) {
        if (source.getEntity() instanceof Wolf w && w.isTame()) return w;
        // getLastHurtByMob() auto-clears after 100 ticks (5s). Only use it for non-entity kills
        // (fire, fall, drowning) — if another player or mob dealt the blow, skip this to avoid
        // giving wolf-owner credit for someone else's kill in multiplayer.
        if (source.getEntity() == null && entity.getLastHurtByMob() instanceof Wolf w && w.isTame()) return w;
        return null;
    }
}
