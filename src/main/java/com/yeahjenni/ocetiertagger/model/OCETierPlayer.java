package com.yeahjenni.ocetiertagger.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Map;

public record OCETierPlayer(
    String id,
    String username,
    int score,
    @SerializedName("leaderboard_position") int leaderboardPosition,
    @SerializedName("last_updated") String lastUpdated,
    @SerializedName("gameModes") Map<String, GameModeTier> gameModes,
    @SerializedName("oceaniasStaff") boolean oceaniasStaff
) {
    public record GameModeTier(String tier, boolean isLT) {}
}