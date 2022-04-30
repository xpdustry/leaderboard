package me.mindustry.leaderboard.repository;

import java.nio.file.*;
import java.util.*;
import me.mindustry.leaderboard.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static org.junit.jupiter.api.Assertions.*;

public final class LeaderboardTest {

  private String playerA;
  private String playerB;
  private String playerC;

  @TempDir
  private Path tempDir;

  @BeforeEach
  void setup() {
    playerA = UUID.randomUUID().toString();
    playerB = UUID.randomUUID().toString();
    playerC = UUID.randomUUID().toString();
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
  void test_leaderboard_add(final String type) {
    final var leaderboard = getLeaderboard(type);
    assertEquals(0, leaderboard.getSize());

    final var playerLeaderboard = leaderboard.addPlayer(playerA);
    assertEquals(1, leaderboard.getSize());
    assertEquals(playerLeaderboard.getUuid(), playerA);

    // Same player, shouldn't add
    leaderboard.addPlayer(playerA);
    assertEquals(1, leaderboard.getSize());

    // Different player; should add
    leaderboard.addPlayer(playerB);
    assertEquals(2, leaderboard.getSize());
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
  void test_leaderboard_has_and_get(final String type) {
    final var leaderboard = getLeaderboard(type);

    assertFalse(leaderboard.hasPlayer(playerC));
    assertNull(leaderboard.getPlayer(playerC));

    final var leaderboardPlayer = leaderboard.addPlayer(playerC);

    assertTrue(leaderboard.hasPlayer(playerC));
    assertEquals(leaderboardPlayer, leaderboard.getPlayer(playerC));
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
  void test_leaderboard_order(final String type) {
    final var leaderboard = getLeaderboard(type);

    final var leaderboardPlayerA = leaderboard.addPlayer(playerA);  // 900 points
    leaderboardPlayerA.addPoints(StandardLeaderboardPoints.PVP_VICTORY);
    leaderboardPlayerA.addPoints(StandardLeaderboardPoints.DESTROYED_CORE);
    leaderboard.updatePlayer(leaderboardPlayerA);

    final var leaderboardPlayerB = leaderboard.addPlayer(playerB);  // 1000 points
    leaderboardPlayerB.addPoints(StandardLeaderboardPoints.PVP_VICTORY);
    leaderboard.updatePlayer(leaderboardPlayerB);

    final var leaderboardPlayerC = leaderboard.addPlayer(playerC);  // 0 points
    leaderboard.updatePlayer(leaderboardPlayerC);

    assertTrue(
      areCollectionEquals(
        List.of(leaderboardPlayerB, leaderboardPlayerA, leaderboardPlayerC),
        leaderboard.getPlayers()
      )
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
  void test_leaderboard_remove(final String type) {
    final var leaderboard = getLeaderboard(type);

    leaderboard.addPlayer(playerA);
    final var leaderboardPlayerB = leaderboard.addPlayer(playerB);
    final var leaderboardPlayerC = leaderboard.addPlayer(playerC);

    assertEquals(3, leaderboard.getSize());
    leaderboard.removePlayer(playerA);
    assertEquals(2, leaderboard.getSize());
    assertTrue(leaderboard.getPlayers().containsAll(List.of(leaderboardPlayerB, leaderboardPlayerC)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "PERSISTENT"})
  void test_leaderboard_reset(final String type) {
    final var leaderboard = getLeaderboard(type);

    leaderboard.addPlayer(playerA);
    leaderboard.addPlayer(playerB);
    leaderboard.addPlayer(playerC);

    assertEquals(3, leaderboard.getSize());
    leaderboard.reset();
    assertEquals(0, leaderboard.getSize());
    assertTrue(leaderboard.getPlayers().isEmpty());
  }

  Leaderboard getLeaderboard(final String type) {
    return switch (type) {
      case "IN_MEMORY" -> Leaderboard.simple();
      case "PERSISTENT" -> Leaderboard.sqlite(tempDir.resolve("data.db").toFile());
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
