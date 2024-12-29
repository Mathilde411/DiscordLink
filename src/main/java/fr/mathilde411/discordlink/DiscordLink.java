package fr.mathilde411.discordlink;

import fr.mathilde411.discordlink.chat.Chat;
import fr.mathilde411.discordlink.discord.Bot;
import fr.mathilde411.discordlink.discord.Webhook;
import fr.mathilde411.discordlink.events.Events;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class DiscordLink extends JavaPlugin {
    public static Logger LOGGER = null;

    private final Bot bot;
    private final Webhook webhook;
    private Chat chat;


    public DiscordLink() throws IOException {
        DiscordLink.LOGGER = getLogger();
        FileConfiguration config = this.getConfig();
        Data data = new Data(getDataFolder().toPath());
        this.bot = new Bot(config, data);
        this.webhook = new Webhook(data);
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig();

        this.chat = new Chat(this.getServer());
        this.startBot();
        this.getServer().getPluginManager().registerEvents(
                new Events(webhook),
                this
        );
    }

    private void startBot() {
        bot.start(this.chat);
        webhook.init();
    }

    @Override
    public void onDisable() {
        bot.stop();
        webhook.uninit();
    }
}
