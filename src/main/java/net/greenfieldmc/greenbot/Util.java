package net.greenfieldmc.greenbot;

import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Util {

    public static final Color ERROR = Color.CINNABAR;
    public static final Color WARNING = Color.of(227, 181, 14);
    public static final Color OK = Color.of(45, 120, 38);

    public static final Color NEUTRAL  = Color.HOKI;

    public static EmbedCreateSpec embed(String title, String description, Color color) {
        return EmbedCreateSpec.builder()
                        .color(color)
                        .title(title)
                        .description(description)
                        .build();
    }

    public static EmbedCreateSpec errorEmbed(String description) {
        return embed("Error", description, ERROR);
    }

    public static EmbedCreateSpec warningEmbed(String title, String description) {
        return embed(title, description, WARNING);
    }

    public static EmbedCreateSpec warningEmbed(String description) {
        return embed("Warning", description, WARNING);
    }

    public static EmbedCreateSpec goodEmbed(String title, String description) {
        return embed(title, description, OK);
    }

    public static CompletableFuture<UUID> getUUIDFromUsername(String username) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                .GET()
                .setHeader("Content-Type", "application/json")
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(body -> {
            var data = new JsonParser().parse(body.body());
            if (data == null || data instanceof JsonNull) return null;
            UUID userId;
            try {
                userId =  UUID.fromString(data.getAsJsonObject().get("id").getAsString().replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5"));
            } catch (IllegalArgumentException e) {
                return null;
            }
            return userId;
        });
    }

}
