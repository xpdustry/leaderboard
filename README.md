# leaderboard

[![Build](https://github.com/xpdustry/leaderboard/actions/workflows/build.yml/badge.svg)](https://github.com/Xpdustry/OmegaLeaderboardPlugin/actions/workflows/build.yml)
[![Mindustry 7.0 ](https://img.shields.io/badge/Mindustry-7.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)

## Description

A leaderboard plugin I wrote for Omega servers, now public.

## Installation

This plugin requires :

- Java 17 or above.

- Mindustry v146 or above.

- [Distributor](https://github.com/xpdustry/distributor) v3.2.1.

## Building

- `./gradlew jar` for a simple jar that contains only the plugin code.

- `./gradlew shadowJar` for a fatJar that contains the plugin and its dependencies (use this for your server).

## Testing

- `./gradlew runMindustryClient`: Run Mindustry in desktop with the plugin.

- `./gradlew runMindustryServer`: Run Mindustry in a server with the plugin.
