package net.greenfieldmc.greenbot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import net.greenfieldmc.greenbot.commands.FailCommand;
import net.greenfieldmc.greenbot.commands.PassCommand;
import net.greenfieldmc.greenbot.commands.SlashCommand;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Greenbot {

    public Greenbot(Plugin plugin) throws IOException {
        var config = new Config(plugin);
        var failures = new FailureConfig(plugin);

        GatewayDiscordClient client = DiscordClientBuilder.create(config.getBotToken()).build().login().block();

        if (client == null) throw new RuntimeException("Discord gateway client was null, bot will not run.");

        var restClient = client.getRestClient();
        var commands = new ArrayList<SlashCommand>() {{
            add(new PassCommand(plugin, config, failures));
            add(new FailCommand(plugin, config, failures));
        }};

        client.on(ReadyEvent.class, event ->
            Mono.fromRunnable(() -> {
                final User user = event.getSelf();
                plugin.getLogger().info("Logged in as " + user.getUsername() + "#" + user.getDiscriminator());
            }
        )).subscribe();

        client.on(DisconnectEvent.class, event ->
                Mono.fromRunnable(() -> {
                    var cause = event.getCause();
                    cause.ifPresent(throwable -> plugin.getLogger().warning("Greenbot disconnected for reason [" + throwable.getMessage() + "]"));
                    if (cause.isEmpty()) plugin.getLogger().info("Greenbot disconnected.");
                })
        ).subscribe();

        client.on(TextChannelDeleteEvent.class, event -> Mono.fromRunnable(() -> {
            var channel = event.getChannel();
            var uid = failures.findDiscordIdByChannel(channel.getId().asLong());
            if (uid != -1) failures.removeFailureChannel(uid);
        })).subscribe();

        var id = restClient.getApplicationId().block();
        restClient.getApplicationService().bulkOverwriteGuildApplicationCommand(id, config.getGuildId(), commands.stream().map(SlashCommand::getCommandRequest).collect(Collectors.toList()))
                .doOnNext(cmd -> plugin.getLogger().info("Successfully registered guild command '" + cmd.name() + "'"))
                .doOnError(e -> plugin.getLogger().warning("Unable to register guild command... [" + e.getMessage() + "]"))
                .subscribe();

        client.on(ChatInputInteractionEvent.class, (event) ->
            Flux.fromIterable(commands)
                    .filter(cmd -> cmd.getName().equalsIgnoreCase(event.getCommandName()))
                    .next()
                    .flatMap(cmd -> cmd.handle(event))
        ).then(client.onDisconnect()).subscribe();

        failures.getFailures().forEach((uid, failure) -> {
            if (failure.creationTime() + (1000L * 60 * 60 * 24 * config.getAutoDeleteFailureChannelDays()) > System.currentTimeMillis()) {
                failures.removeFailureChannel(uid);
            }
        });
    }
}
