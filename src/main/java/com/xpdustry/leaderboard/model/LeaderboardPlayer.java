/*
 * This file is part of LeaderboardPlugin. A simple leaderboard system for players.
 *
 * MIT License
 *
 * Copyright (c) 2023 xpdustry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xpdustry.leaderboard.model;

import arc.util.Strings;
import java.io.Serial;
import java.io.Serializable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class LeaderboardPlayer implements Serializable, Comparable<LeaderboardPlayer> {

    @Serial
    private static final long serialVersionUID = 1624078426106904493L;

    private final String uuid;
    private long points = 0L;

    private LeaderboardPlayer(final @NonNull String uuid) {
        this.uuid = uuid;
    }

    /**
     * No args constructor for ORMLite, <strong>DO NOT USE</strong>.
     */
    @SuppressWarnings("unused")
    LeaderboardPlayer() {
        this.uuid = "";
    }

    public static @NonNull LeaderboardPlayer of(final @NonNull String uuid) {
        return new LeaderboardPlayer(uuid);
    }

    public @NonNull String getUuid() {
        return uuid;
    }

    public long getPoints() {
        return points;
    }

    public void addPoints(final long points) {
        this.points = Math.max(0, this.points + points);
    }

    public void addPoints(final @NonNull LeaderboardPoints points) {
        addPoints(points.getPoints());
    }

    public void resetPoints() {
        points = 0L;
    }

    @Override
    public int compareTo(final @NonNull LeaderboardPlayer o) {
        return Long.compare(this.points, o.points);
    }

    @Override
    public @NonNull String toString() {
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
