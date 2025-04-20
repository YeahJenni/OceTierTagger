package com.kevin.tiertagger.config;

import com.kevin.tiertagger.TierCache;
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
    private Statistic shownStatistic = Statistic.TIER;
    private NametagPosition nametagPosition = NametagPosition.LEFT;
    private int retiredColor = 0xa2d6ff;
    private LinkedTreeMap<String, Integer> tierColors = defaultColors();
    
    public String getGameMode() {
        if (!TierCache.GAME_MODES.contains(this.gameMode)) {
            this.gameMode = "crystal"; 
        }
        return this.gameMode;
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
        TIER,    
    }
    
    public enum NametagPosition {
        LEFT,
        RIGHT
    }
}
