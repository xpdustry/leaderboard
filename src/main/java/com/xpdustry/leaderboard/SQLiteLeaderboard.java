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
package com.xpdustry.leaderboard;

import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.distributor.core.database.SQLiteConnectionFactory;
import fr.xpdustry.distributor.core.dependency.DependencyManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SQLiteLeaderboard implements Leaderboard, PluginListener {

    private static final Logger logger = LoggerFactory.getLogger(SQLiteLeaderboard.class);
    private static final int PAGE_SIZE = 500;

    private final Path file;
    private final DependencyManager dependencies;
    private @MonotonicNonNull SQLiteConnectionFactory factory = null;
    private @MonotonicNonNull Jdbi jdbi = null;

    SQLiteLeaderboard(final Path file, final DependencyManager dependencies) {
        this.file = file;
        this.dependencies = dependencies;
    }

    @Override
    public void onPluginInit() {
        factory = new SQLiteConnectionFactory(
                "", file, dependencies.createClassLoaderFor(SQLiteConnectionFactory.SQLITE_DRIVER));
        factory.start();

        jdbi = Jdbi.create(factory::getConnection);

        try (final var reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(this.getClass().getResourceAsStream("/com/xpdustry/leaderboard/schema.sql")),
                StandardCharsets.UTF_8))) {
            jdbi.withHandle(handle -> handle.execute(reader.lines().collect(Collectors.joining("\n"))));
        } catch (final IOException e) {
            throw new RuntimeException("Failed to run schema script", e);
        }
    }

    @Override
    public void onPluginExit() {
        try {
            factory.close();
        } catch (final SQLException e) {
            logger.error("Failed to close leaderboard database", e);
        }
    }

    @Override
    public void savePlayer(final @NonNull LeaderboardPlayer player) {
        jdbi.withHandle(handle -> handle.createUpdate(
                        "INSERT INTO player(uuid, points) VALUES (:uuid, :points) ON CONFLICT(uuid) DO UPDATE SET points = :points;")
                .bind("uuid", player.getUuid())
                .bind("points", player.getPoints())
                .execute());
    }

    @Override
    public boolean existsPlayerByUuid(final @NonNull String uuid) {
        return jdbi.withHandle(handle -> handle.select("SELECT COUNT(*) FROM player WHERE player.uuid = ?;")
                        .bind(0, uuid)
                        .mapTo(Integer.class)
                        .first()
                > 0);
    }

    @Override
    public @NonNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NonNull String uuid) {
        return jdbi.withHandle(handle -> handle.select("SELECT * FROM player WHERE uuid = ?")
                .bind(0, uuid)
                .map(this::mapPlayer)
                .findFirst());
    }

    @Override
    public @NonNull Iterable<LeaderboardPlayer> findAllPlayers() {
        return new Iterable<>() {
            List<LeaderboardPlayer> players = findAllPlayersPaginated(0);
            int page = 0;
            int index = 0;

            @Override
            public @NonNull Iterator<LeaderboardPlayer> iterator() {
                return new Iterator<>() {

                    @Override
                    public boolean hasNext() {
                        return index < players.size();
                    }

                    @Override
                    public LeaderboardPlayer next() {
                        if (index >= players.size()) {
                            throw new NoSuchElementException();
                        }
                        final var element = players.get(index++);
                        if (index == players.size()) {
                            players = findAllPlayersPaginated(++page);
                            index = 0;
                        }
                        return element;
                    }
                };
            }
        };
    }

    private List<LeaderboardPlayer> findAllPlayersPaginated(final int page) {
        return jdbi.withHandle(handle -> handle.select("SELECT * FROM player ORDER BY points DESC LIMIT ? OFFSET ?;")
                .bind(0, PAGE_SIZE)
                .bind(1, PAGE_SIZE * page)
                .map(this::mapPlayer)
                .collectIntoList());
    }

    @Override
    public long countPlayers() {
        return jdbi.withHandle(handle ->
                handle.select("SELECT COUNT(*) FROM player;").mapTo(Long.class).first());
    }

    @Override
    public void deletePlayerByUuid(final @NonNull String uuid) {
        jdbi.withHandle(handle -> handle.createUpdate("DELETE FROM player WHERE uuid = ?")
                .bind(0, uuid)
                .execute());
    }

    @Override
    public void deleteAllPlayers() {
        jdbi.withHandle(
                handle -> handle.createUpdate("DELETE FROM player WHERE TRUE").execute());
    }

    private LeaderboardPlayer mapPlayer(final ResultSet set, StatementContext ctx) throws SQLException {
        final var player = LeaderboardPlayer.of(set.getString("uuid"));
        player.addPoints(set.getLong("points"));
        return player;
    }
}
