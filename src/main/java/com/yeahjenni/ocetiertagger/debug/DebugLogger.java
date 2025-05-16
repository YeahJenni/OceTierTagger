package com.yeahjenni.ocetiertagger.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yeahjenni.ocetiertagger.ocetiertagger;
import com.yeahjenni.ocetiertagger.TierCache;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugLogger {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File DEBUG_DIR = new File("debug/ocetiertagger");
    private static final List<DebugEntry> LOG_ENTRIES = new ArrayList<>();
    private static File currentLogFile;
    
    static {
        if (!DEBUG_DIR.exists()) {
            DEBUG_DIR.mkdirs();
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = sdf.format(new Date());
        currentLogFile = new File(DEBUG_DIR, "debug_log_" + timestamp + ".json");
    }
    
    public static void logApiRequest(String url, String method, String requestBody) {
        if (!ocetiertagger.getManager().getConfig().isDebugMode()) return;
        
        DebugEntry entry = new DebugEntry();
        entry.type = "API_REQUEST";
        entry.timestamp = System.currentTimeMillis();
        entry.url = url;
        entry.method = method;
        entry.requestBody = requestBody;
        
        LOG_ENTRIES.add(entry);
        saveLogToFile();
    }
    
    public static void logApiResponse(String url, int statusCode, String responseBody) {
        if (!ocetiertagger.getManager().getConfig().isDebugMode()) return;
        
        DebugEntry entry = new DebugEntry();
        entry.type = "API_RESPONSE";
        entry.timestamp = System.currentTimeMillis();
        entry.url = url;
        entry.statusCode = statusCode;
        entry.responseBody = responseBody;
        
        LOG_ENTRIES.add(entry);
        saveLogToFile();
    }
    
    public static void logEvent(String eventType, String message) {
        if (!ocetiertagger.getManager().getConfig().isDebugMode()) return;
        
        DebugEntry entry = new DebugEntry();
        entry.type = "EVENT";
        entry.eventType = eventType;
        entry.message = message;
        entry.timestamp = System.currentTimeMillis();
        
        LOG_ENTRIES.add(entry);
        saveLogToFile();
    }
    
    private static synchronized void saveLogToFile() {
        try (FileWriter writer = new FileWriter(currentLogFile)) {
            GSON.toJson(LOG_ENTRIES, writer);
        } catch (IOException e) {
            ocetiertagger.getLogger().error("Failed to save debug log", e);
        }
    }
    
    public static void clearLogs() {
        LOG_ENTRIES.clear();
        saveLogToFile();
    }
    
    public static File dumpCacheToFile() {
        if (!DEBUG_DIR.exists()) {
            DEBUG_DIR.mkdirs();
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = sdf.format(new Date());
        File dumpFile = new File(DEBUG_DIR, "cache_dump_" + timestamp + ".json");
        
        try (FileWriter writer = new FileWriter(dumpFile)) {
            Map<String, Object> dumpData = new HashMap<>();
            
            dumpData.put("timestamp", System.currentTimeMillis());
            dumpData.put("timerSource", ocetiertagger.getManager().getConfig().getTierlistSource().toString());
            dumpData.put("gameMode", ocetiertagger.getManager().getConfig().getGameMode());
            
            Map<String, Object> cacheInfo = new HashMap<>();
            cacheInfo.put("cachedPlayerCount", TierCache.getCachedPlayerCount());
            cacheInfo.put("notFoundCount", TierCache.getNotFoundCount());
            cacheInfo.put("pendingRequestsCount", TierCache.getPendingRequestsCount());
            dumpData.put("cacheInfo", cacheInfo);
            
            dumpData.put("cachedPlayers", TierCache.dumpCachedPlayers());
            dumpData.put("notFoundPlayers", TierCache.dumpNotFoundPlayers());
            
            GSON.toJson(dumpData, writer);
            
            logEvent("CACHE_DUMP", "Cache data dumped to " + dumpFile.getAbsolutePath());
            
            return dumpFile;
        } catch (IOException e) {
            ocetiertagger.getLogger().error("Failed to dump cache to file", e);
            return null;
        }
    }
    
    private static class DebugEntry {
        String type;  
        long timestamp;
        
        String url;
        String method;
        String requestBody;
        
        int statusCode;
        String responseBody;
        
        String eventType;
        String message;
    }
}