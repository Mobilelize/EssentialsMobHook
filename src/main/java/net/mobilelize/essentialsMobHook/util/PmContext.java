package net.mobilelize.essentialsMobHook.util;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.messaging.IMessageRecipient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public final class PmContext {

    public final TagResolver resolver;
    public final TagResolver spyResolver;

    private PmContext(TagResolver resolver, TagResolver spyResolver) {
        this.resolver = resolver;
        this.spyResolver = spyResolver;
    }

    public static PmContext resolve(IMessageRecipient sender, IMessageRecipient recipient,
                                    String message, IEssentials ess) {
        final boolean useDisplayNames = ess.getSettings().isSocialSpyDisplayNames();

        final Component senderDisplay = display(sender, ess);
        final Component recipientDisplay = display(recipient, ess);
        final Component spySenderDisplay = useDisplayNames ? senderDisplay : Component.text(sender.getName());
        final Component spyRecipientDisplay = useDisplayNames ? recipientDisplay : Component.text(recipient.getName());

        final TagResolver resolver = TagResolver.builder()
                .resolver(Placeholder.component("sender", senderDisplay))
                .resolver(Placeholder.component("recipient", recipientDisplay))
                .resolver(Placeholder.unparsed("message", message))
                .build();

        final TagResolver spyResolver = TagResolver.builder()
                .resolver(Placeholder.component("spy-sender", spySenderDisplay))
                .resolver(Placeholder.component("spy-recipient", spyRecipientDisplay))
                .resolver(Placeholder.unparsed("message", message))
                .build();

        return new PmContext(resolver, spyResolver);
    }

    private static Component display(IMessageRecipient r, IEssentials ess) {
        final UUID uuid = r.getUUID();
        if (uuid != null) {
            final User user = ess.getUser(uuid);
            if (user != null) {
                return LegacyComponentSerializer.legacySection().deserialize(user.getDisplayName());
            }
        }
        return Component.text(r.getDisplayName());
    }
}
