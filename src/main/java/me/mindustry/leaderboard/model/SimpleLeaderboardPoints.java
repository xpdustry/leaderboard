package me.mindustry.leaderboard.model;

import org.jetbrains.annotations.*;

final class SimpleLeaderboardPoints implements LeaderboardPoints {

  private final String name;
  private final String description;
  private final long points;
  private final boolean silent;

  SimpleLeaderboardPoints(final @NotNull String name, final @NotNull String description, final long points, final boolean silent) {
    this.name = name;
    this.description = description;
    this.points = points;
    this.silent = silent;
  }

  SimpleLeaderboardPoints(final @NotNull String name, final @NotNull String description, final long points) {
    this(name, description, points, false);
  }

  SimpleLeaderboardPoints(final @NotNull String name, final long points, final boolean silent) {
    this(name, "", points, silent);
  }

  SimpleLeaderboardPoints(final @NotNull String name, final long points) {
    this(name, "", points, false);
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull String getDescription() {
    return description;
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
