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
import net.greenfieldmc.greenbot.Config;
import net.greenfieldmc.greenbot.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class BetaPackCommand extends AbstractCommand {

    private final Plugin plugin;
    private final Config config;

    public BetaPackCommand(Plugin plugin, Config config) {
        super("Instructions on how to install the Beta Resourcepack", "betapack");
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
        var mention = invoker.get().getMention();
        if (runFor != null) {
            if (invoker.get().getRoleIds().stream().map(Snowflake::asLong).noneMatch(id -> config.getRanksAllowedRunForPermission().contains(id))) return event.reply().withEphemeral(true).withEmbeds(Util.errorEmbed("You do not have permission to run this command for other users."));
            var user = runFor.block();
            if (user == null) return event.reply().withEmbeds(Util.errorEmbed("Unable to find the user specified."));
            mention = user.getMention();
        }

        var channel = event.getInteraction().getChannel().block();
        if (channel == null) return event.reply().withEmbeds(Util.errorEmbed("Could not find channel??? Show this to administrators.")).withEphemeral(true);

        var msg1 = channel.createMessage(MessageCreateSpec.builder()
                .content("This is an *EXPERIMENTAL* version of the Resource Pack. This means the textures are subject to change before the final release without notice.")
                .addEmbed(EmbedCreateSpec.builder()
                        .title("Instructions for a one-time download")
                        .fields(getFieldsFromList(config.getBetapackOneTimeDownloadSteps()))
                        .build())
                .build()).block();
        var msg2 = channel.createMessage(MessageCreateSpec.builder()
                .content("Alternatively, you can use the Git platform to get continuous updates of the pack without having to redo the one-time-download steps.")
                .addEmbed(EmbedCreateSpec.builder()
                        .title("Instructions for git-based updates")
                        .fields(getFieldsFromList(config.getBetapackGitSetupSteps()))
                        .build())
//                .messageReference(msg1.getRestMessage().getId())
                .build()).block();
        channel.createMessage(MessageCreateSpec.builder()
                .content("If you choose the Git route, you should only need to do the previous steps *one* time. Any other time you want to pull an update of the resource pack, do these steps below.")
                .addEmbed(EmbedCreateSpec.builder()
                        .title("Steps to update the pack (via Git)")
                        .fields(getFieldsFromList(config.getBetapackGitUpdateSteps()))
                        .build())
//                .messageReference(msg2.getRestMessage().getId())
                .build()).block();

        return event.reply(mention);

    }

    private List<EmbedCreateFields.Field> getFieldsFromList(List<String> strings) {
        var steps = new ArrayList<EmbedCreateFields.Field>();
        int i = 1;
        for (var step : strings) {
            steps.add(EmbedCreateFields.Field.of(" ", "__***" + (i++) + ".)***__ " + step, false));
        }
        return steps;
    }

    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("run_for")
                        .description("The discord user who wants to install the beta resource pack.")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(false)
                        .build())
                .build();
    }
}
