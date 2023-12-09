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

import org.checkerframework.checker.nullness.qual.NonNull;

final class SimpleLeaderboardPoints implements LeaderboardPoints {

    private final String name;
    private final String description;
    private final long points;
    private final boolean silent;

    SimpleLeaderboardPoints(
            final @NonNull String name, final @NonNull String description, final long points, final boolean silent) {
        this.name = name;
        this.description = description;
        this.points = points;
        this.silent = silent;
    }

    SimpleLeaderboardPoints(final @NonNull String name, final @NonNull String description, final long points) {
        this(name, description, points, false);
    }

    SimpleLeaderboardPoints(final @NonNull String name, final long points, final boolean silent) {
        this(name, "", points, silent);
    }

    SimpleLeaderboardPoints(final @NonNull String name, final long points) {
        this(name, "", points, false);
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull String getDescription() {
        return description;
    }

    @Override
    public long getPoints() {
        return points;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }
}
