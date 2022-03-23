package me.mindustry.leaderboard;

import arc.*;
import arc.util.*;
import com.googlecode.cqengine.persistence.support.serialization.*;
import java.io.*;
import java.util.*;
import me.mindustry.leaderboard.core.*;
import me.mindustry.leaderboard.internal.*;
import me.mindustry.leaderboard.internal.LeaderboardConfig.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.world.blocks.storage.*;
import net.mindustry_ddns.filestore.*;
import org.jetbrains.annotations.*;
import rhino.*;

@SuppressWarnings({"unused", "NullAway.Init"})
public final class LeaderboardPlugin extends Plugin {

  private static final String LEADERBOARD_ENABLED_KEY = "omega-leaderboard-active";

  private final File leaderboardDirectory = new File("./leaderboard");
  private final FileStore<LeaderboardConfig> store = new ConfigFileStore<>("./leaderboard/config.properties", LeaderboardConfig.class);
  private final File leaderboardFile = new File("./leaderboard/data.dat");
  private Leaderboard leaderboard;

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public void init() {
    leaderboardDirectory.mkdirs();

    store.load();

    if (getConf().getLeaderboardType() == LeaderboardType.PERSISTENT) {
      KryoSerializer.validateObjectIsRoundTripSerializable(LeaderboardPlayer.simple(Player.create()));
    }

    leaderboard = switch (getConf().getLeaderboardType()) {
      case IN_MEMORY -> Leaderboard.inMemory();
      case INDEXED -> Leaderboard.indexed();
      case PERSISTENT -> Leaderboard.persistent(leaderboardFile);
    };

    // Puts the plugin in the script scope
    final Scriptable scope = Reflect.get(Vars.mods.getScripts(), "scope");
    ScriptableObject.putConstProperty(scope, "lb", this);

    Events.on(UnitSpawnEvent.class, e -> {
      if (e.unit.isPlayer()) {
        leaderboard.getPlayer(e.unit.getPlayer()).applyPoints(DefaultLeaderboardPoints.PLAYER_DIED);
      }
    });

    Events.on(BlockDestroyEvent.class, e -> {
      if (isEnabled() && e.tile.build instanceof CoreBlock.CoreBuild build) {
        Groups.player.each(
          p -> p.team() == build.team(),
          p -> leaderboard.getPlayer(p).applyPoints(DefaultLeaderboardPoints.DESTROYED_CORE)
        );
      }
    });

    Events.on(BlockBuildEndEvent.class, e -> {
      if (e.unit.isPlayer()) {
        leaderboard.getPlayer(e.unit.getPlayer()).applyPoints(DefaultLeaderboardPoints.BUILD_BLOCK);
      }
    });

    Events.on(GameOverEvent.class, e -> {
      if (isEnabled()) {
        Groups.player.each(
          p -> p.team() == e.winner,
          p -> leaderboard.getPlayer(p).applyPoints(DefaultLeaderboardPoints.WON_GAME)
        );
      }
    });

    Events.on(PlayerJoin.class, e -> {
      if (isEnabled()) {
        final var popup = new StringBuilder().append("Leader Board:");
        final var page = leaderboard.getPage(0, 10);
        for (int i = 0; i < page.size(); i++) {
          final var leaderboardPlayer = page.get(i);
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
      final var leaderboardPlayer = leaderboard.getPlayer(player);
      final var rank = leaderboard.getRank(player);
      player.sendMessage(Strings.format("Rank: @, Points: @", rank == -1 ? "None" : rank, leaderboardPlayer.getPoints()));
    });
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
