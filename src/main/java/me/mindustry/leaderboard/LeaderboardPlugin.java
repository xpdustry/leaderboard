package me.mindustry.leaderboard;

import arc.*;
import arc.files.*;
import arc.util.*;
import com.j256.ormlite.logger.*;
import io.leangen.geantyref.*;
import java.util.*;
import me.mindustry.leaderboard.repository.*;
import me.mindustry.leaderboard.service.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.world.blocks.storage.*;
import net.mindustry_ddns.filestore.*;
import net.mindustry_ddns.filestore.serial.*;
import org.aeonbits.owner.*;
import org.jetbrains.annotations.*;
import rhino.*;

@SuppressWarnings("unused")
public final class LeaderboardPlugin extends Plugin {

  private static final String LEADERBOARD_ENABLED_KEY = "omega-leaderboard:active";

  private final Fi leaderboardDirectory = new Fi("./leaderboard");
  private final Store<LeaderboardConfig> store = FileStore.of(
    leaderboardDirectory.child("config.properties").file(),
    Serializers.config(),
    new TypeToken<>(){},
    ConfigFactory.create(LeaderboardConfig.class)
  );

  @SuppressWarnings("NullAway.Init")
  private Leaderboard leaderboard;
  @SuppressWarnings("NullAway.Init")
  private LeaderboardService service;

  @Override
  public void init() {
    leaderboardDirectory.mkdirs();
    store.load();
    Logger.setGlobalLogLevel(Level.ERROR);

    leaderboard = switch (getConf().getLeaderboardType()) {
      case IN_MEMORY -> Leaderboard.simple();
      case PERSISTENT -> Leaderboard.sqlite(leaderboardDirectory.child("leaderboard.db").file());
    };

    service = LeaderboardService.simple(leaderboard);

    // Puts the plugin in the script scope
    final Scriptable scope = Reflect.get(Vars.mods.getScripts(), "scope");
    ScriptableObject.putConstProperty(scope, "lb", this);

    Events.on(BlockDestroyEvent.class, e -> {
      if (isEnabled() && Vars.state.rules.pvp && e.tile.build instanceof CoreBlock.CoreBuild build) {
        Groups.player.each(
          p -> p.team() == build.team(),
          p -> service.grantPoints(p, StandardLeaderboardPoints.DESTROYED_CORE)
        );
      }
    });

    Events.on(BlockBuildEndEvent.class, e -> {
      if (isEnabled() && e.unit.isPlayer()) {
        service.grantPoints(e.unit.getPlayer(), StandardLeaderboardPoints.BUILD_BLOCK);
      }
    });

    Events.on(GameOverEvent.class, e -> {
      if (isEnabled() && Vars.state.rules.pvp) {
        Groups.player.each(
          p -> p.team() == e.winner,
          p -> service.grantPoints(p, StandardLeaderboardPoints.PVP_VICTORY)
        );
      }
    });

    Events.on(PlayerJoin.class, e -> {
      if (isEnabled()) {
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

          popup.append("\n#").append(i);
          popup.append(" [white]: ").append(playerName).append(" - ").append(leaderboardPlayer.getPoints());
        }
        Call.infoToast(e.player.con, popup.toString(), 10f);
      }
    });
  }

  @Override
  public void registerClientCommands(final @NotNull CommandHandler handler) {
    handler.<Player>register("lb-status", "[status]", "Enable/Disable the leaderboard.", (args, player) -> {
      if (player.admin()) {
        if (args.length == 0) {
          player.sendMessage(Strings.format("The leaderboard is currently @.", isEnabled() ? "enabled" : "disabled"));
        } else {
          switch (args[0].toLowerCase(Locale.ROOT)) {
            case "true", "active", "enable" -> setEnabled(true);
            case "false", "inactive", "disable" -> setEnabled(false);
          }
          player.sendMessage(Strings.format("The leaderboard is now @.", isEnabled() ? "enabled" : "disabled"));
        }
      } else {
        player.sendMessage("[red]Only an admin can use this command");
      }
    });

    handler.<Player>register("lb-rank", "Get your leaderboard status.", (args, player) -> {
      if (isEnabled()) {
        final var leaderboardPlayer = service.getLeaderboard().getPlayer(player.uuid());
        final var rank = service.getRank(player);
        player.sendMessage(
          Strings.format(
            "Rank: @, Points: @",
            rank == -1 ? "None" : rank + 1,
            leaderboardPlayer != null ? leaderboardPlayer.getPoints() : 0
          )
        );
      } else {
        player.sendMessage("[red]The leaderboard is currently disabled.");
      }
    });
  }

  public Leaderboard getLeaderboard() {
    return leaderboard;
  }

  public LeaderboardService getLeaderboardService() {
    return service;
  }

  public @NotNull LeaderboardConfig getConf() {
    return store.get();
  }

  public boolean isEnabled() {
    return Core.settings.getBool(LEADERBOARD_ENABLED_KEY, false);
  }

  public void setEnabled(final boolean active) {
    Core.settings.put(LEADERBOARD_ENABLED_KEY, active);
  }
}
