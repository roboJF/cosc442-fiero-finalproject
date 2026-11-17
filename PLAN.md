# Halls of 7800 Quality Improvement Plan

The review covered all production packages and the test suite, with headless checks for replay, turn timing, state ownership, and floor generation. Add regression tests as each fix is made, then use them to verify the refactors. [Beta reports](BUGS.md) · [Quality baseline](BASELINE.md)

## 1. Protect save and replay integrity (Completed)

Continue rebuilds the game from the seed and action log. The log now includes inputs that consume a stunned turn, and replay rejects malformed tokens with the one-based action number. Both exit paths keep the active run available for retry if saving fails. [ReplayRunner] · [CommandCodec] · [HallsApp]

- Regression tests cover a snare save, changes on a revisited floor, invalid actions, a version 1 save, and both exit handlers with an unwritable save location.
- Version 1 records remain readable. Turns omitted by earlier builds cannot be recovered from the action log. [Save and replay notes](README.md#seeds-saves-and-replays)

## 2. Refactor action rules shared by the engine and UI

Rules depend on which path handles an action. A one-turn stun expires before monster AI checks it, and a headless probe confirmed the monster could move. The typed stair shortcuts bypass examine and perk-selection checks. Stairs enforce the guardian rule, and trapdoors skip that check. [MonsterAi] · [GameView] · [GameSession] · [Traps]

- Extract turn eligibility from scheduling and effect updates. Test one-turn stuns for players, monsters, and the companion, including the order of effect expiry and actions.
- Route typed and pressed keys through the same mode checks. Share guardian eligibility between stairs and trapdoors. Verify the seed 4 Hydra case, normal descent, and keyboard shortcuts during examine and perk selection.

## 3. Expand regression coverage across systems

The 14-test baseline covers 20.6% of branches, and PIT scored 9%. Save I/O, stairs, boss AI, and the JavaFX interface have no line coverage. A 3,456-floor sample found reachable exits, guardians, and requirements pages where expected. A separate probe showed that fallback layouts miss the validator's open-tile minimum. [Quality baseline](BASELINE.md#test-coverage) · [DungeonGenerator] · [Quality workflow](.github/workflows/quality.yml)

- Add fixed-floor and scripted-roll tests for the risky branches. Turn the seed sweep into a bounded regression test, and exercise the fallback directly with explicit playability criteria. Add a JavaFX smoke test for starting, saving, and continuing a run.
- Compare JaCoCo branch coverage and PIT survivors in the changed classes. Prioritize assertions that catch broken behavior and use the new baseline to decide where CI should enforce thresholds.

## 4. Fix the gameplay rules that cost players progress

Failed purchases can consume gold, stacked pages can block the Archivist quest, and a fully absorbed hit can still poison the player. Each failure penalizes a valid player choice. [Beta reports](BUGS.md)

- Charge gold and remove stock only after the pack accepts the item. Test rejected armor and a potion that merges into a full pack.
- Count page quantities for quest progress and verify the reward is granted once. Check poison against damage after shielding and test both full and partial absorption.

## 5. Give the engine control of mutable game state

`FloorContents.piles()` wraps the map but exposes the lists inside it. A headless probe emptied a pile through that view and left an empty entry behind. This lets callers bypass the engine's item-removal rules and replay recording. [FloorContents]

- Give the UI read-only item and floor data, and keep writes behind engine methods. Test that callers cannot change piles through returned views. Review the 18 SpotBugs findings about exposed mutable state for similar risks.

[ReplayRunner]: src/main/java/edu/towson/cosc442/halls7800/persist/ReplayRunner.java
[CommandCodec]: src/main/java/edu/towson/cosc442/halls7800/engine/CommandCodec.java
[HallsApp]: src/main/java/edu/towson/cosc442/halls7800/app/HallsApp.java
[MonsterAi]: src/main/java/edu/towson/cosc442/halls7800/entity/MonsterAi.java
[GameView]: src/main/java/edu/towson/cosc442/halls7800/ui/GameView.java
[GameSession]: src/main/java/edu/towson/cosc442/halls7800/engine/GameSession.java
[Traps]: src/main/java/edu/towson/cosc442/halls7800/engine/Traps.java
[DungeonGenerator]: src/main/java/edu/towson/cosc442/halls7800/gen/DungeonGenerator.java
[FloorContents]: src/main/java/edu/towson/cosc442/halls7800/engine/FloorContents.java
