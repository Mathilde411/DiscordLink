package fr.mathilde411.discordlink.discord;

import fr.mathilde411.discordlink.Data;
import fr.mathilde411.discordlink.DiscordLink;
import fr.mathilde411.discordlink.chat.Chat;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.file.FileConfiguration;

public class Bot {

    private final String token;
    private final Data data;
    private final long channelID;

    private JDA jda;
    private Chat chat;
    private Thread statusThread;
    private Webhook webhook;


    public Bot(FileConfiguration config, Data data) {
        this.token = config.getString("token");
        this.data = data;
        this.channelID = config.getLong("channel");
    }

    public void start(Chat chat) {
        if(this.token == null || this.token.isEmpty()) {
            DiscordLink.LOGGER.warning("Discord bot token is invalid. Discord messages won't be sent to the server.");
            return;
        }

        try {
            this.jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new Events(this, chat))
                    .build().awaitReady();
        } catch (InvalidTokenException e) {
            DiscordLink.LOGGER.warning("Discord bot token is invalid. Discord messages won't be sent to the server.");
            return;
        } catch(InterruptedException | IllegalArgumentException e) {
            DiscordLink.LOGGER.warning(String.format(
                    "Discord error (%s). Discord messages won't be sent to the server.",
                    e.getMessage()
            ));
        }
        this.chat = chat;
        setupWebhook();
        setupCommands();
        statusThread = new Thread(() -> {
            try {
                while (true) {
                    updateStatus();
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ignored) {}
        });
        statusThread.start();
    }

    public void stop() {
        if (statusThread != null)
            statusThread.interrupt();
        if (jda != null)
            jda.shutdown();
        jda = null;
        chat = null;
        statusThread = null;
    }

    private void setupWebhook() {
        Long webhookID = (Long) data.get("webhook_id");
        if (webhookID != null) {
            try{

                Webhook webhook = jda.retrieveWebhookById(webhookID).complete();
                if (webhook.getChannel().getIdLong() != channelID) {
                    TextChannel channel = jda.getTextChannelById(channelID);
                    if (channel != null)
                        webhook.getManager().setChannel(channel).queue();
                    else
                        DiscordLink.LOGGER.warning("Could not change webhook channel, channel wasn't found.");
                }
                data.set("webhook_token", webhook.getToken());
                this.webhook = webhook;
            } catch(RuntimeException e) {
                if (e instanceof ErrorResponseException err && err.getErrorResponse() == ErrorResponse.UNKNOWN_WEBHOOK)
                    createWebhook();
                else
                    DiscordLink.LOGGER.warning("Error while creating webhook: " + e.getMessage());
            }
        } else {
            createWebhook();
        }

    }

    private void createWebhook() {
        try {
            TextChannel channel = jda.getTextChannelById(channelID);
            if (channel == null) {
                DiscordLink.LOGGER.warning("Error while creating webhook: channel doesn't exist.");
                return;
            }
            Webhook webhook = channel.createWebhook("Chat").complete();
            data.set("webhook_id", webhook.getIdLong());
            data.set("webhook_token", webhook.getToken());
            this.webhook = webhook;
        } catch(RuntimeException e) {
            DiscordLink.LOGGER.warning("Error while creating webhook: " + e.getMessage());
        }
    }

    private void setupCommands() {
        jda.updateCommands().addCommands(
                Commands.slash("players", "Get online players.")
        ).queue();
    }

    public void updateStatus() {
        Chat.PlayerData pl = chat.players();
        jda.getPresence().setPresence(
                OnlineStatus.ONLINE,
                Activity.customStatus(pl.number() + "/" + pl.max() + " players")
        );
    }

    public JDA jda() {
        return jda;
    }

    public Webhook webhook() {
        return webhook;
    }

    public long channelID() {
        return channelID;
    }

}
