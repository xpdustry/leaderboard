package me.mindustry.leaderboard;

import arc.*;
import java.lang.reflect.*;
import java.util.*;
import me.mindustry.leaderboard.model.*;
import me.mindustry.leaderboard.repository.*;
import me.mindustry.leaderboard.service.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

final class OmegaLeaderboardPoints implements LeaderboardPointsRegistry {

  private static final LeaderboardPoints PVP_VICTORY = LeaderboardPoints.of("Victory", +100);
  private static final LeaderboardPoints PVP_DEFEAT = LeaderboardPoints.of("Defeat", -100);

  private static final List<LeaderboardPoints> ALL = new ArrayList<>();

  static final OmegaLeaderboardPoints INSTANCE = new OmegaLeaderboardPoints();

  static {
    for(final var field : OmegaLeaderboardPoints.class.getDeclaredFields())  {
      if (field.getType().equals(LeaderboardPoints.class) && Modifier.isStatic(field.getModifiers())) {
        try {
          ALL.add((LeaderboardPoints) field.get(null));
        } catch (final Exception e) {
          throw new RuntimeException("This ain't supposed to happen (Failed to register the points).", e);
        }
      }
    }
  }

  private OmegaLeaderboardPoints() {
  }

  @Override
  public @NotNull Collection<LeaderboardPoints> getLeaderboardPoints() {
    return ALL;
  }

  @Override
  public void registerLeaderboardPoints(@NotNull LeaderboardService service) {
    Events.on(GameOverEvent.class, e -> {
      if (Vars.state.rules.pvp) {
        Groups.player.each(p -> service.grantPoints(p, p.team() == e.winner ? PVP_VICTORY : PVP_DEFEAT));
      }
    });
  }
}
