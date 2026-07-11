package com.example.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.level.GameType;

public class GCommand extends Command {

    public GCommand() {
        super("g", "Gamemode command.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {

        builder.then(literal("c").executes(ctx -> {
            mc.gameMode.setLocalMode(GameType.CREATIVE);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("s").executes(ctx -> {
            mc.gameMode.setLocalMode(GameType.SURVIVAL);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("a").executes(ctx -> {
            mc.gameMode.setLocalMode(GameType.ADVENTURE);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("sp").executes(ctx -> {
            mc.gameMode.setLocalMode(GameType.SPECTATOR);
            return SINGLE_SUCCESS;
        }));
    }
}
