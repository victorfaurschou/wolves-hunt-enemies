package com.victorfaurschou.wolveshuntenemies.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class WolvesHuntEnemiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("wolves-hunt-enemies")
                        .then(ClientCommands.literal("config")
                                .executes(ctx -> {
                                    Minecraft mc = Minecraft.getInstance();
                                    mc.execute(() -> mc.setScreen(ClothConfigScreen.create(null)));
                                    return 1;
                                }))
                        .then(ClientCommands.literal("version")
                                .executes(ctx -> {
                                    String version = FabricLoader.getInstance()
                                            .getModContainer("wolves-hunt-enemies")
                                            .map(c -> c.getMetadata().getVersion().getFriendlyString())
                                            .orElse("unknown");
                                    ctx.getSource().sendFeedback(Component.literal("Wolves Hunt Enemies " + version));
                                    return 1;
                                }))));
    }
}
