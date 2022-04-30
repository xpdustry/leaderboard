package me.mindustry.leaderboard;

import org.aeonbits.owner.*;

public interface LeaderboardConfig extends Accessible {

  @DefaultValue("PERSISTENT")
  @Key("leaderboard.type")
  LeaderboardType getLeaderboardType();

  enum LeaderboardType {
    IN_MEMORY, PERSISTENT
  }
}
