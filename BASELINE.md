# Halls of 7800 Quality Baseline

Reviewed commit `2a9a375` on November 17, 2026. The build and headless checks ran on Windows with JDK 24, targeting Java 17. JavaFX play, PIT mutation testing, and SpotBugs were outside this review.

## Code quality

- The game rules run independently of JavaFX. Small command classes, fixed test floors, and seeded random streams make gameplay behavior easier to isolate and reproduce.
- `GameSession` spans 841 lines and coordinates turns, floor changes, combat outcomes, trading, progression, and replay recording. Changes to turn handling can affect several of those systems at once. [GameSession]

## Test coverage

- `.\mvnw.cmd -B clean test` passed all **14 tests** with zero failures or skips. JaCoCo measured **30.5% line coverage** (1,846 of 6,056) and **20.6% branch coverage** (665 of 3,228). [Local JaCoCo report](target/site/jacoco/index.html)
- Line coverage reaches **72.3%** in `world` and **51.2%** in `gen`, then drops to **30.6%** in `engine`, **19.9%** in `persist`, and **17.8%** in `quest`. `Shop`, `SaveStore`, `StairsCommand`, `BossAi`, and the application and UI packages have zero line coverage.
- Generation testing compares one seed at one depth. The replay test checks turn, position, and rendered map after basic commands. [Tests](src/test/java/edu/towson/cosc442/halls7800)

## Weaknesses and risks

Headless checks reproduced the failure paths in [BUGS.md](BUGS.md).

| Area | Observed behavior |
| --- | --- |
| Save and continue | A controlled snare check restored turn 1 with the player still stunned after saving on turn 2. Stunned turns advance the simulation without entering the action log. [GameSession] |
| Shop | A full-pack armor purchase removed the gold and stock even though the player received no item. [Shop] |
| Quest progress | Three requirements pages in one stack counted as one page. [QuestLog] |
| Combat | A fully absorbed hit still inflicted poison because the effect check uses damage before shielding. [CombatResolver] |
| Guardian progression | On seed 4, a trapdoor dropped the player from depth 3 to 4 with the Hydra alive. The stairs correctly refused descent. [Traps] |

## Further investigation

- Check whether existing saves survive changes to game rules or random rolls. Start with a multi-floor replay that checks health, effects, inventory, and monster state after Continue. [ReplayRunner]
- Sweep multiple seeds and difficulties for reachable exits, guardian placement, and required quest items, including fallback layouts. [DungeonGenerator]
- Verify CI on a clean Linux checkout. The workflow runs `./mvnw` directly, but Git tracks it without the executable bit. The workflow also has no coverage threshold. [Quality workflow](.github/workflows/quality.yml)

[GameSession]: src/main/java/edu/towson/cosc442/halls7800/engine/GameSession.java
[Shop]: src/main/java/edu/towson/cosc442/halls7800/quest/Shop.java
[QuestLog]: src/main/java/edu/towson/cosc442/halls7800/quest/QuestLog.java
[CombatResolver]: src/main/java/edu/towson/cosc442/halls7800/combat/CombatResolver.java
[Traps]: src/main/java/edu/towson/cosc442/halls7800/engine/Traps.java
[ReplayRunner]: src/main/java/edu/towson/cosc442/halls7800/persist/ReplayRunner.java
[DungeonGenerator]: src/main/java/edu/towson/cosc442/halls7800/gen/DungeonGenerator.java
