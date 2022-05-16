package me.mindustry.leaderboard.service;

import arc.util.*;
import java.util.*;
import me.mindustry.leaderboard.model.*;
import me.mindustry.leaderboard.repository.*;
import mindustry.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

public class SimpleLeaderboardService implements LeaderboardService {

  protected final Leaderboard leaderboard;

  SimpleLeaderboardService(final @NotNull Leaderboard leaderboard) {
    this.leaderboard = leaderboard;
  }

  @Override
  public long getPoints(final @NotNull String uuid) {
    return leaderboard.findPlayerByUuid(uuid).map(LeaderboardPlayer::getPoints).orElse(0L);
  }

  @Override
  public void grantPoints(final @NotNull String uuid, final @NotNull LeaderboardPoints points) {
    final var player = leaderboard.findPlayerByUuid(uuid).orElseGet(() -> LeaderboardPlayer.of(uuid));
    player.addPoints(points);
    leaderboard.savePlayer(player);
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
  public long getRank(final @NotNull String uuid) {
    if (!leaderboard.existsPlayerByUuid(uuid)) {
      leaderboard.savePlayer(LeaderboardPlayer.of(uuid));
    }
    final var players = leaderboard.findAllPlayers().iterator();
    var rank = 1;
    while (players.hasNext()) {
      if (players.next().getUuid().equals(uuid)) return rank;
      rank++;
    }
    throw new IllegalStateException();
  }

  @Override
  public void showLeaderboard(final @NotNull Player player) {
    final var builder = new StringBuilder().append("[yellow]Leader Board:[]");
    final var players = leaderboard.findAllPlayers().iterator();
    var rank = 1;

    while (players.hasNext() && rank <= 10) {
      final var leaderboardPlayer = players.next();
      final var name = Optional
        .ofNullable(Groups.player.find(p -> p.uuid().equals(leaderboardPlayer.getUuid())))
        .map(Player::name)
        .orElseGet(() -> Vars.netServer.admins.getInfo(leaderboardPlayer.getUuid()).lastName);

      builder.append("\n#").append(rank++);
      builder.append(" [white]: ").append(name).append(" - ").append(leaderboardPlayer.getPoints());
    }

    Call.infoToast(player.con, builder.toString(), 10f);
  }
}
