package me.mindustry.leaderboard.core;

public enum DefaultLeaderboardPoints {
  DESTROYED_CORE(-100),
  PLAYER_DIED(-10),
  WON_GAME(+1000),
  BUILD_BLOCK(+1);

  private final long points;

  DefaultLeaderboardPoints(final long points) {
    this.points = points;
  }

  public long getPoints() {
    return points;
  }
}
