package me.mindustry.leaderboard;

import me.mindustry.leaderboard.model.*;

public final class StandardLeaderboardPoints {

  public static final LeaderboardPoints PVP_VICTORY = LeaderboardPoints.of("Victory", +1000);
  public static final LeaderboardPoints DESTROYED_CORE = LeaderboardPoints.of("Destroyed core", -100);
  public static final LeaderboardPoints BUILD_BLOCK = LeaderboardPoints.of("Build", +1, true);

  private StandardLeaderboardPoints() {
  }
}
