package me.mindustry.leaderboard.util;

import java.util.*;
import mindustry.gen.*;

public class TestPlayer extends Player {

  private final String uuid = UUID.randomUUID().toString();

  @Override
  public String uuid() {
    return uuid;
  }
}
