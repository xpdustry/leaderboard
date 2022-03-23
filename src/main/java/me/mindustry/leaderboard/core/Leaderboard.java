package me.mindustry.leaderboard.core;

import java.io.*;
import java.util.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

public interface Leaderboard {

  static Leaderboard inMemory() {
    return new InMemoryLeaderboard();
  }

  static Leaderboard indexed() {
    return new IndexedLeaderboard();
  }

  static Leaderboard persistent(final @NotNull File file) {
    return new IndexedLeaderboard(file);
  }

  @NotNull Collection<LeaderboardPlayer> getPlayers();

  @NotNull LeaderboardPlayer getPlayer(final @NotNull Player player);

  boolean hasPlayer(final @NotNull Player player);

  void removePlayer(final @NotNull Player player);

  void reset();

  int getSize();

  /**
   * Returns an ordered subset of the leaderboard.
   *
   * @param num  the page number, begins at 0.
   * @param size the size of the page
   * @return the page of the leaderboard
   */
  @NotNull List<LeaderboardPlayer> getPage(final int num, final int size);

  /**
   * Returns the rank of the player or -1 if its data does not exist in the leaderboard.
   */
  int getRank(final @NotNull Player player);
}
