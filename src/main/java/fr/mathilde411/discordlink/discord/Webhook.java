package fr.mathilde411.discordlink.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import club.minnced.discord.webhook.util.WebhookErrorHandler;
import fr.mathilde411.discordlink.Data;
import fr.mathilde411.discordlink.DiscordLink;
import fr.mathilde411.discordlink.util.MCMDFormat;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Webhook implements WebhookErrorHandler {

    // Source and license: https://commons.wikimedia.org/wiki/File:Minecraft-creeper-face.jpg
    private static final String DEFAULT_AVATAR = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Minecraft-creeper-face.jpg/640px-Minecraft-creeper-face.jpg";
    public static final String DEFAULT_NAME = "Minecraft Server";

    private final Data data;
    private boolean initialized;
    private WebhookClient webhookClient = null;

    public Webhook(Data data) {
        this.data = data;
        this.initialized = false;
    }

    public void init() {
        uninit();

        String token = (String) data.get("webhook_token");
        Long id = (Long) data.get("webhook_id");
        if(id == null || token == null || token.isEmpty()) {
            DiscordLink.LOGGER.warning("Webhook is not set, won't send messages to Discord.");
            return;
        }

        webhookClient = WebhookClient.withId(id, token).setErrorHandler(this);
        initialized = true;
    }

    public void uninit() {
        if (webhookClient != null) {
            webhookClient.close();
            initialized = false;
        }
    }

    private void send(WebhookMessage message) {
        if (!initialized || webhookClient == null) {
            DiscordLink.LOGGER.warning("Webhook is not initialized, won't send message to Discord.");
            return;
        }

        try {
            webhookClient.send(message);
        } catch (Exception throwable) {
            DiscordLink.LOGGER.warning(
                    String.format(
                            "Webhook couldn't send message (%s).",
                            throwable.getMessage()
                    )
            );
        }
    }


    private WebhookMessageBuilder setPlayer(OfflinePlayer player, WebhookMessageBuilder builder) {
        return builder.setUsername(player.getName()).setAvatarUrl("https://crafthead.net/avatar/" + player.getUniqueId());
    }

    private WebhookEmbedBuilder setPlayer(OfflinePlayer player, WebhookEmbedBuilder builder) {
        return builder.setAuthor(new WebhookEmbed.EmbedAuthor(Objects.requireNonNull(player.getName()), "https://crafthead.net/avatar/" + player.getUniqueId(), null));
    }

    public void sendPlayerMessage(OfflinePlayer player, String message) {

        send(setPlayer(player, new WebhookMessageBuilder()).setContent(message).build());
    }

    public void sendPlayerEmbed(OfflinePlayer player, String title, MCMDFormat.MarkdownMessage message, Map<String, String> fields) {

        WebhookEmbedBuilder embedBuilder = setPlayer(player, new WebhookEmbedBuilder())
                .setTitle(new WebhookEmbed.EmbedTitle(title, null))
                .setDescription(message.message())
                .setColor(message.firstColor().color());
        for (Map.Entry<String, String> field : fields.entrySet()) {
            embedBuilder.addField(new WebhookEmbed.EmbedField(false , field.getKey(), field.getValue()));
        }
        send(new WebhookMessageBuilder().setUsername(DEFAULT_NAME).setAvatarUrl(DEFAULT_AVATAR).addEmbeds(embedBuilder.build()).build());
    }

    public void sendPlayerEmbed(OfflinePlayer player, String title, MCMDFormat.MarkdownMessage message) {
        sendPlayerEmbed(player, title, message, new HashMap<>());
    }

    @Override
    public void handle(@NotNull WebhookClient client, @NotNull String message, @Nullable Throwable throwable) {
        DiscordLink.LOGGER.warning(
                String.format(
                        "Webhook couldn't send message (%s).",
                        throwable != null ? throwable.getMessage() : "Unknown error"
                )
        );
    }
}
