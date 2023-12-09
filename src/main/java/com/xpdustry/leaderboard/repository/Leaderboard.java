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

import com.xpdustry.leaderboard.model.LeaderboardPlayer;
import java.io.File;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Leaderboard {

    static @NonNull Leaderboard simple() {
        return new SimpleLeaderboard();
    }

    static @NonNull Leaderboard sqlite(final @NonNull File file) {
        return new SQLLeaderboard(file);
    }

    static @NonNull Leaderboard sql(
            final @NonNull String url,
            final @Nullable String username,
            final @Nullable String password,
            final @NonNull String table) {
        return new SQLLeaderboard(url, username, password, table);
    }

    static @NonNull Leaderboard sql(
            final @NonNull String url, final @Nullable String username, final @Nullable String password) {
        return new SQLLeaderboard(url, username, password);
    }

    /**
     * Saves the leaderboard player.
     *
     * @param player the leaderboard player
     */
    void savePlayer(final @NonNull LeaderboardPlayer player);

    boolean existsPlayerByUuid(final @NonNull String uuid);

    /**
     * Searches for the uuid of a leaderboard player.
     *
     * @param uuid the uuid of the leaderboard player
     * @return an optional encapsulating the leaderboard player
     */
    @NonNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NonNull String uuid);

    /**
     * Returns an ordered iterable of the leaderboard players from the highest score to the lowest.
     */
    @NonNull Iterable<LeaderboardPlayer> findAllPlayers();

    long countPlayers();

    void deletePlayerByUuid(final @NonNull String uuid);

    void deleteAllPlayers();
}
