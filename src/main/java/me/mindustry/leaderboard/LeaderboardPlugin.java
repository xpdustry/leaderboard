package me.mindustry.leaderboard;

import arc.*;
import arc.files.*;
import arc.util.*;
import fr.xpdustry.flex.*;
import io.leangen.geantyref.*;
import java.util.function.Function;
import java.util.function.*;
import me.mindustry.leaderboard.repository.*;
import me.mindustry.leaderboard.service.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import net.mindustry_ddns.filestore.*;
import net.mindustry_ddns.filestore.serial.*;
import org.aeonbits.owner.*;
import org.jetbrains.annotations.*;
import rhino.*;

@SuppressWarnings("unused")
public final class LeaderboardPlugin extends Plugin {

  private static final Fi LEADERBOARD_DIRECTORY = new Fi("./leaderboard");
  private static final String LEADERBOARD_ENABLED_KEY = "omega-leaderboard:active";

  private final Store<LeaderboardConfig> store = FileStore.of(
    LEADERBOARD_DIRECTORY.child("config.properties").file(),
    Serializers.config(),
    new TypeToken<>() {},
    ConfigFactory.create(LeaderboardConfig.class)
  );

  @SuppressWarnings("NullAway.Init")
  private LeaderboardService service;
  private Function<Leaderboard, LeaderboardService> leaderboardServiceFactory = LeaderboardService::simple;
  private Supplier<Leaderboard> customLeaderboardProvider = () -> {
    throw new IllegalStateException("The custom leaderboard provider hasn't been set.");
  };

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

    // Puts the plugin in the script scope
    final Scriptable scope = Reflect.get(Vars.mods.getScripts(), "scope");
    ScriptableObject.putConstProperty(scope, "lb", service);

    Events.on(PlayerJoin.class, e -> showLeaderboard(e.player));

    addRegistry(OmegaLeaderboardPoints.INSTANCE);
    FlexPlugin.registerFlexExtension(new LeaderboardFlexExtension(service));
  }

  @Override
  public void registerClientCommands(final @NotNull CommandHandler handler) {
    handler.<Player>register("lb-rank", "Get your leaderboard status.", (args, player) -> {
      final var leaderboardPlayer = service.getLeaderboard().addPlayer(player.uuid());
      final var rank = service.getRank(player);
      player.sendMessage(Strings.format("Rank: @, Points: @", rank, leaderboardPlayer.getPoints()));
    });

    handler.<Player>register("lb-board", "Show the leaderboard", (args, player) -> {
      showLeaderboard(player);
    });
  }

  public LeaderboardService getLeaderboardService() {
    return service;
  }

  public void setCustomLeaderboardProvider(final @NotNull Supplier<@NotNull Leaderboard> customLeaderboardProvider) {
    this.customLeaderboardProvider = customLeaderboardProvider;
  }

  public Function<Leaderboard, LeaderboardService> getLeaderboardServiceFactory() {
    return leaderboardServiceFactory;
  }

  public void setLeaderboardServiceFactory(final @NotNull Function<Leaderboard, @NotNull LeaderboardService> leaderboardServiceFactory) {
    this.leaderboardServiceFactory = leaderboardServiceFactory;
  }

  public @NotNull LeaderboardConfig getConf() {
    return store.get();
  }

  public void addRegistry(final @NotNull LeaderboardPointsRegistry registry) {
    registry.registerLeaderboardPoints(service);
  }

  private void showLeaderboard(final @NotNull Player player) {
    final var popup = new StringBuilder().append("[yellow]Leader Board:[]");
    final var players = service.getLeaderboard().getPlayers();
    for (int i = 0; i < Math.min(10, players.size()); i++) {
      final var leaderboardPlayer = players.get(i);
      final String playerName;

      final var localPlayer = Groups.player.find(p -> p.uuid().equals(leaderboardPlayer.getUuid()));
      if (localPlayer != null) {
        playerName = localPlayer.name();
      } else {
        playerName = Vars.netServer.admins.getInfo(leaderboardPlayer.getUuid()).lastName;
      }

      popup.append("\n#").append(i + 1);
      popup.append(" [white]: ").append(playerName).append(" - ").append(leaderboardPlayer.getPoints());
    }
    Call.infoToast(player.con, popup.toString(), 10f);
  }
}
