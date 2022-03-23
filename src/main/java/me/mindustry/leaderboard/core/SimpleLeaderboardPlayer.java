package me.mindustry.leaderboard.core;

import arc.util.*;
import org.jetbrains.annotations.*;

final class SimpleLeaderboardPlayer implements LeaderboardPlayer {

  private final String uuid;
  private long points = 0;

  SimpleLeaderboardPlayer(final @NotNull String uuid) {
    this.uuid = uuid;
  }

  @Override
  public @NotNull String getUuid() {
    return uuid;
  }

  @Override
  public long getPoints() {
    return points;
  }

  @Override
  public void applyPoints(long points) {
    this.points = Math.max(0, this.points + points);
  }

  @Override
  public void applyPoints(@NotNull DefaultLeaderboardPoints point) {

  }

  @Override
  public String toString() {
    return Strings.format("SimpleLeaderboardPlayer{uuid='@', points='@'}", uuid, points);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimpleLeaderboardPlayer that = (SimpleLeaderboardPlayer) o;

    if (points != that.points) return false;
    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    int result = uuid.hashCode();
    result = 31 * result + (int) (points ^ (points >>> 32));
    return result;
  }
}
