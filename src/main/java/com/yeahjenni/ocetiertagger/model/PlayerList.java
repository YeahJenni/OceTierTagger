package com.yeahjenni.ocetiertagger.model;

import com.google.gson.annotations.SerializedName;
import com.yeahjenni.ocetiertagger.ocetiertagger;
import com.yeahjenni.ocetiertagger.config.TierTaggerConfig;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record PlayerList(List<PlayerInfo> players, List<UUID> unknown, @SerializedName("fetch_unknown") @Nullable Boolean fetchUnknown) {
    public static CompletableFuture<PlayerList> get(HttpClient client) {
        TierTaggerConfig config = ocetiertagger.getManager().getConfig();
        String apiPath = config.getTierlistSource().getApiPath();
        String endpoint = "https://api.yeahjenni.xyz/" + apiPath + "/players";

        final HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint)).GET().build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> ocetiertagger.GSON.fromJson(s, PlayerList.class))
                .whenComplete((i, t) -> {
                    if (t != null) ocetiertagger.getLogger().warn("Error fetching tier list", t);
                });
    }
}
