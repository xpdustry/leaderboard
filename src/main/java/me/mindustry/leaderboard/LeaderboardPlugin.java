package me.mindustry.leaderboard;

import arc.*;
import arc.files.*;
import arc.util.*;
import io.leangen.geantyref.*;
import java.util.*;
import java.util.function.*;
import me.mindustry.leaderboard.repository.*;
import me.mindustry.leaderboard.service.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import net.mindustry_ddns.filestore.*;
import net.mindustry_ddns.filestore.serial.*;
import org.aeonbits.owner.*;
import org.jetbrains.annotations.*;

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
    new TypeToken<>() {
    },
    ConfigFactory.create(LeaderboardConfig.class)
  );

  public static void setCustomLeaderboardProvider(final @NotNull Supplier<@NotNull Leaderboard> customLeaderboardProvider) {
    LeaderboardPlugin.customLeaderboardProvider = customLeaderboardProvider;
  }

  public static Function<Leaderboard, LeaderboardService> getLeaderboardServiceFactory() {
    return leaderboardServiceFactory;
  }

  public static void setLeaderboardServiceFactory(final @NotNull Function<Leaderboard, @NotNull LeaderboardService> leaderboardServiceFactory) {
    LeaderboardPlugin.leaderboardServiceFactory = leaderboardServiceFactory;
  }

  public static void addPointsRegistry(final @NotNull PointsRegistry registry) {
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

    final var leaderboard = switch (getConf().getLeaderboardType()) {
      case IN_MEMORY -> Leaderboard.simple();
      case PERSISTENT -> Leaderboard.sqlite(LEADERBOARD_DIRECTORY.child("leaderboard.sqlite").file());
      case CUSTOM -> customLeaderboardProvider.get();
    };

    service = leaderboardServiceFactory.apply(leaderboard);
    if (getConf().showLeaderboardOnJoin()) {
      Events.on(PlayerJoin.class, e -> service.showLeaderboard(e.player));
    }
  }

  @Override
  public void registerClientCommands(final @NotNull CommandHandler handler) {
    handler.<Player>register("lb-rank", "Get your leaderboard status.", (args, player) -> {
      player.sendMessage(Strings.format("Rank: @, Points: @", service.getRank(player), service.getPoints(player)));
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

  private @NotNull LeaderboardConfig getConf() {
    return store.get();
  }
}
