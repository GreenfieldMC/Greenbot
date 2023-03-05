package net.greenfieldmc.greenbot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.TextChannelCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import net.greenfieldmc.greenbot.Config;
import net.greenfieldmc.greenbot.FailureConfig;
import net.greenfieldmc.greenbot.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FailCommand extends AbstractCommand {

    private final Plugin plugin;
    private final Config config;
    private final FailureConfig failures;

    public FailCommand(Plugin plugin, Config config, FailureConfig failures) {
        super("Fail a builder application.", "fail");

        this.plugin = plugin;
        this.config = config;
        this.failures = failures;
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        var invoker = event.getInteraction().getMember();
        if (invoker.isEmpty()) {
            Bukkit.getLogger().warning("'fail' was ran by an unknown member");
            return Mono.empty();
        }

        var options = event.getInteraction().getCommandInteraction().orElse(null);
        if (options == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the command interaction."));

        var failedUser = options.getOption("discord_user").flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asUser).orElse(null);
        if (failedUser == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));

        var message = options.getOption("failure_message").flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).orElse(null);

        var user = failedUser.block();
        if (user == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));

        var guild = event.getClient().getGuildById(Snowflake.of(config.getGuildId())).block();
        if (guild == null) {
            invoker.get().getPrivateChannel().doOnSuccess(c -> c.createMessage("Unable to find the guild with the ID: " + config.getGuildId()).block()).doOnError(e -> Bukkit.getLogger().severe(e.getMessage())).subscribe();
            return Mono.empty();
        }

        boolean deletedExisting = false;
        if (failures.hasFailureChannel(user.getId().asLong())) {
            var channel = guild.getChannelById(Snowflake.of(failures.getFailureChannel(user.getId().asLong()).channelId())).block();
            channel.delete("Delete old failed application channel.").block();
            deletedExisting = true;
            //if the channel currently exists, delete it and make a new channel and send the new message
        }

        List<PermissionOverwrite> permissionOverwriteList = new ArrayList<>(config.getFailureMessageAllowedRoles().stream().map(rid -> PermissionOverwrite.forRole(Snowflake.of(rid), PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.VIEW_CHANNEL), PermissionSet.all())).toList());
        permissionOverwriteList.add(PermissionOverwrite.forMember(user.getId(), PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.VIEW_CHANNEL), PermissionSet.all()));
        permissionOverwriteList.add(PermissionOverwrite.forRole(Snowflake.of(config.getGuildId()), PermissionSet.none(), PermissionSet.all()));

        var channel = guild.createTextChannel(TextChannelCreateSpec.builder()
                .parentId(Snowflake.of(config.getFailureMessagesCategory()))
                .name("App-" + user.getUsername() + user.getDiscriminator())
                .permissionOverwrites(permissionOverwriteList)
                .reason("Create new failed application channel.")
                .build()).block();

        channel.createMessage(MessageCreateSpec.builder()
                .addEmbed(Util.embed("About your application...", """
                        Thank you for taking your time to apply to be a part of the Greenfield Build Team. Unfortunately, we regret to inform you that your application was not approved. This could've been any of the following issues:
                        
                        1. Your images of previous builds weren't accessible, or did not exist.
                        \t        **If you reapply, make sure we can view your images.**
                        2. Your build quality did not meet our standards.
                        \t        **Get your self familiar with our build styles and standards. We also recommend checking out #building-help-chat for more assistance on your building.**
                        3. Your application may have been poorly written.
                        
                        ${comment}
                        """.replace("${comment}", message == null ? "*No additional comments*" : "Additional comments: \n```" + message + "```"), Util.NEUTRAL).withFooter(EmbedCreateFields.Footer.of("This message will disappear automatically on ", null)).withTimestamp(Instant.now().plus(config.getAutoDeleteFailureChannelDays(), ChronoUnit.DAYS)))
                .content(user.getMention())
                .build()).block();

        failures.addFailureChannel(user.getId().asLong(), channel.getId().asLong());

        return event.reply().withEmbeds(Util.goodEmbed("Application Denied", "Application for " + user.getMention() + " was denied." + (deletedExisting ? " This user already had a channel for a failed application, deleting it and remaking it..." : "")));
    }

    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("discord_user")
                        .description("The discord user who has failed application review.")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("failure_message")
                        .description("The failure message to send to this user.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();
    }
}
