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
package com.xpdustry.leaderboard;

import arc.util.Strings;
import java.util.Optional;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimpleLeaderboardService implements LeaderboardService {

    protected final Leaderboard leaderboard;

    SimpleLeaderboardService(final @NonNull Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public long getPoints(final @NonNull String uuid) {
        return leaderboard
                .findPlayerByUuid(uuid)
                .map(LeaderboardPlayer::getPoints)
                .orElse(0L);
    }

    @Override
    public void grantPoints(final @NonNull String uuid, final @NonNull LeaderboardPoints points) {
        final var player = leaderboard.findPlayerByUuid(uuid).orElseGet(() -> LeaderboardPlayer.of(uuid));
        player.addPoints(points);
        leaderboard.savePlayer(player);
    }

    @Override
    public void grantPoints(final @NonNull Player player, final @NonNull LeaderboardPoints points) {
        grantPoints(player.uuid(), points);
        if (points.isSilent()) return;
        Call.warningToast(
                player.con(),
                Iconc.power,
                Strings.format(
                        "[yellow]@[] [cyan]>[] [@]@[]",
                        points.getName(),
                        points.getPoints() > 0 ? "green" : "red",
                        points.getPoints()));
    }

    @Override
    public long getRank(final @NonNull String uuid) {
        if (!leaderboard.existsPlayerByUuid(uuid)) {
            leaderboard.savePlayer(LeaderboardPlayer.of(uuid));
        }
        final var players = leaderboard.findAllPlayers().iterator();
        var rank = 1;
        while (players.hasNext()) {
            if (players.next().getUuid().equals(uuid)) return rank;
            rank++;
        }
        throw new IllegalStateException();
    }

    @Override
    public void showLeaderboard(final @NonNull Player player) {
        final var builder = new StringBuilder().append("[yellow]Leaderboard:[]");
        final var players = leaderboard.findAllPlayers().iterator();
        var rank = 1;

        while (players.hasNext() && rank <= 10) {
            final var leaderboardPlayer = players.next();
            final var name = Optional.ofNullable(
                            Groups.player.find(p -> p.uuid().equals(leaderboardPlayer.getUuid())))
                    .map(Player::name)
                    .orElseGet(() -> Vars.netServer.admins.getInfo(leaderboardPlayer.getUuid()).lastName);

            builder.append("\n#").append(rank++);
            builder.append(" [white]: ").append(name).append("[white] - ").append(leaderboardPlayer.getPoints());
        }

        Call.infoToast(player.con, builder.toString(), 10f);
    }
}
