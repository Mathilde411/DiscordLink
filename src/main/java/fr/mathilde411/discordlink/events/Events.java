package fr.mathilde411.discordlink.events;

import fr.mathilde411.discordlink.discord.Webhook;
import fr.mathilde411.discordlink.util.MCMDFormat;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class Events implements Listener {


    private final Webhook webhook;
    public Events(Webhook webhook) {
        this.webhook = webhook;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        webhook.sendPlayerEmbed(
                event.getPlayer(),
                "Server Join",
                MCMDFormat.toMarkdown(Objects.requireNonNull(event.getJoinMessage()))
        );
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        webhook.sendPlayerEmbed(
                event.getPlayer(),
                "Server Leave",
                MCMDFormat.toMarkdown(Objects.requireNonNull(event.getQuitMessage()))
        );
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        webhook.sendPlayerMessage(event.getPlayer(), event.getMessage());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Death Count", String.valueOf(event.getEntity().getStatistic(Statistic.DEATHS) + 1));
        webhook.sendPlayerEmbed(
                event.getEntity(),
                "Death",
                MCMDFormat.toMarkdown(event.getDeathMessage()),
                fields
        );
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        String message = "";
        Advancement adv = event.getAdvancement();
        if (!Objects.requireNonNull(adv.getDisplay()).shouldAnnounceChat())
            return;

        // We don't have the original message :(
        switch(adv.getDisplay().getType()) {
            case TASK -> message = event.getPlayer().getDisplayName() + " has made the advancement §a§l[" + adv.getDisplay().getTitle() + "]";
            case CHALLENGE -> message = event.getPlayer().getDisplayName() + " has completed the challenge §5§l[" + adv.getDisplay().getTitle() + "]";
            case GOAL -> message = event.getPlayer().getDisplayName() + " has reached the goal §a§l[" + adv.getDisplay().getTitle() + "]";
        }

        Map<String, String> fields = new HashMap<>();
        fields.put("Description", adv.getDisplay().getDescription());
        webhook.sendPlayerEmbed(
                event.getPlayer(),
                "Advancement",
                MCMDFormat.toMarkdown(message),
                fields
        );
    }


}
