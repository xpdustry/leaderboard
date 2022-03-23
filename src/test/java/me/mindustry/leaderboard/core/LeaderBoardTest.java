package me.mindustry.leaderboard.core;

import java.util.*;
import me.mindustry.leaderboard.util.*;
import mindustry.gen.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderBoardTest {

  private Player playerA;
  private Player playerB;
  private Player playerC;

  @BeforeEach
  void beforeAll() {
    playerA = new TestPlayer();
    playerB = new TestPlayer();
    playerC = new TestPlayer();
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "INDEXED"})
  void test_player_get(final String type) {
    final var leaderboard = getLeaderboard(type);
    assertEquals(0, leaderboard.getSize());

    final var playerLeaderboard = leaderboard.getPlayer(playerA);
    assertEquals(1, leaderboard.getSize());
    assertEquals(playerLeaderboard.getUuid(), playerA.uuid());

    // Same player, shouldn't add
    leaderboard.getPlayer(playerA);
    assertEquals(1, leaderboard.getSize());

    // Different player; should add
    leaderboard.getPlayer(playerB);
    assertEquals(2, leaderboard.getSize());
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "INDEXED"})
  void test_player_has(final String type) {
    final var leaderboard = getLeaderboard(type);

    assertFalse(leaderboard.hasPlayer(playerC));
    leaderboard.getPlayer(playerC);
    assertTrue(leaderboard.hasPlayer(playerC));
  }

  @ParameterizedTest
  @ValueSource(strings = {"IN_MEMORY", "INDEXED"})
  void test_player_page(final String type) {
    final var leaderboard = getLeaderboard(type);

    final var leaderboardPlayerA = leaderboard.getPlayer(playerA);  // 900 points
    leaderboardPlayerA.applyPoints(DefaultLeaderboardPoints.WON_GAME);
    leaderboardPlayerA.applyPoints(DefaultLeaderboardPoints.DESTROYED_CORE);

    final var leaderboardPlayerB = leaderboard.getPlayer(playerB);  // 1000 points
    leaderboardPlayerB.applyPoints(DefaultLeaderboardPoints.WON_GAME);

    final var leaderboardPlayerC = leaderboard.getPlayer(playerC);  // 0 points

    final var page = leaderboard.getPage(0, 2);
    assertEquals(2, page.size());
    assertTrue(areCollectionEquals(List.of(leaderboardPlayerB, leaderboardPlayerA), page));
  }

  Leaderboard getLeaderboard(final String type) {
    return switch (type) {
      case "IN_MEMORY" -> Leaderboard.inMemory();
      case "INDEXED" -> Leaderboard.indexed();
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
