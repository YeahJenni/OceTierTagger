package com.yeahjenni.ocetiertagger.config;

import com.yeahjenni.ocetiertagger.TierCache;
import com.google.gson.internal.LinkedTreeMap;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TierTaggerConfig implements Serializable, IConfig {
    private boolean enabled = true;
    private String gameMode = "crystal"; 
    private boolean showRetired = true;
    private boolean showHighestTier = false; 
    private Statistic shownStatistic = Statistic.TIER;
    private boolean showBestTierFirst = false;
    private boolean showLeaderboardPosition = false; 
    private NametagPosition nametagPosition = NametagPosition.LEFT;
    private int retiredColor = 0xa2d6ff;
    private LinkedTreeMap<String, Integer> tierColors = defaultColors();
    private TierlistSource tierlistSource = TierlistSource.OCETIERS;
    
    private boolean debugMode = false;
    private boolean useDevApi = false;
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGameMode() {
        if (!TierCache.GAME_MODES.contains(this.gameMode)) {
            this.gameMode = "crystal"; 
        }
        return this.gameMode;
    }
    
    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
    
    public boolean isShowRetired() {
        return showRetired;
    }
    
    public void setShowRetired(Boolean showRetired) {
        this.showRetired = showRetired;
    }
    
    public boolean isShowHighestTier() {
        return showHighestTier;
    }
    
    public void setShowHighestTier(boolean showHighestTier) {
        this.showHighestTier = showHighestTier;
    }
    
    public Statistic getShownStatistic() {
        return shownStatistic;
    }
    
    public void setShownStatistic(Statistic statistic) {
        this.shownStatistic = statistic;
    }
    
    public NametagPosition getNametagPosition() {
        return nametagPosition;
    }
    
    public void setNametagPosition(NametagPosition position) {
        this.nametagPosition = position;
    }
    
    public int getRetiredColor() {
        return retiredColor;
    }
    
    public void setRetiredColor(int color) {
        this.retiredColor = color;
    }
    
    public LinkedTreeMap<String, Integer> getTierColors() {
        return tierColors;
    }
    
    public TierlistSource getTierlistSource() {
        return tierlistSource;
    }
    
    public void setTierlistSource(TierlistSource tierlistSource) {
        this.tierlistSource = tierlistSource;
    }
    
    public boolean isShowLeaderboardPosition() {
        return showLeaderboardPosition;
    }
    
    public void setShowLeaderboardPosition(boolean showLeaderboardPosition) {
        this.showLeaderboardPosition = showLeaderboardPosition;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public boolean isUseDevApi() {
        return useDevApi;
    }
    
    public void setUseDevApi(boolean useDevApi) {
        this.useDevApi = useDevApi;
    }
    
    private static LinkedTreeMap<String, Integer> defaultColors() {
        LinkedTreeMap<String, Integer> colors = new LinkedTreeMap<>();
        colors.put("LT1", 0xFF000080); 
        colors.put("LT2", 0xFF0000FF); 
        colors.put("LT3", 0xFF4169E1);
        colors.put("LT4", 0xFF1E90FF); 
        colors.put("LT5", 0xFF87CEEB);

        colors.put("HT1", 0xFFFF0000); 
        colors.put("HT2", 0xFFFF6600);
        colors.put("HT3", 0xFFFFFF00);
        colors.put("HT4", 0xFFAAFF00); 
        colors.put("HT5", 0xFF32CD32); 

        return colors;
    }
    
    public enum Statistic {
        TIER    
    }
    
    public enum NametagPosition {
        LEFT,
        RIGHT
    }
    
    public enum TierlistSource {
        OCETIERS("ocetiers.com"),
        MCTIERS("mctiers.com"),
        MCTIERSIO("mctiers.io");
        
        private final String displayName;
        
        TierlistSource(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getApiPath() {
            return name().toLowerCase();
        }
    }
}
