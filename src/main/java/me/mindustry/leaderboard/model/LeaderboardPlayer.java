package me.mindustry.leaderboard.model;

import arc.util.*;
import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;
import java.io.*;
import org.jetbrains.annotations.*;

@DatabaseTable(tableName = "players")
public final class LeaderboardPlayer implements Serializable, Comparable<LeaderboardPlayer> {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  @DatabaseField(columnName = "uuid", canBeNull = false, id = true)
  private final String uuid;

  @DatabaseField(columnName = "points", canBeNull = false)
  private long points = 0;

  /**
   * No args constructor for the SQLite library, DO NOT USE.
   */
  LeaderboardPlayer() {
    this.uuid = "";
  }

  private LeaderboardPlayer(final @NotNull String uuid) {
    this.uuid = uuid;
  }

  public static LeaderboardPlayer of(final @NotNull String uuid) {
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
  public String toString() {
    return Strings.format("SimpleLeaderboardPlayer{uuid='@', points='@'}", uuid, points);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LeaderboardPlayer that = (LeaderboardPlayer) o;
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
