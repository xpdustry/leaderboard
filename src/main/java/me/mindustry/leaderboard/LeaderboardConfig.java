package me.mindustry.leaderboard;

import org.aeonbits.owner.*;
import org.jetbrains.annotations.*;

public interface LeaderboardConfig extends Accessible {

  @DefaultValue("PERSISTENT")
  @Key("leaderboard.type")
  @NotNull LeaderboardType getLeaderboardType();

  @DefaultValue("true")
  @Key("leaderboard.player.show-on-join")
  boolean showLeaderboardOnJoin();

  enum LeaderboardType {
    IN_MEMORY, PERSISTENT, CUSTOM
  }
}
