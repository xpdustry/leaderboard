# OmegaLeaderboardPlugin

[![Build](https://github.com/Xpdustry/OmegaLeaderboardPlugin/actions/workflows/build.yml/badge.svg)](https://github.com/Xpdustry/OmegaLeaderboardPlugin/actions/workflows/build.yml)
[![Mindustry 6.0 | 7.0 ](https://img.shields.io/badge/Mindustry-6.0%20%7C%207.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)

## Description

Leaderboard plugin for Omega servers.

## Building

- `./gradlew jar` for a simple jar that contains only the plugin code.

- `./gradlew shadowJar` for a fatJar that contains the plugin and its dependencies (use this for your server).

## Testing

- `./gradlew runMindustryClient`: Run Mindustry in desktop with the plugin.

- `./gradlew runMindustryServer`: Run Mindustry in a server with the plugin.

## Running

This plugin is compatible with V6 and V7, but it requires [Flex](https://github.com/Xpdustry/Flex) as dependency.

> If you run Mindustry below v136, you will need an additional mod called [mod-loader](https://github.com/Xpdustry/ModLoaderPlugin) for the dependency resolution.
