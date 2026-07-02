package com.victorfaurschou.wolveshuntenemies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.monster.Monster;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WolvesHuntEnemiesConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve(WolvesHuntEnemies.MOD_ID + ".json").toFile();
    public static final List<String> ALL_HOSTILE_MOBS = List.of(
            "minecraft:blaze",
            "minecraft:bogged",
            "minecraft:breeze",
            "minecraft:cave_spider",
            "minecraft:creeper",
            "minecraft:drowned",
            "minecraft:elder_guardian",
            "minecraft:enderman",
            "minecraft:endermite",
            "minecraft:evoker",
            "minecraft:guardian",
            "minecraft:husk",
            "minecraft:magma_cube",
            "minecraft:phantom",
            "minecraft:piglin_brute",
            "minecraft:pillager",
            "minecraft:ravager",
            "minecraft:silverfish",
            "minecraft:skeleton",
            "minecraft:slime",
            "minecraft:spider",
            "minecraft:stray",
            "minecraft:vex",
            "minecraft:vindicator",
            "minecraft:warden",
            "minecraft:witch",
            "minecraft:wither_skeleton",
            "minecraft:zombie",
            "minecraft:zombie_villager",
            "minecraft:zombified_piglin",
            "minecraft:zoglin"
    );

    private static final Set<String> ALL_HOSTILE_MOBS_SET = new HashSet<>(ALL_HOSTILE_MOBS);
    
    private static final Set<String> DEFAULT_ENABLED_MOBS;
    static {
        Set<String> d = new HashSet<>();
        d.add("minecraft:skeleton");
        d.add("minecraft:zombie");
        d.add("minecraft:spider");
        DEFAULT_ENABLED_MOBS = Collections.unmodifiableSet(d);
    }

    public static volatile float   attackRadius = 16.0f;
    public static volatile float   chaseRadius  = 32.0f;
    public static volatile boolean packSpread   = false;
    public static volatile boolean pathBack     = false;
    public static volatile boolean lootPickup       = false;
    public static volatile boolean experiencePickup = false;
    public static Set<String> enabledMobs = Collections.synchronizedSet(new HashSet<>(DEFAULT_ENABLED_MOBS));

    public static boolean isMobEnabled(Monster entity) {
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return false;
        String idStr = id.toString();
        return !ALL_HOSTILE_MOBS_SET.contains(idStr) || enabledMobs.contains(idStr);
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                try (FileReader r = new FileReader(CONFIG_FILE)) {
                    ConfigData d = GSON.fromJson(r, ConfigData.class);
                    if (d != null) {
                        attackRadius = d.attackRadius != null ? Math.max(4f, Math.min(64f, d.attackRadius))    : 16.0f;
                        chaseRadius  = d.chaseRadius  != null ? Math.max(4f, Math.min(128f, d.chaseRadius))    : 32.0f;
                        packSpread   = d.packSpread   != null ? d.packSpread   : false;
                        pathBack     = d.pathBack     != null ? d.pathBack     : false;
                        lootPickup       = d.lootPickup       != null ? d.lootPickup       : false;
                        experiencePickup = d.experiencePickup != null ? d.experiencePickup : false;
                        enabledMobs = Collections.synchronizedSet(
                                d.enabledMobs != null ? new HashSet<>(d.enabledMobs) : new HashSet<>(DEFAULT_ENABLED_MOBS));
                    }
                }
            }
        } catch (IOException e) {
            WolvesHuntEnemies.LOGGER.warn("Failed to load config", e);
        } catch (com.google.gson.JsonSyntaxException e) {
            WolvesHuntEnemies.LOGGER.warn("Config is malformed or from an older version, resetting to defaults", e);
            CONFIG_FILE.delete();
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(CONFIG_FILE)) {
                List<String> mobList;
                synchronized (enabledMobs) {
                    mobList = new ArrayList<>(enabledMobs);
                }
                GSON.toJson(new ConfigData(attackRadius, chaseRadius, packSpread, pathBack, lootPickup, experiencePickup, mobList), w);
            }
        } catch (IOException e) {
            WolvesHuntEnemies.LOGGER.warn("Failed to save config", e);
        }
    }

    static class ConfigData {
        Float   attackRadius;
        Float   chaseRadius;
        Boolean packSpread;
        Boolean pathBack;
        Boolean lootPickup;
        Boolean experiencePickup;
        List<String> enabledMobs;

        ConfigData() {}

        ConfigData(float attackRadius, float chaseRadius, boolean packSpread, boolean pathBack,
                   boolean lootPickup, boolean experiencePickup, List<String> enabledMobs) {
            this.attackRadius     = attackRadius;
            this.chaseRadius      = chaseRadius;
            this.packSpread       = packSpread;
            this.pathBack         = pathBack;
            this.lootPickup       = lootPickup;
            this.experiencePickup = experiencePickup;
            this.enabledMobs      = enabledMobs;
        }
    }
}
