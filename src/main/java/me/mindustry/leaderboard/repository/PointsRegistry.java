package me.mindustry.leaderboard.repository;

import java.util.*;
import me.mindustry.leaderboard.model.*;
import org.jetbrains.annotations.*;

public interface PointsRegistry {

  static @NotNull PointsRegistry of(final @NotNull LeaderboardPoints... points) {
    return new SimplePointsRegistry(List.of(points));
  }

  @NotNull Collection<LeaderboardPoints> getLeaderboardPoints();
}
