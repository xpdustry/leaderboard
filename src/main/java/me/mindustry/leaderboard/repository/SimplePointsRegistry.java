package me.mindustry.leaderboard.repository;

import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

final class SimplePointsRegistry implements PointsRegistry {

  private final Collection<LeaderboardPoints> points;

  SimplePointsRegistry(final @NotNull Collection<LeaderboardPoints> points) {
    this.points = points;
  }

  @Override
  public @NotNull Collection<LeaderboardPoints> getLeaderboardPoints() {
    return Collections.unmodifiableCollection(points);
  }
}
