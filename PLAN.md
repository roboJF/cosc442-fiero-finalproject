# Halls of 7800 Quality Improvement Plan

The review covered all production packages and the test suite, with headless checks for replay, turn timing, state ownership, and floor generation. Add regression tests as each fix is made, then use them to verify the refactors. [Beta reports](BUGS.md) · [Quality baseline](BASELINE.md)

## 1. Protect save and replay integrity (Completed)

Continue rebuilds the game from the seed and action log. The log now includes inputs that consume a stunned turn, and replay rejects malformed tokens with the one-based action number. Both exit paths keep the active run available for retry if saving fails. [ReplayRunner] · [CommandCodec] · [HallsApp]

- Regression tests cover a snare save, changes on a revisited floor, invalid actions, a version 1 save, and both exit handlers with an unwritable save location.
- Version 1 records remain readable. Turns omitted by earlier builds cannot be recovered from the action log. [Save and replay notes](README.md#seeds-saves-and-replays)

## 2. Refactor action rules shared by the engine and UI (Completed)

New runs decide whether a creature can act before its stun timer ticks. Typed and pressed shortcuts pass through the same input modes. Stairs and trapdoors both check for a living guardian before descent. [MonsterAi] · [GameView] · [GameSession] · [Traps]

- Regression tests cover one-turn stuns, effect expiry, boss phase changes, and the seed 4 Hydra encounter. A JavaFX smoke test confirmed that typed `>` and Page Down are blocked during examine and perk selection, and that typed stairs work in normal play.
- Version 2 introduced the updated stun and guardian rules. Version 1 runs keep the original stun and trapdoor rules when replayed, continued, and saved again. [Save and replay notes](README.md#seeds-saves-and-replays)

## 3. Expand regression coverage across systems (Completed)

The suite grew from 43 to 62 test cases. The new tests use fixed maps and scripted combat rolls, and check that stairs, guardians, requirements pages, and vault keys stay reachable across 432 generated floors. Direct fallback tests cover connectivity, borders, optional stairs, and progression. The fallback layouts still fall below the usual open-tile minimum. [Quality baseline](BASELINE.md#test-coverage) · [DungeonGenerator]

- GitHub Actions enables the JavaFX smoke test under Xvfb. It checks that a player can start a run, save to a temporary directory, continue from the title screen, and save again with both actions recorded. [Quality workflow](.github/workflows/quality.yml)
- JaCoCo line coverage rose from 46.53% to 57.99% and branch coverage rose from 37.74% to 46.43% with the smoke test enabled. Headless coverage reached 47.88% of lines and 40.66% of branches. This work raised the CI minimums to 45% line and 38% branch coverage.
- Across the classes below, PIT killed 104 of 219 mutants, up from 55. Mutants without coverage fell from 87 to 61. The JavaFX smoke test contributes to JaCoCo coverage and runs outside PIT.

| Class | JaCoCo branches covered | PIT survivors |
| --- | ---: | ---: |
| `CombatResolver` | 12/30 → 21/30 | 12 → 7 |
| `DungeonGenerator` | 5/14 → 10/14 | 2 → 3 |
| `FloorValidator` | 24/38 → 35/38 | 20 → 10 |
| `LootPlacer` | 28/33 → 30/33 | 29 → 21 |
| `ResidentPlacer` | 18/28 → 21/28 | 13 → 12 |
| `HallsApp` | 7/30 → 16/30 | 1 → 1 |

## 4. Fix the gameplay rules that cost players progress (Completed)

Conover charges for an item after the pack accepts it. A stack can still accept matching items when every slot is occupied. The Archivist counts the pages in a stack and grants the reward once. On-hit effects require damage to get through the shield. [Beta reports](BUGS.md)

- Regression tests cover rejected armor and unfamiliar potions, a potion purchase that merges into a full pack, stacked pages and repeat turn-in, and poison on fully and partially absorbed hits.
- New runs use version 3 records. Version 1 and 2 records keep their original gameplay rules and combat roll sequence when replayed or continued. [Save and replay notes](README.md#seeds-saves-and-replays)
- JaCoCo reached 60.07% line and 48.24% branch coverage with the smoke test enabled. Headless coverage reached 50.08% line and 42.52% branch. This work raised the CI minimums to 47% line and 40% branch coverage.

## 5. Give the engine control of mutable game state

`FloorContents.piles()` wraps the map but exposes the lists inside it. A headless probe emptied a pile through that view and left an empty entry behind. This lets callers bypass the engine's item-removal rules and replay recording. [FloorContents]

- Give the UI read-only item and floor data, and keep writes behind engine methods. Test that callers cannot change piles through returned views. Review the 13 remaining SpotBugs findings about shared mutable references, including references used by the UI to display the live game.

[ReplayRunner]: src/main/java/edu/towson/cosc442/halls7800/persist/ReplayRunner.java
[CommandCodec]: src/main/java/edu/towson/cosc442/halls7800/engine/CommandCodec.java
[HallsApp]: src/main/java/edu/towson/cosc442/halls7800/app/HallsApp.java
[MonsterAi]: src/main/java/edu/towson/cosc442/halls7800/entity/MonsterAi.java
[GameView]: src/main/java/edu/towson/cosc442/halls7800/ui/GameView.java
[GameSession]: src/main/java/edu/towson/cosc442/halls7800/engine/GameSession.java
[Traps]: src/main/java/edu/towson/cosc442/halls7800/engine/Traps.java
[DungeonGenerator]: src/main/java/edu/towson/cosc442/halls7800/gen/DungeonGenerator.java
[FloorContents]: src/main/java/edu/towson/cosc442/halls7800/engine/FloorContents.java
