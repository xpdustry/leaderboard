package me.mindustry.leaderboard.repository;

import java.util.*;
import me.mindustry.leaderboard.model.*;
import me.mindustry.leaderboard.service.*;
import org.jetbrains.annotations.*;

public interface LeaderboardPointsRegistry {

  @NotNull Collection<LeaderboardPoints> getLeaderboardPoints();

  void registerLeaderboardPoints(final @NotNull LeaderboardService service);
}
