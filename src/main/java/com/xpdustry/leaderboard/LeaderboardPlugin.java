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
import arc.util.CommandHandler;
import arc.util.Strings;
import fr.xpdustry.distributor.api.DistributorProvider;
import fr.xpdustry.distributor.api.plugin.AbstractMindustryPlugin;
import fr.xpdustry.distributor.core.DistributorCorePlugin;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;
import net.mindustry_ddns.filestore.FileStore;
import net.mindustry_ddns.filestore.Store;
import net.mindustry_ddns.filestore.serial.Serializers;
import org.aeonbits.owner.ConfigFactory;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unused")
public final class LeaderboardPlugin extends AbstractMindustryPlugin {

    private final List<PointsRegistry> registries = new ArrayList<>();
    private Function<Leaderboard, LeaderboardService> leaderboardServiceProvider = LeaderboardService::simple;
    private Supplier<Leaderboard> leaderboardProvider = () -> {
        final var leaderboard = new SQLiteLeaderboard(
                getDirectory().resolve("database.sqlite"),
                ((DistributorCorePlugin) DistributorProvider.get()).getDependencyManager());
        addListener(leaderboard);
        return leaderboard;
    };

    private @MonotonicNonNull LeaderboardService service;

    private final Store<LeaderboardConfig> store = FileStore.of(
            getDirectory().resolve("config.properties").toFile(),
            Serializers.config(),
            new TypeToken<>() {},
            ConfigFactory.create(LeaderboardConfig.class));

    private static LeaderboardPlugin getInstance() {
        return ((LeaderboardPlugin) Vars.mods.getMod(LeaderboardPlugin.class).main);
    }

    public static void setLeaderboardProvider(final @NonNull Supplier<@NonNull Leaderboard> leaderboardProvider) {
        getInstance().leaderboardProvider = leaderboardProvider;
    }

    public static Function<Leaderboard, LeaderboardService> getLeaderboardServiceProvider() {
        return getInstance().leaderboardServiceProvider;
    }

    public static void setLeaderboardServiceProvider(
            final @NonNull Function<Leaderboard, @NonNull LeaderboardService> leaderboardServiceProvider) {
        getInstance().leaderboardServiceProvider = leaderboardServiceProvider;
    }

    public static void addPointsRegistry(final @NonNull PointsRegistry registry) {
        getInstance().registries.add(registry);
    }

    public static List<PointsRegistry> getPointsRegistries() {
        return Collections.unmodifiableList(getInstance().registries);
    }

    public static LeaderboardService getLeaderboardService() {
        return getInstance().service;
    }

    @Override
    public void onInit() {
        store.load();
        service = leaderboardServiceProvider.apply(leaderboardProvider.get());
        if (getConf().showLeaderboardOnJoin()) {
            Events.on(EventType.PlayerJoin.class, e -> service.showLeaderboard(e.player));
        }
    }

    @Override
    public void onClientCommandsRegistration(final CommandHandler handler) {
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
