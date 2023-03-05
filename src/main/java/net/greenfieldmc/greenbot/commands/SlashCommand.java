package net.greenfieldmc.greenbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public interface SlashCommand {

    String getName();

    String getDescription();

    Mono<Void> handle(ChatInputInteractionEvent event);

//    Mono<Void> tabComplete(ChatInputAutoCompleteEvent event);

    ApplicationCommandRequest getCommandRequest();

}
