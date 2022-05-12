package me.mindustry.leaderboard;

import fr.xpdustry.flex.*;
import me.mindustry.leaderboard.service.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

final class LeaderboardFlexExtension implements FlexExtension {

  private final LeaderboardService service;

  LeaderboardFlexExtension(final @NotNull LeaderboardService service) {
    this.service = service;
  }

  @Override
  public @Nullable String handleFlexString(final @NotNull String handler, final @NotNull Player player) {
    if (handler.equals("omega-leaderboard:rank")) {
      return Integer.toString(service.getRank(player));
    } else {
      return null;
    }
  }
}
