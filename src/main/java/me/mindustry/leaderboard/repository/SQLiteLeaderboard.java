package me.mindustry.leaderboard.repository;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.jdbc.*;
import com.j256.ormlite.table.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

final class SQLiteLeaderboard implements Leaderboard {

  private final Dao<LeaderboardPlayer, String> dao;

  SQLiteLeaderboard(final @NotNull File file) {
    try {
      final var connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + file.getAbsolutePath());
      this.dao = DaoManager.createDao(connectionSource, LeaderboardPlayer.class);
      TableUtils.createTableIfNotExists(connectionSource, LeaderboardPlayer.class);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull List<LeaderboardPlayer> getPlayers() {
    try {
      return dao.queryBuilder().orderBy("points", false).query();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull LeaderboardPlayer addPlayer(@NotNull String uuid) {
    try {
      return dao.createIfNotExists(LeaderboardPlayer.of(uuid));
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @Nullable LeaderboardPlayer getPlayer(@NotNull String uuid) {
    try {
      return dao.queryForId(uuid);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasPlayer(@NotNull String uuid) {
    return getPlayer(uuid) != null;
  }

  @Override
  public void removePlayer(@NotNull String uuid) {
    try {
      dao.deleteById(uuid);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void updatePlayer(@NotNull LeaderboardPlayer player) {
    try {
      dao.update(player);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void reset() {
    try {
      dao.deleteBuilder().delete();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getSize() {
    try {
      return (int) dao.countOf();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
