package net.greenfieldmc.greenbot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import net.greenfieldmc.greenbot.Config;
import net.greenfieldmc.greenbot.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArchiveCommand extends AbstractCommand {

    private final Plugin plugin;
    private final Config config;

    public ArchiveCommand(Plugin plugin, Config config) {
        super("Archive a channel", "archive");

        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        var invoker = event.getInteraction().getMember();
        if (invoker.isEmpty()) {
            Bukkit.getLogger().warning("'" + getName() + "' was ran by an unknown member");
            return Mono.empty();
        }

        var options = event.getInteraction().getCommandInteraction().orElse(null);
        if (options == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the command interaction."));

        var guild = event.getClient().getGuildById(Snowflake.of(config.getGuildId())).block();
        if (guild == null) {
            invoker.get().getPrivateChannel().doOnSuccess(c -> c.createMessage("Unable to find the guild with the ID: " + config.getGuildId()).block()).doOnError(e -> Bukkit.getLogger().severe(e.getMessage())).subscribe();
            return Mono.empty();
        }

        List<PermissionOverwrite> permissionOverwriteList = new ArrayList<>(config.getFailureMessageAllowedRoles().stream().map(rid -> PermissionOverwrite.forRole(Snowflake.of(rid), PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.VIEW_CHANNEL), PermissionSet.all())).toList());
        permissionOverwriteList.add(PermissionOverwrite.forRole(Snowflake.of(config.getGuildId()), PermissionSet.none(), PermissionSet.all()));

        var channel = event.getInteraction().getChannel().block();
        channel.getRestChannel().modify(ChannelModifyRequest.builder()
                .parentId(Possible.of(Optional.of(config.getArchiveCategoryId() + "")))
                .permissionOverwrites(permissionOverwriteList.stream().map(PermissionOverwrite::getData).collect(Collectors.toList()))
                .build(), "Archive channel").block();

        return event.reply().withEmbeds(Util.goodEmbed("Archived Channel", "This channel has been archived."));
    }

    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .build();
    }
}
