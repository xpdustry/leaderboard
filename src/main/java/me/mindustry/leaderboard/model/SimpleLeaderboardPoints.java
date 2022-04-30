package me.mindustry.leaderboard.model;

import org.jetbrains.annotations.*;

final class SimpleLeaderboardPoints implements LeaderboardPoints {

  private final String name;
  private final long points;
  private final boolean silent;

  SimpleLeaderboardPoints(final @NotNull String name, final long points, final boolean silent) {
    this.name = name;
    this.points = points;
    this.silent = silent;
  }

  SimpleLeaderboardPoints(final @NotNull String name, final long points) {
    this(name, points, false);
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public long getPoints() {
    return points;
  }

  @Override
  public boolean isSilent() {
    return silent;
  }
}
