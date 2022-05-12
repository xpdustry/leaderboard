package me.mindustry.leaderboard.service;

import me.mindustry.leaderboard.model.*;
import me.mindustry.leaderboard.repository.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

public interface LeaderboardService {

  static @NotNull LeaderboardService simple(final @NotNull Leaderboard leaderboard) {
    return new SimpleLeaderboardService(leaderboard);
  }

  @NotNull Leaderboard getLeaderboard();

  void grantPoints(final @NotNull Player player, final @NotNull LeaderboardPoints points);

  void grantPoints(final @NotNull String uuid, final @NotNull LeaderboardPoints points);

  /**
   * Returns the rank of a leaderboard player.
   *
   * @param uuid the uuid of the leaderboard player
   * @return the rank of the leaderboard player
   */
  default int getRank(final @NotNull String uuid) {
    if (!getLeaderboard().hasPlayer(uuid)) getLeaderboard().addPlayer(uuid);
    final var sorted = getLeaderboard().getPlayers();
    for (int i = 0; i < sorted.size(); i++) {
      if (sorted.get(i).getUuid().equals(uuid)) return i + 1;
    }
    throw new IllegalStateException();
  }

  default int getRank(final @NotNull Player player) {
    return getRank(player.uuid());
  }
}
