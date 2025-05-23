package com.yeahjenni.ocetiertagger;

import com.yeahjenni.ocetiertagger.model.GameMode;
import com.yeahjenni.ocetiertagger.model.OCETierPlayer;
import com.yeahjenni.ocetiertagger.model.OCETierPlayer.GameModeTier;
import com.yeahjenni.ocetiertagger.model.PlayerInfo;
import com.yeahjenni.ocetiertagger.debug.DebugLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import com.yeahjenni.ocetiertagger.config.TierTaggerConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import static java.util.Map.entry;

public class TierCache {
    private static final String API_BASE_URL_FORMAT = "https://api.yeahjenni.xyz/%s/player/";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<String, OCETierPlayer> USERNAME_CACHE = new ConcurrentHashMap<>();

    private static final Set<String> NOT_FOUND_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final Map<String, CompletableFuture<OCETierPlayer>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    private static final OCETierPlayer NOT_FOUND_PLACEHOLDER = new OCETierPlayer(
        "not-found", "not-found", 0, 0, "", Collections.emptyMap(), false, false, false
    );

    public static final List<String> GAME_MODES = Arrays.asList(
        "sword", "diamondPot", "netheritePot", "axe", 
        "uhc", "smp", "crystal", "diamondSmp", "cart", "mace"
    );

    private static final Map<String, String> GAME_MODE_DISPLAY = Map.of(
        "sword", "Sword",
        "diamondPot", "Diamond Pot",
        "netheritePot", "Netherite Pot",
        "axe", "Axe",
        "uhc", "UHC",
        "smp", "SMP",
        "crystal", "Crystal",
        "diamondSmp", "Diamond SMP",
        "cart", "Cart",
        "mace", "Mace"
    );


    public static final Map<String, String> GAMEMODE_ICON_CHARS = Map.ofEntries(
            entry("sword", "\uE001"),        
            entry("diamondPot", "\uE002"),   
            entry("netheritePot", "\uE003"), 
            entry("axe", "\uE004"),          
            entry("uhc", "\uE005"),          
            entry("smp", "\uE006"),          
            entry("crystal", "\uE007"),     
            entry("diamondSmp", "\uE008"),  
            entry("cart", "\uE009"),        
            entry("mace", "\uE00A")          
    );

    public static final String OCEANIAS_STAFF_ICON = "\u2604"; 
    public static final String OWNER_ICON = "\uD83D\uDC51"; 
    public static final String AVE_ICON = "\uE00B";

    /**
     * Gets the base API URL based on current config settings
     */
    private static String getApiBaseUrl() {
        TierTaggerConfig config = ocetiertagger.getManager().getConfig();
        String domain = config.isUseDevApi() ? "dev.yeahjenni.xyz" : "api.yeahjenni.xyz";
        return String.format("https://%s/%s/player/", domain, config.getTierlistSource().getApiPath());
    }

