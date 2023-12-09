/*
 * This file is part of LeaderboardPlugin. A simple leaderboard system for players.
 *
 * MIT License
 *
 * Copyright (c) 2023 xpdustry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xpdustry.leaderboard.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.xpdustry.leaderboard.model.LeaderboardPlayer;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class SQLLeaderboard implements Leaderboard {

    private static final String DEFAULT_TABLE = "leaderboard_player";

    static {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(com.j256.ormlite.logger.Level.ERROR);
    }

    private final Dao<LeaderboardPlayer, String> dao;

    SQLLeaderboard(
            final @NonNull String url,
            final @Nullable String username,
            final @Nullable String password,
            final @NonNull String table) {
        try {
            final var source = new JdbcConnectionSource(url, username, password);

            final var uuidField = new DatabaseFieldConfig("uuid");
            uuidField.setId(true);
            uuidField.setCanBeNull(false);

            final var scoreField = new DatabaseFieldConfig("points");
            scoreField.setCanBeNull(false);

            dao = DaoManager.createDao(
                    source, new DatabaseTableConfig<>(LeaderboardPlayer.class, table, List.of(uuidField, scoreField)));

            TableUtils.createTableIfNotExists(source, LeaderboardPlayer.class);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    SQLLeaderboard(final @NonNull String url, final @Nullable String username, final @Nullable String password) {
        this(url, username, password, DEFAULT_TABLE);
    }

    SQLLeaderboard(final @NonNull File file) {
        this("jdbc:sqlite:" + file.getAbsolutePath(), null, null, DEFAULT_TABLE);
    }

    @Override
    public void savePlayer(final @NonNull LeaderboardPlayer player) {
        try {
            dao.createOrUpdate(player);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsPlayerByUuid(final @NonNull String uuid) {
        return findPlayerByUuid(uuid).isPresent();
    }

    @Override
    public @NonNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NonNull String uuid) {
        try {
            return Optional.ofNullable(dao.queryForId(uuid));
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Iterable<LeaderboardPlayer> findAllPlayers() {
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
    public void deletePlayerByUuid(final @NonNull String uuid) {
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
