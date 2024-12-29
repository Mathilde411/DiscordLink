package fr.mathilde411.discordlink.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.List;

public class Chat {
    private final Server server;

    public record Message(MessageSources source, String sender, String message, Message reply) {}

    public record PlayerData(List<String> players, int number, int max) {}

    public Chat(Server server) {
        this.server = server;
    }

    private void format(Message message, ComponentBuilder builder, boolean inReply) {
        if (inReply)
            builder.append("Reply to: ").color(ChatColor.GRAY);

        if (message.source != MessageSources.MINECRAFT)
            builder.append(String.format("[%s] ", message.source.getName())).color(ChatColor.of(message.source.color()));

        if (message.source != MessageSources.SERVER) {
            builder.append(String.format("<%s> ", message.sender));
            if (inReply)
                builder.color(ChatColor.GRAY);
            else
                builder.color(ChatColor.WHITE);
        }

        builder.append(message.message);

        if (inReply) {
            builder.color(ChatColor.GRAY);
            builder.append("\n> ");
        }
        else
            builder.color(ChatColor.WHITE);

    }

    public void send(Message message) {
        ComponentBuilder builder = new ComponentBuilder();
        if (message.reply != null) {
            format(message.reply, builder, true);
        }
        format(message, builder, false);

        server.getOnlinePlayers().forEach(player ->
                player.spigot().sendMessage(builder.build())
        );
    }

    public PlayerData players() {
        List<String> players = server.getOnlinePlayers().stream().map(Player::getName).toList();
        return new PlayerData(
                players,
                players.size(),
                server.getMaxPlayers()
        );
    }
}