    /**
     * Fetch player data from the API by username
     */
    public static CompletableFuture<OCETierPlayer> fetchPlayerByUsername(String username) {
        String lowerUsername = username.toLowerCase();

        if (USERNAME_CACHE.containsKey(lowerUsername)) {
            return CompletableFuture.completedFuture(USERNAME_CACHE.get(lowerUsername));
        }

        if (NOT_FOUND_PLAYERS.contains(lowerUsername)) {
            return CompletableFuture.completedFuture(NOT_FOUND_PLACEHOLDER);
        }

        if (PENDING_REQUESTS.containsKey(lowerUsername)) {
            return PENDING_REQUESTS.get(lowerUsername);
        }

        CompletableFuture<OCETierPlayer> future = new CompletableFuture<>();
        PENDING_REQUESTS.put(lowerUsername, future);

        String apiUrl = getApiBaseUrl() + username;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .GET()
            .build();

        if (ocetiertagger.getManager().getConfig().isDebugMode()) {
            DebugLogger.logApiRequest(apiUrl, "GET", null);
        }

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                int statusCode = response.statusCode();
                String responseBody = response.body();
                
                if (ocetiertagger.getManager().getConfig().isDebugMode()) {
                    DebugLogger.logApiResponse(apiUrl, statusCode, responseBody);
                }
                
                if (statusCode == 200) {
                    OCETierPlayer player = GSON.fromJson(responseBody, OCETierPlayer.class);
                    USERNAME_CACHE.put(lowerUsername, player);
                    return player;
                } else if (statusCode == 404) {
                    NOT_FOUND_PLAYERS.add(lowerUsername);
                    return NOT_FOUND_PLACEHOLDER;
                } else {
                    throw new RuntimeException("Unexpected response code: " + statusCode);
                }
            })
            .whenComplete((result, error) -> {
                PENDING_REQUESTS.remove(lowerUsername);
                if (error != null) {
                    future.completeExceptionally(error);
                } else {
                    future.complete(result);
                }
            });
    }

    /**
     * Get the tier for a specific player and game mode
     */
    public static CompletableFuture<String> getPlayerTier(String username, String gameMode) {
        return fetchPlayerByUsername(username).thenApply(player -> {
            if (player == null || player == NOT_FOUND_PLACEHOLDER) {
                return null;
            }
            OCETierPlayer.GameModeTier tierData = player.gameModes().get(gameMode);
            if (tierData == null || tierData.tier() == null) {
                return null;
            }
            return tierData.tier(); 
        });
    }

    /**
     * Get the cached tier for a specific player and game mode
     */
    public static String getCachedTier(String username, String gameMode) {
        OCETierPlayer player = USERNAME_CACHE.get(username.toLowerCase());
        if (player == null) return null;
        OCETierPlayer.GameModeTier tierData = player.gameModes().get(gameMode);
        if (tierData == null || tierData.tier() == null) return null;
        return tierData.tier(); 
    }

    /**
     * Search for player information by username
     */
    public static CompletableFuture<PlayerInfo> searchPlayer(String username) {
        return fetchPlayerByUsername(username).thenApply(player -> {
            if (player == null || player == NOT_FOUND_PLACEHOLDER) return null;

            Map<String, PlayerInfo.Ranking> rankings = new HashMap<>();
            if (player.gameModes() != null) {
                player.gameModes().forEach((mode, tierData) -> {
                    if (tierData.tier() != null) {
                        // Determine position from tier string
                        int position = 0;
                        String tier = tierData.tier();
                        if (tier.startsWith("LT")) {
                            position = 1;  
                        } else if (tier.startsWith("HT")) {
                            position = 0;
                        }
                        
                        rankings.put(mode, new PlayerInfo.Ranking(
                            tier,
                            Instant.now().getEpochSecond(), 
                            position, 
                            false 
                        ));
                    }
                });
            }

            return new PlayerInfo(
                player.username(),
                player.id(),
                "OCE",
                player.score(),
                player.leaderboardPosition(),
                rankings,
                player.oceaniasStaff(),
                player.owner(),
                player.ave()
            );
        });
    }

    /**
     * Get player information by username
     */
    public static CompletableFuture<PlayerInfo> getPlayerInfo(String username) {
        return searchPlayer(username);
    }

    /**
     * Clear all cached data
     */
    public static void clearCache() {
        USERNAME_CACHE.clear();
        NOT_FOUND_PLAYERS.clear();
        PENDING_REQUESTS.clear();
    }

    /**
     * Get the number of cached players
     */
    public static int getCachedPlayerCount() {
        return USERNAME_CACHE.size();
    }

    /**
     * Get the number of not found players
     */
    public static int getNotFoundCount() {
        return NOT_FOUND_PLAYERS.size();
    }

    /**
     * Get the number of pending requests
     */
    public static int getPendingRequestsCount() {
        return PENDING_REQUESTS.size();
    }

    /**
     * Dump cached player data for debugging
     */
    public static Map<String, OCETierPlayer> dumpCachedPlayers() {
        return new HashMap<>(USERNAME_CACHE);
    }

    /**
     * Dump not found players for debugging
     */
    public static Set<String> dumpNotFoundPlayers() {
        return new HashSet<>(NOT_FOUND_PLAYERS);
    }

    /**
     * Force clear all cached data completely, including not found players
     */
    public static void forceClearAllCache() {
        USERNAME_CACHE.clear();
        NOT_FOUND_PLAYERS.clear();
        PENDING_REQUESTS.clear();
        ocetiertagger.getLogger().info("Force cleared all cache data including not found players");
    }

    /**
     * Find a game mode by its ID
     */
    public static GameMode findMode(String id) {
        String title = GAME_MODE_DISPLAY.getOrDefault(id, id);
        return new GameMode(id, title);
    }

    /**
     * Find the next game mode in the list
     */
    public static String findNextMode(String currentMode) {
        int index = GAME_MODES.indexOf(currentMode);
        if (index == -1) return GAME_MODES.get(0); 
        return GAME_MODES.get((index + 1) % GAME_MODES.size());
    }

    /**
     * Get the display name for a game mode
     */
    public static String getGameModeDisplay(String gameMode) {
        return GAME_MODE_DISPLAY.getOrDefault(gameMode, gameMode);
    }

    public static class TierInfo {
        private final String gameMode;
        private final String tier;
        
        public TierInfo(String gameMode, String tier) {
            this.gameMode = gameMode;
            this.tier = tier;
        }
        
        public String getGameMode() {
            return gameMode;
        }
        
        public String getTier() {
            return tier;
        }
    }

    /**
     * Helper method to determine tier value (higher = better)
     */
    public static int getTierValue(String tier) {
        if (tier == null) return -1;
        
        switch (tier) {
            case "HT1": return 10;
            case "LT1": return 9;
            case "HT2": return 8;
            case "LT2": return 7;
            case "HT3": return 6;
            case "LT3": return 5;
            case "HT4": return 4;
            case "LT4": return 3;
            case "HT5": return 2;
            case "LT5": return 1;
            case "RHT1": return 10; 
            case "RLT1": return 9;
            case "RHT2": return 8;
            case "RLT2": return 7;
            case "RHT3": return 6;
            case "RLT3": return 5;
            case "RHT4": return 4;
            case "RLT4": return 3;
            case "RHT5": return 2;
            case "RLT5": return 1;
            default: return 0;
        }
    }

    /**
     * Get the best tier for a username
     */
    public static TierInfo getBestTier(String username) {
        OCETierPlayer player = USERNAME_CACHE.get(username.toLowerCase());
        if (player == null) return null;

        String bestGameMode = null;
        String bestTier = null;
        int bestValue = -1;

        for (Map.Entry<String, OCETierPlayer.GameModeTier> entry : player.gameModes().entrySet()) {
            String gameMode = entry.getKey();
            OCETierPlayer.GameModeTier tierData = entry.getValue();

            if (tierData == null || tierData.tier() == null) continue;

            String tier = tierData.tier(); 
            int value = getTierValue(tier);

            if (value > bestValue) {
                bestValue = value;
                bestTier = tier;
                bestGameMode = gameMode;
            }
        }

        if (bestTier == null) return null;
        return new TierInfo(bestGameMode, bestTier);
    }

    /**
     * Get the cached player data for a username
     */
    public static OCETierPlayer getPlayerData(String username) {
        return USERNAME_CACHE.get(username.toLowerCase());
    }
}
