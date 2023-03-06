package net.greenfieldmc.greenbot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import net.greenfieldmc.greenbot.Config;
import net.greenfieldmc.greenbot.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

public class MacInstallCommand extends AbstractCommand {

    private final Plugin plugin;
    private final Config config;

    public MacInstallCommand(Plugin plugin, Config config) {
        super("How to install Greenfield on Mac", "macinstall");
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

        if (config.getMapVersions().isEmpty()) {
            return event.reply(invoker.get().getMention()).withEmbeds(Util.warningEmbed("Map Versions", "There doesn't seem to be any map versions defined. This might be an error."));
        }

        var options = event.getInteraction().getCommandInteraction().orElse(null);
        if (options == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the command interaction."));

        var runFor = options.getOption("run_for").flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asUser).orElse(null);

        if (runFor != null) {
            if (invoker.get().getRoleIds().stream().map(Snowflake::asLong).noneMatch(id -> config.getRanksAllowedRunForPermission().contains(id))) return event.reply().withEphemeral(true).withEmbeds(Util.errorEmbed("You do not have permission to run this command for other users."));
            var user = runFor.block();
            if (user == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));

            var channel = event.getInteraction().getChannel().block();
            if (channel == null) return event.reply().withEmbeds(Util.errorEmbed("Could not find channel??? Show this to administrators.")).withEphemeral(true);

            channel.createMessage(user.getMention() + " " + config.getMacInstallLink()).block();
            return event.reply().withEphemeral(true).withEmbeds(Util.goodEmbed("Sent Mac Install Video", "Sent Mac install video successfully."));
        }

        return event.reply(invoker.get().getMention() + " " + config.getMacInstallLink()).withEphemeral(true);
    }

    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("run_for")
                        .description("The discord user who needs to see the Mac install tutorial.")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .build();
    }
}
