package com.victorfaurschou.wolveshuntenemies.client;

import com.victorfaurschou.wolveshuntenemies.WolvesHuntEnemiesConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.wolves-hunt-enemies.title"))
                .setSavingRunnable(WolvesHuntEnemiesConfig::save);

        ConfigEntryBuilder e = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("config.wolves-hunt-enemies.category.general"));

        general.addEntry(e.startIntSlider(
                        Component.translatable("config.wolves-hunt-enemies.attack_radius"),
                        WolvesHuntEnemiesConfig.attackRadius, 4, 32)
                .setDefaultValue(12)
                .setTextGetter(v -> Component.literal(v + " blocks"))
                .setSaveConsumer(v -> WolvesHuntEnemiesConfig.attackRadius = v)
                .setTooltip(Component.translatable("config.wolves-hunt-enemies.attack_radius.tooltip"))
                .build());

        general.addEntry(e.startIntSlider(
                        Component.translatable("config.wolves-hunt-enemies.chase_radius"),
                        WolvesHuntEnemiesConfig.chaseRadius, 4, 64)
                .setDefaultValue(24)
                .setTextGetter(v -> Component.literal(v + " blocks"))
                .setSaveConsumer(v -> WolvesHuntEnemiesConfig.chaseRadius = v)
                .setTooltip(Component.translatable("config.wolves-hunt-enemies.chase_radius.tooltip"))
                .build());

        general.addEntry(e.startBooleanToggle(
                        Component.translatable("config.wolves-hunt-enemies.pack_spread"),
                        WolvesHuntEnemiesConfig.packSpread)
                .setDefaultValue(false)
                .setSaveConsumer(v -> WolvesHuntEnemiesConfig.packSpread = v)
                .setTooltip(Component.translatable("config.wolves-hunt-enemies.pack_spread.tooltip"))
                .build());

        general.addEntry(e.startBooleanToggle(
                        Component.translatable("config.wolves-hunt-enemies.path_back"),
                        WolvesHuntEnemiesConfig.pathBack)
                .setDefaultValue(false)
                .setSaveConsumer(v -> WolvesHuntEnemiesConfig.pathBack = v)
                .setTooltip(Component.translatable("config.wolves-hunt-enemies.path_back.tooltip"))
                .build());

        general.addEntry(e.startBooleanToggle(
                        Component.translatable("config.wolves-hunt-enemies.loot_pickup"),
                        WolvesHuntEnemiesConfig.lootPickup)
                .setDefaultValue(false)
                .setSaveConsumer(v -> WolvesHuntEnemiesConfig.lootPickup = v)
                .setTooltip(Component.translatable("config.wolves-hunt-enemies.loot_pickup.tooltip"))
                .build());

        general.addEntry(e.startBooleanToggle(
                        Component.translatable("config.wolves-hunt-enemies.experience_pickup"),
                        WolvesHuntEnemiesConfig.experiencePickup)
                .setDefaultValue(false)
                .setSaveConsumer(v -> WolvesHuntEnemiesConfig.experiencePickup = v)
                .setTooltip(Component.translatable("config.wolves-hunt-enemies.experience_pickup.tooltip"))
                .build());

        ConfigCategory mobs = builder.getOrCreateCategory(
                Component.translatable("config.wolves-hunt-enemies.category.mobs"));

        for (String mobId : WolvesHuntEnemiesConfig.ALL_HOSTILE_MOBS) {
            String[] parts = mobId.split(":");
            boolean vanillaDefault = mobId.equals("minecraft:skeleton")
                    || mobId.equals("minecraft:zombie")
                    || mobId.equals("minecraft:spider");
            mobs.addEntry(e.startBooleanToggle(
                            Component.translatable("entity." + parts[0] + "." + parts[1]),
                            WolvesHuntEnemiesConfig.enabledMobs.contains(mobId))
                    .setDefaultValue(vanillaDefault)
                    .setSaveConsumer(v -> {
                        if (v) WolvesHuntEnemiesConfig.enabledMobs.add(mobId);
                        else   WolvesHuntEnemiesConfig.enabledMobs.remove(mobId);
                    })
                    .build());
        }

        return builder.build();
    }
}
