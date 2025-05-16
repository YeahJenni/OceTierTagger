package com.yeahjenni.ocetiertagger.config;

import com.yeahjenni.ocetiertagger.TierCache;
import com.yeahjenni.ocetiertagger.ocetiertagger;
import com.yeahjenni.ocetiertagger.tierlist.PlayerSearchScreen;
import com.yeahjenni.ocetiertagger.debug.DebugLogger;
import com.yeahjenni.ocetiertagger.Ukutils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.text.Text;
import net.uku3lig.ukulib.config.option.*;
import net.uku3lig.ukulib.config.option.widget.ButtonTab;
import net.uku3lig.ukulib.config.screen.TabbedConfigScreen;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TTConfigScreen extends TabbedConfigScreen<TierTaggerConfig> {
    public TTConfigScreen(Screen parent) {
        super("ocetiertagger Config", parent, ocetiertagger.getManager());
    }

    @Override
    protected Tab[] getTabs(TierTaggerConfig config) {
        if (config.isDebugMode()) {
            return new Tab[]{new MainSettingsTab(), new ColorsTab(), new DebugTab()};
        } else {
            return new Tab[]{new MainSettingsTab(), new ColorsTab()};
        }
    }

    public class MainSettingsTab extends ButtonTab<TierTaggerConfig> {
        public MainSettingsTab() {
            super("ocetiertagger.config", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            List<WidgetCreator> widgets = new ArrayList<>(Arrays.asList(
                    CyclingOption.ofBoolean("ocetiertagger.config.enabled", config.isEnabled(), config::setEnabled),
                    new CyclingOption<>("ocetiertagger.config.gamemode", TierCache.GAME_MODES, config.getGameMode(),
                            config::setGameMode, mode -> Text.literal(TierCache.getGameModeDisplay(mode))),
                    CyclingOption.ofBoolean("ocetiertagger.config.retired", config.isShowRetired(), config::setShowRetired),
                    CyclingOption.ofBoolean("ocetiertagger.config.besttier", config.isShowBestTierFirst(), config::setShowBestTierFirst),
                    CyclingOption.ofBoolean("ocetiertagger.config.showleaderboard", config.isShowLeaderboardPosition(), config::setShowLeaderboardPosition),
                    new CyclingOption<>("ocetiertagger.config.statistic", 
                            Arrays.asList(TierTaggerConfig.Statistic.values()),
                            config.getShownStatistic(), 
                            config::setShownStatistic,
                            stat -> Text.literal(stat.name())),
                    new CyclingOption<>("ocetiertagger.config.nametagposition",
                            Arrays.asList(TierTaggerConfig.NametagPosition.values()),
                            config.getNametagPosition(),
                            config::setNametagPosition,
                            pos -> Text.literal(pos.name())),
                    new CyclingOption<>("ocetiertagger.config.tierlistsource",
                            Arrays.asList(TierTaggerConfig.TierlistSource.values()),
                            config.getTierlistSource(),
                            config::setTierlistSource,
                            e -> Text.literal(e.getDisplayName())),
                    new SimpleButton("ocetiertagger.clear", b -> TierCache.clearCache()),
                    new ScreenOpenButton("ocetiertagger.config.search", PlayerSearchScreen::new),
                    
                    new CyclingOption<>("ocetiertagger.config.debug.mode", 
                            Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                            config.isDebugMode(),
                            val -> {
                                config.setDebugMode(val);
                                MinecraftClient.getInstance().setScreen(new TTConfigScreen(parent));
                            },
                            val -> Text.literal(val ? "Enabled" : "Disabled"))
            ));
            
            return widgets.toArray(WidgetCreator[]::new);
        }
    }

    public class ColorsTab extends ButtonTab<TierTaggerConfig> {
        protected ColorsTab() {
            super("ocetiertagger.colors", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            // i genuinely don't understand but chaining the calls just EXPLODES????
            Comparator<Map.Entry<String, Integer>> comparator = Comparator.comparing(e -> e.getKey().charAt(2));
            comparator = comparator.thenComparing(e -> e.getKey().charAt(0));

            List<ColorOption> tiers = config.getTierColors().entrySet().stream()
                    .sorted(comparator)
                    .map(e -> new ColorOption(e.getKey(), e.getValue(), val -> config.getTierColors().put(e.getKey(), val)))
                    .collect(Collectors.toList());

            tiers.add(new ColorOption("ocetiertagger.colors.retired", config.getRetiredColor(), config::setRetiredColor));

            return tiers.toArray(WidgetCreator[]::new);
        }
    }

    public class DebugTab extends ButtonTab<TierTaggerConfig> {
        public DebugTab() {
            super("ocetiertagger.debug", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            return new WidgetCreator[] {
                    CyclingOption.ofBoolean("ocetiertagger.debug.use_dev_api", 
                            config.isUseDevApi(), 
                            val -> {
                                config.setUseDevApi(val);
                                TierCache.clearCache(); 
                            }),
                    new SimpleButton("ocetiertagger.debug.clear_logs", b -> {
                        DebugLogger.clearLogs();
                        Ukutils.sendToast(
                                Text.literal("Debug logs cleared"),
                                Text.literal("Debug logs have been cleared.")
                        );
                    }),
                    new SimpleButton("ocetiertagger.debug.open_logs_folder", b -> {
                        try {
                            net.minecraft.util.Util.getOperatingSystem().open(
                                    new File("debug/ocetiertagger"));
                        } catch (Exception e) {
                            ocetiertagger.getLogger().error("Failed to open debug logs folder", e);
                        }
                    }),
                    new SimpleButton("ocetiertagger.debug.dump_cache", b -> {
                        File dumpFile = DebugLogger.dumpCacheToFile();
                        if (dumpFile != null) {
                            Ukutils.sendToast(
                                    Text.literal("Cache Dumped"),
                                    Text.literal("Cache data saved to " + dumpFile.getName())
                            );
                        } else {
                            Ukutils.sendToast(
                                    Text.literal("Cache Dump Failed"),
                                    Text.literal("Failed to create dump file")
                            );
                        }
                    }),
                    new SimpleButton("ocetiertagger.debug.force_clear", b -> {
                        TierCache.forceClearAllCache();
                        Ukutils.sendToast(
                                Text.literal("Cache Forcefully Cleared"),
                                Text.literal("All cache data has been cleared")
                        );
                    })
            };
        }
    }
}
