package me.mindustry.leaderboard.internal;

import org.aeonbits.owner.*;

public interface LeaderboardConfig extends Accessible {

  @DefaultValue("PERSISTENT")
  @Key("leaderboard.type")
  LeaderboardType getLeaderboardType();

  enum LeaderboardType {
    IN_MEMORY, INDEXED, PERSISTENT
  }
}
