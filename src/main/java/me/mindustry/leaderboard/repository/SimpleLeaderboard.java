package me.mindustry.leaderboard.repository;

import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

final class SimpleLeaderboard implements Leaderboard {

  private final Map<String, LeaderboardPlayer> players = new HashMap<>();

  SimpleLeaderboard() {
  }

  @Override
  public void savePlayer(final @NotNull LeaderboardPlayer player) {
    players.put(player.getUuid(), player);
  }

  @Override
  public boolean existsPlayerByUuid(final @NotNull String uuid) {
    return players.containsKey(uuid);
  }

  @Override
  public @NotNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NotNull String uuid) {
    return Optional.ofNullable(players.get(uuid));
  }

  @Override
  public @NotNull Iterable<LeaderboardPlayer> findAllPlayers() {
    return players.values().stream()
      .sorted(Comparator.comparingLong(LeaderboardPlayer::getPoints).reversed())
      .toList();
  }

  @Override
  public long countPlayers() {
    return players.size();
  }

  @Override
  public void deletePlayerByUuid(final @NotNull String uuid) {
    players.remove(uuid);
  }

  @Override
  public void deleteAllPlayers() {
    players.clear();
  }
}
