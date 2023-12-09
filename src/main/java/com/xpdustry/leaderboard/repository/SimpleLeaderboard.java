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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

final class SimpleLeaderboard implements Leaderboard {

    private final Map<String, LeaderboardPlayer> players = new HashMap<>();

    SimpleLeaderboard() {}

    @Override
    public void savePlayer(final @NonNull LeaderboardPlayer player) {
        players.put(player.getUuid(), player);
    }

    @Override
    public boolean existsPlayerByUuid(final @NonNull String uuid) {
        return players.containsKey(uuid);
    }

    @Override
    public @NonNull Optional<LeaderboardPlayer> findPlayerByUuid(final @NonNull String uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public @NonNull Iterable<LeaderboardPlayer> findAllPlayers() {
        return players.values().stream()
                .sorted(Comparator.comparingLong(LeaderboardPlayer::getPoints).reversed())
                .toList();
    }

    @Override
    public long countPlayers() {
        return players.size();
    }

    @Override
    public void deletePlayerByUuid(final @NonNull String uuid) {
        players.remove(uuid);
    }

    @Override
    public void deleteAllPlayers() {
        players.clear();
    }
}
