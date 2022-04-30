package me.mindustry.leaderboard.service;

import arc.util.*;
import me.mindustry.leaderboard.model.*;
import me.mindustry.leaderboard.repository.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

final class SimpleLeaderboardService implements LeaderboardService {

  private final Leaderboard leaderboard;

  SimpleLeaderboardService(final @NotNull Leaderboard leaderboard) {
    this.leaderboard = leaderboard;
  }

  @Override
  public @NotNull Leaderboard getLeaderboard() {
    return leaderboard;
  }

  @Override
  public void grantPoints(final @NotNull Player player, final @NotNull LeaderboardPoints points) {
    grantPoints(player.uuid(), points);
    if (points.isSilent()) return;
    Call.warningToast(
      player.con(),
      Iconc.power,
      Strings.format(
        "[yellow]@[] [cyan]>[] [@]@[]",
        points.getName(),
        points.getPoints() > 0 ? "green" : "red",
        points.getPoints()
      )
    );
  }

  @Override
  public void grantPoints(final @NotNull String uuid, final @NotNull LeaderboardPoints points) {
    final var player = leaderboard.addPlayer(uuid);
    player.addPoints(points);
    leaderboard.updatePlayer(player);
  }
}
