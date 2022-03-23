package me.mindustry.leaderboard.core;

import java.util.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

final class InMemoryLeaderboard implements Leaderboard {

  private final Map<String, LeaderboardPlayer> players = new HashMap<>();

  InMemoryLeaderboard() {
  }

  @Override
  public @NotNull Collection<LeaderboardPlayer> getPlayers() {
    return Collections.unmodifiableCollection(players.values());
  }

  @Override
  public @NotNull LeaderboardPlayer getPlayer(final @NotNull Player player) {
    return players.computeIfAbsent(player.uuid(), k -> LeaderboardPlayer.simple(player));
  }

  @Override
  public boolean hasPlayer(final @NotNull Player player) {
    return players.containsKey(player.uuid());
  }

  @Override
  public void removePlayer(final @NotNull Player player) {
    players.remove(player.uuid());
  }

  @Override
  public void reset() {
    players.clear();
  }

  @Override
  public int getSize() {
    return players.size();
  }

  @Override
  public @NotNull List<LeaderboardPlayer> getPage(final int num, final int size) {
    return players.values().stream()
      .sorted(Comparator.comparingLong(LeaderboardPlayer::getPoints).reversed())
      .skip((long) num * size)
      .limit(size)
      .toList();
  }

  @Override
  public int getRank(final @NotNull Player player) {
    final var all = getPage(0, getSize());
    for (int i = 0; i < all.size(); i++) {
      if (all.get(i).getUuid().equals(player.uuid())) return i;
    }

    return -1;
  }
}
