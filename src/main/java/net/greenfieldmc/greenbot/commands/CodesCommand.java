package net.greenfieldmc.greenbot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import net.greenfieldmc.greenbot.CodesConfig;
import net.greenfieldmc.greenbot.Config;
import net.greenfieldmc.greenbot.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class CodesCommand extends AbstractCommand {

    private final Plugin plugin;
    private final Config config;
    private final CodesConfig codesConfig;

    public CodesCommand(Plugin plugin, Config config, CodesConfig codesConfig) {
        super("List the current build codes Greenfield follows.", "codes");
        this.plugin = plugin;
        this.config = config;
        this.codesConfig = codesConfig;
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        var invoker = event.getInteraction().getMember();
        if (invoker.isEmpty()) {
            Bukkit.getLogger().warning("'" + getName() + "' was ran by an unknown member");
            return Mono.empty();
        }

        if (codesConfig.getCodes().isEmpty()) {
            return event.reply(invoker.get().getMention()).withEmbeds(Util.warningEmbed("Server Build Codes", "There doesn't seem to be any build codes defined. This might be an error."));
        }

        var options = event.getInteraction().getCommandInteraction().orElse(null);
        if (options == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the command interaction."));

        var runFor = options.getOption("run_for").flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asUser).orElse(null);

        List<EmbedCreateFields.Field> codes = new ArrayList<>();

        for (int i = 0; i < codesConfig.getCodes().size(); i++) {
            codes.add(EmbedCreateFields.Field.of(" ", "__***" + (i+1) + ".) ***__ " + codesConfig.getCodes().get(i), false));
        }

        if (runFor != null) {
            if (invoker.get().getRoleIds().stream().map(Snowflake::asLong).noneMatch(id -> config.getRanksAllowedRunForPermission().contains(id))) return event.reply().withEphemeral(true).withEmbeds(Util.errorEmbed("You do not have permission to run this command for other users."));
            var user = runFor.block();
            if (user == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));

            var channel = event.getInteraction().getChannel().block();
            if (channel == null) return event.reply().withEmbeds(Util.errorEmbed("Could not find channel??? Show this to administrators.")).withEphemeral(true);

            channel.createMessage(MessageCreateSpec.builder()
                    .content(user.getMention())
                    .addEmbed(EmbedCreateSpec.builder()
                        .title("Server Build Codes")
                        .color(Util.OK)
                        .fields(codes)
                        .build()).build()).block();
            return event.reply().withEphemeral(true).withEmbeds(Util.goodEmbed("Sent Build Codes", "Build codes successfully sent"));
        }

        return event.reply(invoker.get().getMention()).withEmbeds(EmbedCreateSpec.builder()
                        .title("Server Build Codes")
                        .color(Util.OK)
                        .fields(codes)
                        .build()
                ).withEphemeral(true);
    }

    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("run_for")
                        .description("The discord user who needs to see the build codes.")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .build();
    }
}
