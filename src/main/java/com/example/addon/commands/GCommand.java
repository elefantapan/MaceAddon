package com.example.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.CommandSource;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.GameMode;

public class GCommand extends Command {

    public GCommand() {
        super("g", "Client side gamemode illusion.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        builder.then(literal("c").executes(context -> {
            MinecraftClient.getInstance()
                .interactionManager
                .setGameMode(GameMode.CREATIVE);

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("s").executes(context -> {
            MinecraftClient.getInstance()
                .interactionManager
                .setGameMode(GameMode.SURVIVAL);

            return SINGLE_SUCCESS;
        }));
    }
}
