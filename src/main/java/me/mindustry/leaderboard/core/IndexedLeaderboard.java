package me.mindustry.leaderboard.core;

import com.googlecode.cqengine.*;
import com.googlecode.cqengine.attribute.*;
import com.googlecode.cqengine.index.navigable.*;
import com.googlecode.cqengine.index.unique.*;
import com.googlecode.cqengine.persistence.disk.*;
import com.googlecode.cqengine.query.*;
import com.googlecode.cqengine.query.option.*;
import com.googlecode.cqengine.resultset.common.*;
import java.io.*;
import java.util.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

final class IndexedLeaderboard implements Leaderboard {

  private static final SimpleAttribute<LeaderboardPlayer, String> UUID_ATTRIBUTE = new SimpleAttribute<>() {
    @Override
    public String getValue(final @NotNull LeaderboardPlayer object, final @NotNull QueryOptions queryOptions) {
      return object.getUuid();
    }
  };

  private static final SimpleAttribute<LeaderboardPlayer, Long> POINTS_ATTRIBUTE = new SimpleAttribute<>() {
    @Override
    public Long getValue(final @NotNull LeaderboardPlayer object, final @NotNull QueryOptions queryOptions) {
      return object.getPoints();
    }
  };

  private static final AttributeOrder<LeaderboardPlayer> POINTS_ATTRIBUTE_ORDER = new AttributeOrder<>(POINTS_ATTRIBUTE, true);

  private final IndexedCollection<LeaderboardPlayer> players;

  IndexedLeaderboard() {
    players = new ObjectLockingIndexedCollection<>();
    players.addIndex(UniqueIndex.onAttribute(UUID_ATTRIBUTE));
    players.addIndex(NavigableIndex.onAttribute(POINTS_ATTRIBUTE));
  }

  IndexedLeaderboard(final @NotNull File file) {
    players = new ObjectLockingIndexedCollection<>(DiskPersistence.onPrimaryKeyInFile(UUID_ATTRIBUTE, file));
    players.addIndex(NavigableIndex.onAttribute(POINTS_ATTRIBUTE));
  }

  @Override
  public @NotNull Collection<LeaderboardPlayer> getPlayers() {
    return Collections.unmodifiableCollection(players);
  }

  @Override
  public @NotNull LeaderboardPlayer getPlayer(@NotNull Player player) {
    var leaderboardPlayer = findPlayer(player);
    if (leaderboardPlayer == null) {
      players.add(leaderboardPlayer = LeaderboardPlayer.simple(player));
    }
    return leaderboardPlayer;
  }

  @Override
  public boolean hasPlayer(@NotNull Player player) {
    return findPlayer(player) != null;
  }

  @Override
  public void removePlayer(@NotNull Player player) {
    var leaderboardPlayer = findPlayer(player);
    if (leaderboardPlayer != null) players.remove(leaderboardPlayer);
  }

  @Override
  public void reset() {
    players.clear();
  }

  @Override
  public int getSize() {
    return players.size();
  }

  @Override
  public @NotNull List<LeaderboardPlayer> getPage(int num, int size) {
    return players.retrieve(
        QueryFactory.all(LeaderboardPlayer.class), QueryFactory.queryOptions(QueryFactory.orderBy(POINTS_ATTRIBUTE_ORDER))
      )
      .stream()
      .skip((long) num * size)
      .limit(size)
      .toList();
  }

  @Override
  public int getRank(@NotNull Player player) {
    final var all = getPage(0, getSize());
    for (int i = 0; i < all.size(); i++) {
      if (all.get(i).getUuid().equals(player.uuid())) return i;
    }

    return -1;
  }

  private @Nullable LeaderboardPlayer findPlayer(final @NotNull Player player) {
    try {
      return players.retrieve(QueryFactory.equal(UUID_ATTRIBUTE, player.uuid())).uniqueResult();
    } catch (final NoSuchObjectException e) {
      return null;
    }
  }
}
