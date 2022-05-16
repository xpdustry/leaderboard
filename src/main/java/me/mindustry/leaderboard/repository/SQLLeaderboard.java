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
    com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.ERROR);
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
  public void savePlayer(final @NotNull LeaderboardPlayer player) {
    try {
      dao.createOrUpdate(player);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean existsPlayerByUuid(final @NotNull String uuid) {
    return findPlayerByUuid(uuid).isPresent();
  }

  @Override
  public @NotNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NotNull String uuid) {
    try {
      return Optional.ofNullable(dao.queryForId(uuid));
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull Iterable<LeaderboardPlayer> findAllPlayers() {
    try {
      return dao.queryBuilder().orderBy("points", false).query();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long countPlayers() {
    try {
      return dao.countOf();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deletePlayerByUuid(final @NotNull String uuid) {
    try {
      dao.deleteById(uuid);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteAllPlayers() {
    try {
      dao.deleteBuilder().delete();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
