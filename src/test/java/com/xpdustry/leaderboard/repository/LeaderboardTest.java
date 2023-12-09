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

import arc.struct.Seq;
import com.xpdustry.leaderboard.model.LeaderboardPlayer;
import com.xpdustry.leaderboard.model.LeaderboardPoints;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LeaderboardTest {

    private static final LeaderboardPoints POINTS_A = LeaderboardPoints.of("A", +100);
    private static final LeaderboardPoints POINTS_B = LeaderboardPoints.of("B", +50);

    private LeaderboardPlayer playerA;
    private LeaderboardPlayer playerB;
    private LeaderboardPlayer playerC;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setup() {
        playerA = LeaderboardPlayer.of(UUID.randomUUID().toString());
        playerB = LeaderboardPlayer.of(UUID.randomUUID().toString());
        playerC = LeaderboardPlayer.of(UUID.randomUUID().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
    void test_leaderboard_add(final String type) {
        final var leaderboard = getLeaderboard(type);
        assertEquals(0, leaderboard.countPlayers());

        leaderboard.savePlayer(playerA);
        assertEquals(1, leaderboard.countPlayers());

        // Same player, shouldn't add
        leaderboard.savePlayer(playerA);
        assertEquals(1, leaderboard.countPlayers());

        // Different player; should add
        leaderboard.savePlayer(playerB);
        assertEquals(2, leaderboard.countPlayers());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
    void test_leaderboard_has_and_get(final String type) {
        final var leaderboard = getLeaderboard(type);

        assertFalse(leaderboard.existsPlayerByUuid(playerC.getUuid()));
        assertTrue(leaderboard.findPlayerByUuid(playerC.getUuid()).isEmpty());

        playerC.addPoints(100);
        leaderboard.savePlayer(playerC);

        assertTrue(leaderboard.existsPlayerByUuid(playerC.getUuid()));
        assertEquals(playerC, leaderboard.findPlayerByUuid(playerC.getUuid()).get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
    void test_leaderboard_order(final String type) {
        final var leaderboard = getLeaderboard(type);

        playerA.addPoints(POINTS_B);
        leaderboard.savePlayer(playerA); // 50 points

        playerB.addPoints(POINTS_A);
        playerB.addPoints(POINTS_B);
        leaderboard.savePlayer(playerB); // 150 points

        leaderboard.savePlayer(playerC); // 0 points

        assertTrue(areCollectionEquals(
                List.of(playerB, playerA, playerC),
                Seq.with(leaderboard.findAllPlayers()).list()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
    void test_leaderboard_delete(final String type) {
        final var leaderboard = getLeaderboard(type);

        leaderboard.savePlayer(playerA);
        leaderboard.savePlayer(playerB);
        leaderboard.savePlayer(playerC);

        assertEquals(3, leaderboard.countPlayers());
        leaderboard.deletePlayerByUuid(playerA.getUuid());
        assertEquals(2, leaderboard.countPlayers());
        assertTrue(Seq.with(leaderboard.findAllPlayers()).list().containsAll(List.of(playerB, playerC)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
    void test_leaderboard_delete_all(final String type) {
        final var leaderboard = getLeaderboard(type);

        leaderboard.savePlayer(playerA);
        leaderboard.savePlayer(playerB);
        leaderboard.savePlayer(playerC);

        assertEquals(3, leaderboard.countPlayers());
        leaderboard.deleteAllPlayers();
        assertEquals(0, leaderboard.countPlayers());
        assertTrue(Seq.with(leaderboard.findAllPlayers()).isEmpty());
    }

    Leaderboard getLeaderboard(final String type) {
        return switch (type) {
            case "IN_MEMORY" -> Leaderboard.simple();
            case "PERSISTENT" -> Leaderboard.sqlite(
                    tempDir.resolve("data.sqlite").toFile());
            default -> throw new IllegalArgumentException("Unexpected leaderboard type: " + type);
        };
    }

    <T> boolean areCollectionEquals(final Collection<T> collectionA, final Collection<T> collectionB) {
        if (collectionA.size() != collectionB.size()) {
            return false;
        } else {
            final var iteratorA = collectionA.iterator();
            final var iteratorB = collectionB.iterator();
            while (iteratorA.hasNext() && iteratorB.hasNext()) {
                if (!Objects.equals(iteratorA.next(), iteratorB.next())) return false;
            }
            return true;
        }
    }
}
