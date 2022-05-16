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
   * Saves the leaderboard player.
   *
   * @param player the leaderboard player
   */
  void savePlayer(final @NotNull LeaderboardPlayer player);

  boolean existsPlayerByUuid(final @NotNull String uuid);

  /**
   * Searches for the uuid of a leaderboard player.
   *
   * @param uuid the uuid of the leaderboard player
   * @return an optional encapsulating the leaderboard player
   */
  @NotNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NotNull String uuid);

  /**
   * Returns an ordered iterable of the leaderboard players from the highest score to the lowest.
   */
  @NotNull Iterable<LeaderboardPlayer> findAllPlayers();

  long countPlayers();

  void deletePlayerByUuid(final @NotNull String uuid);

  void deleteAllPlayers();
}
