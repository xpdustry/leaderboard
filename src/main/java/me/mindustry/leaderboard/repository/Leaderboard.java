package me.mindustry.leaderboard.repository;

import java.io.*;
import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

public interface Leaderboard {

  static @NotNull Leaderboard simple() {
    return new SimpleLeaderboard();
  }

  static @NotNull Leaderboard sqlite(final @NotNull File file) {
    return new SQLLeaderboard(file);
  }

  static @NotNull Leaderboard sql(final @NotNull String url, final @Nullable String username, final @Nullable String password, final @NotNull String table) {
    return new SQLLeaderboard(url, username, password, table);
  }

  static @NotNull Leaderboard sql(final @NotNull String url, final @Nullable String username, final @Nullable String password) {
    return new SQLLeaderboard(url, username, password);
  }

  /**
   * Returns an unmodifiable ordered list of the leaderboard players from the highest score to the lowest.
   */
  @NotNull List<LeaderboardPlayer> getPlayers();

  /**
   * Creates a new leaderboard player.
   * <p>
   * If the leaderboard player already exists for the given player, it's returned.
   *
   * @param uuid the uuid of the player
   * @return the leaderboard player
   */
  @NotNull LeaderboardPlayer addPlayer(final @NotNull String uuid);

  /**
   * Searches for the uuid of a leaderboard player.
   *
   * @param uuid the uuid of the leaderboard player
   * @return the leaderboard player if it exists, otherwise null
   */
  @Nullable LeaderboardPlayer getPlayer(final @NotNull String uuid);

  boolean hasPlayer(final @NotNull String uuid);

  void removePlayer(final @NotNull String uuid);

  /**
   * Updates the leaderboard player.
   * <p>
   * If the player does not exist, it is created instead.
   *
   * @param player the leaderboard player
   */
  void updatePlayer(final @NotNull LeaderboardPlayer player);

  /**
   * Resets the leaderboard.
   */
  void reset();

  /**
   * Returns the number of leaderboard players.
   */
  int getSize();
}
