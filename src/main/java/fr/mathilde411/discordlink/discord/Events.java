package fr.mathilde411.discordlink.discord;

import fr.mathilde411.discordlink.chat.Chat;
import fr.mathilde411.discordlink.chat.MessageSources;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Events extends ListenerAdapter {

    private final Bot bot;
    private final Chat chat;

    public Events(Bot bot, Chat chat) {
        this.bot = bot;
        this.chat = chat;
    }

    private static String username(User user) {
        String res = user.getGlobalName();
        return res != null ? res : user.getName();
    }

    private static String membername(Member member) {
        return member.getEffectiveName();
    }

    private Chat.Message getMessage(Message message, boolean isReply) {
        String username = message.getMember() == null ? username(message.getAuthor()) : membername(message.getMember());
        String content = message.getContentDisplay();

        Message inReplyTo;
        Chat.Message reply = null;
        if (!isReply && message.getType() == MessageType.INLINE_REPLY && (inReplyTo = message.getReferencedMessage()) != null)
            reply = getMessage(inReplyTo, true);

        if (!message.isWebhookMessage() || message.getAuthor().getIdLong() != bot.webhook().getIdLong())
            return new Chat.Message(MessageSources.DISCORD, username, content, reply);

        if(!username.equals(Webhook.DEFAULT_NAME))
            return new Chat.Message(MessageSources.MINECRAFT, username, content, reply);

        if (content.isEmpty()) {
            for (MessageEmbed embed : message.getEmbeds()) {
                if (embed.getDescription() != null && !embed.getDescription().isEmpty()) {
                    content = embed.getDescription();
                    break;
                }
            }
        }

        return new Chat.Message(MessageSources.SERVER, username, content, reply);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (
                event.getChannel().getIdLong() != bot.channelID()
                        || (event.getMessage().isWebhookMessage()
                        && event.getMessage().getAuthor().getIdLong() == bot.webhook().getIdLong())
        )
            return;

        chat.send(getMessage(event.getMessage(), false));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("players")) {
            playersCommand(event);
        }
    }

    private void playersCommand(SlashCommandInteractionEvent event) {
        Chat.PlayerData players = chat.players();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Online players (" + players.number() + "/" + players.max() + ")")
                .setDescription(String.join("\n", players.players()));

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
