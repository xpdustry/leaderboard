package me.mindustry.leaderboard.core;

import com.googlecode.cqengine.persistence.support.serialization.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

@PersistenceConfig(polymorphic = true)
public interface LeaderboardPlayer {

  static LeaderboardPlayer simple(final @NotNull Player player) {
    return new SimpleLeaderboardPlayer(player.uuid());
  }

  @NotNull String getUuid();

  long getPoints();

  void applyPoints(final long points);

  default void applyPoints(final @NotNull DefaultLeaderboardPoints defaultPoints) {
    applyPoints(defaultPoints.getPoints());
  }
}
