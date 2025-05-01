package com.yeahjenni.ocetiertagger.config;

import com.yeahjenni.ocetiertagger.TierCache;
import com.yeahjenni.ocetiertagger.ocetiertagger;
import com.yeahjenni.ocetiertagger.tierlist.PlayerSearchScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.text.Text;
import net.uku3lig.ukulib.config.option.*;
import net.uku3lig.ukulib.config.option.widget.ButtonTab;
import net.uku3lig.ukulib.config.screen.TabbedConfigScreen;

import java.util.*;
import java.util.stream.Collectors;

public class TTConfigScreen extends TabbedConfigScreen<TierTaggerConfig> {
    public TTConfigScreen(Screen parent) {
        super("ocetiertagger Config", parent, ocetiertagger.getManager());
    }

    @Override
    protected Tab[] getTabs(TierTaggerConfig config) {
        return new Tab[]{new MainSettingsTab(), new ColorsTab()};
    }

    public class MainSettingsTab extends ButtonTab<TierTaggerConfig> {
        public MainSettingsTab() {
            super("ocetiertagger.config", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            return new WidgetCreator[]{
                    CyclingOption.ofBoolean("ocetiertagger.config.enabled", config.isEnabled(), config::setEnabled),
                    new CyclingOption<>("ocetiertagger.config.gamemode", TierCache.GAME_MODES, config.getGameMode(),
                            config::setGameMode, mode -> Text.literal(TierCache.getGameModeDisplay(mode))),
                    CyclingOption.ofBoolean("ocetiertagger.config.retired", config.isShowRetired(), config::setShowRetired),
                    CyclingOption.ofBoolean("ocetiertagger.config.besttier", config.isShowBestTierFirst(), config::setShowBestTierFirst),
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
                    new ScreenOpenButton("ocetiertagger.config.search", PlayerSearchScreen::new)
            };
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
}
