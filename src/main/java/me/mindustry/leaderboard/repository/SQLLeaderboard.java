package me.mindustry.leaderboard.repository;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.field.*;
import com.j256.ormlite.jdbc.*;
import com.j256.ormlite.logger.*;
import com.j256.ormlite.table.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

final class SQLLeaderboard implements Leaderboard {

  private static final String DEFAULT_TABLE = "leaderboard_player";

  static {
    Logger.setGlobalLogLevel(Level.ERROR);
  }

  private final Dao<LeaderboardPlayer, String> dao;

  SQLLeaderboard(final @NotNull String url, final @Nullable String username, final @Nullable String password, final @NotNull String table) {
    try {
      final var source = new JdbcConnectionSource(url, username, password);

      final var uuidField = new DatabaseFieldConfig("uuid");
      uuidField.setId(true);
      uuidField.setCanBeNull(false);

      final var scoreField = new DatabaseFieldConfig("points");
      scoreField.setCanBeNull(false);

      dao = DaoManager.createDao(
        source,
        new DatabaseTableConfig<>(LeaderboardPlayer.class, table, List.of(uuidField, scoreField))
      );

      TableUtils.createTableIfNotExists(source, LeaderboardPlayer.class);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  SQLLeaderboard(final @NotNull String url, final @Nullable String username, final @Nullable String password) {
    this(url, username, password, DEFAULT_TABLE);
  }

  SQLLeaderboard(final @NotNull File file) {
    this("jdbc:sqlite:" + file.getAbsolutePath(), null, null, DEFAULT_TABLE);
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
  public @NotNull LeaderboardPlayer addPlayer(final @NotNull String uuid) {
    try {
      return dao.createIfNotExists(LeaderboardPlayer.of(uuid));
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @Nullable LeaderboardPlayer getPlayer(final @NotNull String uuid) {
    try {
      return dao.queryForId(uuid);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasPlayer(final @NotNull String uuid) {
    return getPlayer(uuid) != null;
  }

  @Override
  public void removePlayer(final @NotNull String uuid) {
    try {
      dao.deleteById(uuid);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void updatePlayer(final @NotNull LeaderboardPlayer player) {
    try {
      dao.createOrUpdate(player);
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
