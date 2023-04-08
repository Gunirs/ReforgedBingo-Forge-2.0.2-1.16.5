package com.envyful.reforged.bingo.forge.listener;

import com.envyful.reforged.bingo.forge.ReforgedBingo;
import com.envyful.reforged.bingo.forge.player.BingoAttribute;
import com.envyful.reforged.bingo.forge.shade.envy.api.player.EnvyPlayer;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PokemonCatchListener {
    private final ReforgedBingo mod;

    public PokemonCatchListener(ReforgedBingo mod) {
        this.mod = mod;
        Pixelmon.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBingoSlotComplete(CaptureEvent.SuccessfulCapture event) {
        // TODO Gunirs replace event.player to event.getPlayer()
        EnvyPlayer<ServerPlayerEntity> player = this.mod.getPlayerManager().getPlayer(event.getPlayer());
        if (player != null) {
            BingoAttribute attribute = player.getAttribute(ReforgedBingo.class);
            if (attribute != null) {
                attribute.catchPokemon(event.getPokemon().getSpecies());
            }
        }
    }

    @SubscribeEvent
    public void onRaidDenCapture(CaptureEvent.SuccessfulRaidCapture event) {
        // TODO Gunirs replace event.player to event.getPlayer()
        EnvyPlayer<ServerPlayerEntity> player = this.mod.getPlayerManager().getPlayer(event.getPlayer());
        if (player != null) {
            BingoAttribute attribute = player.getAttribute(ReforgedBingo.class);
            if (attribute != null) {
                attribute.catchPokemon(event.getRaidPokemon().getSpecies());
            }
        }
    }
}