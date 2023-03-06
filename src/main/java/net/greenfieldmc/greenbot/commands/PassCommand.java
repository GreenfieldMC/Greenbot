package net.greenfieldmc.greenbot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.MessageCreateSpec;
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

import java.util.concurrent.ExecutionException;

public class PassCommand extends AbstractCommand {

    private final Plugin plugin;
    private final Config config;
    private final FailureConfig failures;

    public PassCommand(Plugin plugin, Config config, FailureConfig failures) {
        super("Mark a user as having their application accepted", "pass");
        this.plugin = plugin;
        this.config = config;
        this.failures = failures;
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

        var passedUser = options.getOption("discord_user").flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asUser).orElse(null);
        if (passedUser == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));

        var user = passedUser.block();
        if (user == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));

        var guild = event.getClient().getGuildById(Snowflake.of(config.getGuildId())).block();
        if (guild == null) {
            invoker.get().getPrivateChannel().doOnSuccess(c -> c.createMessage("Unable to find the guild with the ID: " + config.getGuildId()).block()).doOnError(e -> Bukkit.getLogger().severe(e.getMessage())).subscribe();
            return Mono.empty();
        }

        var testerChannel = guild.getChannelById(Snowflake.of(config.getTestingChannelId())).block();
        if (testerChannel == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the testing channel with ID: " + config.getTestingChannelId()));

        //get the user IGN specified
        var ign = options.getOption("minecraft_ign").flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).orElse(null);

        //If no ign is specified, we assume the user may not have had a java edition account when applying but expressed intent to create one later?
        //Otherwise, if it is specified, we need to check if the user exists. If the user does not exist, send error to sender, otherwise, send welcome message to new user and set rank
        if (ign == null) {
            var member = guild.getMemberById(user.getId()).block();
            member.addRole(Snowflake.of(config.getTestingRankId())).block();
            testerChannel.getRestChannel().createMessage(MessageCreateSpec.builder()
                    .content(user.getMention())
                    .addEmbed(Util.warningEmbed(
                            "Your application has been approved!",
                            "However, " + user.getMention() + ", we could not find your IGN, so you are not yet whitelisted. Please message an administrator your Minecraft Java edition account name. Once you are whitelisted, please follow the next steps in the pinned messages!"))
                    .build().asRequest()).block();
            removeFailureChannel(guild, user.getId().asLong());
            return event.reply().withEmbeds(Util.warningEmbed("Application Approved [no IGN specified]", "No IGN was specified for that user. Check with this member to get their Java edition IGN, then run the pass command again with their IGN."));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    var uuid = Util.getUUIDFromUsername(ign).get();
                    if (uuid == null) {
                        event.reply().withEmbeds(Util.errorEmbed("No Minecraft user was found with that username.")).block();
                    } else {
                        var member = guild.getMemberById(user.getId()).block();
                        member.addRole(Snowflake.of(config.getTestingRankId())).block();
                        Bukkit.getOfflinePlayer(uuid).setWhitelisted(true);
                        testerChannel.getRestChannel().createMessage(MessageCreateSpec.builder()
                                .content(user.getMention())
                                .addEmbed(Util.warningEmbed(
                                        "Your application has been approved!",
                                        "You should be whitelisted! Please follow the next steps in the pinned messages!"))
                                .build().asRequest()).block();
                        event.reply().withEmbeds(Util.goodEmbed("Application Approved", user.getMention() + ", ``" + ign + "``, has been whitelisted!")).block();
                        removeFailureChannel(guild, user.getId().asLong());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return Mono.empty();
    }

    private void removeFailureChannel(Guild guild, long discordId) {
        if (failures.hasFailureChannel(discordId)) {
            var channel = guild.getChannelById(Snowflake.of(failures.getFailureChannel(discordId).channelId())).block();
            channel.delete("Delete old failed application channel.").block();
        }
    }

    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("discord_user")
                        .description("The discord user who has passed application review.")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("minecraft_ign")
                        .description("The Minecraft username of the user who has passed application review.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();

    }
}
