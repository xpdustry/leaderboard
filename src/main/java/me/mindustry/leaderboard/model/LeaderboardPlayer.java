package me.mindustry.leaderboard.model;

import arc.util.*;
import java.io.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;

public final class LeaderboardPlayer implements Serializable, Comparable<LeaderboardPlayer> {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  private final String uuid;
  private long points = 0;

  private LeaderboardPlayer(final @NotNull String uuid) {
    this.uuid = uuid;
  }

  /**
   * No args constructor for ORMLite, <strong>DO NOT USE</strong>.
   */
  @SuppressWarnings("unused")
  LeaderboardPlayer() {
    this.uuid = "";
  }

  public static @NotNull LeaderboardPlayer of(final @NotNull String uuid) {
    return new LeaderboardPlayer(uuid);
  }

  public @NotNull String getUuid() {
    return uuid;
  }

  public long getPoints() {
    return points;
  }

  public void addPoints(final long points) {
    this.points = Math.max(0, this.points + points);
  }

  public void addPoints(final @NotNull LeaderboardPoints points) {
    addPoints(points.getPoints());
  }

  @Override
  public int compareTo(final @NotNull LeaderboardPlayer o) {
    return Long.compare(this.points, o.points);
  }

  @Override
  public @NotNull String toString() {
    return Strings.format("SimpleLeaderboardPlayer{uuid='@', points='@'}", uuid, points);
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final LeaderboardPlayer that = (LeaderboardPlayer) o;
    return uuid.equals(that.uuid) && points == that.points;
  }

  @Override
  public int hashCode() {
    int result = uuid.hashCode();
    result = 31 * result + (int) (points ^ (points >>> 32));
    return result;
  }
}
