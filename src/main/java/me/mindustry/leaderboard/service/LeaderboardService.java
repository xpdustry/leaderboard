package me.mindustry.leaderboard.service;

import me.mindustry.leaderboard.model.*;
import me.mindustry.leaderboard.repository.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

public interface LeaderboardService {

  static @NotNull LeaderboardService simple(final @NotNull Leaderboard leaderboard) {
    return new SimpleLeaderboardService(leaderboard);
  }

  long getPoints(final @NotNull String uuid);

  default long getPoints(final @NotNull Player player) {
    return getPoints(player.uuid());
  }

  void grantPoints(final @NotNull String uuid, final @NotNull LeaderboardPoints points);

  default void grantPoints(final @NotNull Player player, final @NotNull LeaderboardPoints points) {
    grantPoints(player.uuid(), points);
  }

  long getRank(final @NotNull String uuid);

  default long getRank(final @NotNull Player player) {
    return getRank(player.uuid());
  }

  void showLeaderboard(final @NotNull Player player);
}
