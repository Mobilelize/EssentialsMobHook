package net.mobilelize.essentialsMobHook.listener;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.messaging.IMessageRecipient;
import net.ess3.api.events.PrivateMessagePreSendEvent;
import net.ess3.api.events.PrivateMessageSentEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.mobilelize.essentialsMobHook.EssentialsMobHook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class SimpleMessageRecipientHook implements Listener {

    private final IEssentials ess;
    private final EssentialsMobHook plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public SimpleMessageRecipientHook(IEssentials ess, EssentialsMobHook plugin) {
        this.ess = ess;
        this.plugin = plugin;
    }

    private void sendFmt(IMessageRecipient recipient, String template, TagResolver resolver) {
        Component component = MM.deserialize(template, resolver);
        UUID uuid = recipient.getUUID();
        if (uuid != null) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.sendMessage(component);
                return;
            }
        }
        recipient.sendMessage(PlainTextComponentSerializer.plainText().serialize(component));
    }

    @EventHandler
    public void onPm(PrivateMessagePreSendEvent event) {
        event.setCancelled(true);

        final FileConfiguration config = plugin.getConfig();
        final IMessageRecipient sender = event.getSender();
        final IMessageRecipient recipient = event.getRecipient();
        final String message = event.getMessage();

        final User senderUser = ess.getUser(sender.getUUID());
        final User recipientUser = ess.getUser(recipient.getUUID());

        final PmContext ctx = PmContext.resolve(sender, recipient, message, ess);

        // --- Reachability ---
        if (!recipient.isReachable()) {
            sendFmt(sender, config.getString("msg-unreachable", "<dark_red><recipient> recently went offline.</dark_red>"), ctx.resolver);
            firePost(sender, recipient, message, IMessageRecipient.MessageResponse.UNREACHABLE);
            return;
        }

        if (recipientUser != null && recipientUser.isIgnoreMsg()) {
            final boolean bypass = senderUser != null && senderUser.isAuthorized("essentials.msgtoggle.bypass");
            if (!bypass) {
                sendFmt(sender, config.getString("msg-ignored", "<red><recipient></red> <dark_red>has messages disabled.</dark_red>"), ctx.resolver);
                firePost(sender, recipient, message, IMessageRecipient.MessageResponse.MESSAGES_IGNORED);
                return;
            }
        }
        if (senderUser != null && recipientUser != null && recipientUser.isIgnoredPlayer(senderUser)) {
            firePost(sender, recipient, message, IMessageRecipient.MessageResponse.SENDER_IGNORED);
            return;
        }

        final boolean afk = recipientUser != null && recipientUser.isAfk();

        // --- Deliver to recipient ---
        sendFmt(recipient, config.getString("msg-format-recipient", "<yellow>(From <sender>)</yellow> <yellow><message></yellow>"), ctx.resolver);

        // --- Reply recipients ---
        recipient.setReplyRecipient(sender);
        sender.setReplyRecipient(recipient);

        // --- AFK notice to sender ---
        if (afk) {
            final String afkMsg = recipientUser.getAfkMessage();
            if (afkMsg != null) {
                final TagResolver afkResolver = TagResolver.builder()
                        .resolver(ctx.resolver)
                        .resolver(Placeholder.unparsed("afk-message", afkMsg))
                        .build();
                sendFmt(sender, config.getString("msg-afk-with-message", "<gray><recipient></gray> <dark_purple>is currently AFK and may not respond: <afk-message></dark_purple>"), afkResolver);
            } else {
                sendFmt(sender, config.getString("msg-afk", "<gray><recipient></gray> <dark_purple>is currently AFK and may not respond."), ctx.resolver);
            }
        }

        // --- Deliver to sender ---
        sendFmt(sender, config.getString("msg-format-sender", "<yellow>(To <recipient>)</yellow> <yellow><message></yellow>"), ctx.resolver);

        // --- Social spy ---
        if (ess.getSettings().isSocialSpyMessages()
                && senderUser != null && !senderUser.isAuthorized("essentials.chat.spy.exempt")
                && recipientUser != null && !recipientUser.isAuthorized("essentials.chat.spy.exempt")) {
            final boolean muted = senderUser.isMuted() && ess.getSettings().getSocialSpyListenMutedPlayers();
            final String spyKey = muted ? "msg-social-spy-muted" : "msg-social-spy";
            final String spyDefault = muted
                    ? "<white>[<gold>SS</gold>]</white> <gray>(muted)</gray> <gold>[<red><spy-sender></red> <gray>-></gray> <red><spy-recipient></red>]</gold> <gray><message>"
                    : "<white>[<gold>SS</gold>]</white> <gold>[<red><spy-sender></red> <gray>-></gray> <red><spy-recipient></red>]</gold> <gray><message>";
            final String spyTemplate = config.getString(spyKey, spyDefault);
            for (User onlineUser : ess.getOnlineUsers()) {
                if (onlineUser.isSocialSpyEnabled()
                        && !onlineUser.equals(senderUser)
                        && !onlineUser.equals(recipientUser)) {
                    sendFmt(onlineUser, spyTemplate, ctx.spyResolver);
                }
            }
        }

        firePost(sender, recipient, message, afk ? IMessageRecipient.MessageResponse.SUCCESS_BUT_AFK : IMessageRecipient.MessageResponse.SUCCESS);
    }

    private void firePost(IMessageRecipient sender, IMessageRecipient recipient, String message, IMessageRecipient.MessageResponse response) {
        ess.getServer().getPluginManager().callEvent(new PrivateMessageSentEvent(sender, recipient, message, response));
    }
}
