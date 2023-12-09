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

import arc.Events;
import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Strings;
import com.xpdustry.leaderboard.repository.Leaderboard;
import com.xpdustry.leaderboard.repository.PointsRegistry;
import com.xpdustry.leaderboard.service.LeaderboardService;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import net.mindustry_ddns.filestore.FileStore;
import net.mindustry_ddns.filestore.Store;
import net.mindustry_ddns.filestore.serial.Serializers;
import org.aeonbits.owner.ConfigFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unused")
public final class LeaderboardPlugin extends Plugin {

    private static final Fi LEADERBOARD_DIRECTORY = new Fi("./leaderboard");
    private static final Collection<PointsRegistry> registries = new ArrayList<>();
    private static Function<Leaderboard, LeaderboardService> leaderboardServiceFactory = LeaderboardService::simple;
    private static Supplier<Leaderboard> customLeaderboardProvider = () -> {
        throw new IllegalStateException("The custom leaderboard provider hasn't been set.");
    };

    @SuppressWarnings("NullAway.Init")
    private static LeaderboardService service;

    private final Store<LeaderboardConfig> store = FileStore.of(
            LEADERBOARD_DIRECTORY.child("config.properties").file(),
            Serializers.config(),
            new TypeToken<>() {},
            ConfigFactory.create(LeaderboardConfig.class));

    public static void setCustomLeaderboardProvider(
            final @NonNull Supplier<@NonNull Leaderboard> customLeaderboardProvider) {
        LeaderboardPlugin.customLeaderboardProvider = customLeaderboardProvider;
    }

    public static Function<Leaderboard, LeaderboardService> getLeaderboardServiceFactory() {
        return leaderboardServiceFactory;
    }

    public static void setLeaderboardServiceFactory(
            final @NonNull Function<Leaderboard, @NonNull LeaderboardService> leaderboardServiceFactory) {
        LeaderboardPlugin.leaderboardServiceFactory = leaderboardServiceFactory;
    }

    public static void addPointsRegistry(final @NonNull PointsRegistry registry) {
        registries.add(registry);
    }

    public static Collection<PointsRegistry> getPointsRegistries() {
        return Collections.unmodifiableCollection(registries);
    }

    public static LeaderboardService getLeaderboardService() {
        return service;
    }

    @Override
    public void init() {
        LEADERBOARD_DIRECTORY.mkdirs();
        store.load();

        final var leaderboard =
                switch (getConf().getLeaderboardType()) {
                    case IN_MEMORY -> Leaderboard.simple();
                    case PERSISTENT -> Leaderboard.sqlite(
                            LEADERBOARD_DIRECTORY.child("leaderboard.sqlite").file());
                    case CUSTOM -> customLeaderboardProvider.get();
                };

        service = leaderboardServiceFactory.apply(leaderboard);
        if (getConf().showLeaderboardOnJoin()) {
            Events.on(EventType.PlayerJoin.class, e -> service.showLeaderboard(e.player));
        }
    }

    @Override
    public void registerClientCommands(final @NonNull CommandHandler handler) {
        handler.<Player>register("lb-rank", "Get your leaderboard status.", (args, player) -> {
            player.sendMessage(
                    Strings.format("Rank: @, Points: @", service.getRank(player), service.getPoints(player)));
        });

        handler.<Player>register("lb-board", "Show the top 10 of the leaderboard.", (args, player) -> {
            service.showLeaderboard(player);
        });

        handler.<Player>register("lb-points", "[page]", "Display the available points.", (args, player) -> {
            final var page = args.length == 0 ? 0 : Strings.parseInt(args[0], -1);
            if (page == -1) {
                player.sendMessage("Invalid page number " + args[0]);
                return;
            } else if (page < 0) {
                player.sendMessage("The page number is negative.");
                return;
            }

            final var points = registries.stream()
                    .flatMap(r -> r.getLeaderboardPoints().stream())
                    .skip(page * 10L)
                    .limit(10L)
                    .toList();

            if (points.isEmpty()) {
                player.sendMessage("No points at page " + page);
            } else {
                final var builder = new StringBuilder();
                builder.append("LeaderboardPoints (page ").append(page).append("):");
                points.forEach(p -> {
                    builder.append("\n[cyan]-[white] ").append(p.getName());
                    if (!p.getDescription().isBlank()) {
                        builder.append("[cyan]:[white] ").append(p.getDescription());
                    }
                    builder.append(" ");
                    if (p.getPoints() > 0) {
                        builder.append("[green]+");
                    } else if (p.getPoints() < 0) {
                        builder.append("[red]");
                    }
                    builder.append(p.getPoints());
                });
                player.sendMessage(builder.toString());
            }
        });
    }

    private @NonNull LeaderboardConfig getConf() {
        return store.get();
    }
}
