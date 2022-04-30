package me.mindustry.leaderboard.repository;

import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

final class SimpleLeaderboard implements Leaderboard {

  private final Map<String, LeaderboardPlayer> players = new HashMap<>();

  SimpleLeaderboard() {
  }

  @Override
  public @NotNull List<LeaderboardPlayer> getPlayers() {
    return players.values().stream()
      .sorted(Comparator.comparingLong(LeaderboardPlayer::getPoints).reversed())
      .toList();
  }

  @Override
  public @NotNull LeaderboardPlayer addPlayer(final @NotNull String uuid) {
    return players.computeIfAbsent(uuid, LeaderboardPlayer::of);
  }

  @Override
  public @Nullable LeaderboardPlayer getPlayer(final @NotNull String uuid) {
    return players.get(uuid);
  }

  @Override
  public boolean hasPlayer(final @NotNull String uuid) {
    return players.containsKey(uuid);
  }

  @Override
  public void removePlayer(final @NotNull String uuid) {
    players.remove(uuid);
  }

  @Override
  public void updatePlayer(final @NotNull LeaderboardPlayer player) {
  }

  @Override
  public void reset() {
    players.clear();
  }

  @Override
  public int getSize() {
    return players.size();
  }
}
