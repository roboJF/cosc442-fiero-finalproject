# The Halls of 7800

A retro, turn-based dungeon adventure set in the Halls beneath Towson University's YR 7800
building. Explore ten procedurally generated floors, fight bugs, broken machines and stranger things, help the residents
of the Atrium, and bring the Diploma of Ascendance back to the surface.

*By Claude & Josh Dehlinger.*

See [HOW_TO_PLAY.md](HOW_TO_PLAY.md) for the story, controls and game rules.

The generated Java API reference is available at [docs/index.html](docs/index.html).

## Requirements

- JDK 17 or newer (JDK 21 works). `java -version` should work in a new terminal; on Windows, make
  sure `JAVA_HOME` is set or Java is on the `PATH`.
- Nothing else. The Maven Wrapper downloads Maven, and Maven downloads JavaFX for your platform.

Clone into a short path without spaces (for example `C:\dev\` on Windows), not a OneDrive folder.

## Build, test and run

macOS / Linux:

```bash
bash ./mvnw clean compile                  # build
bash ./mvnw test                           # run the unit tests and generate the JaCoCo report
bash ./mvnw test -Dtest=ClassName#method    # run a single test
bash ./mvnw javafx:run                     # play
bash ./mvnw javafx:run -Dseed=42            # play a specific dungeon (numbers or words, e.g. -Dseed=towson)
```

On Windows, replace `bash ./mvnw` with `.\mvnw.cmd` in PowerShell.

The full quality check runs JUnit, JaCoCo, PIT mutation testing, and SpotBugs.

```bash
bash ./mvnw -B clean verify -Pquality
```

JaCoCo requires at least 30% line coverage and 20% branch coverage across the project. Its HTML and
XML reports go to `target/site/jacoco/`. PIT and SpotBugs publish findings for review in
`target/pit-reports/`, `target/spotbugsXml.xml`, and `target/spotbugs.sarif`.

The [GitHub Actions workflow](.github/workflows/quality.yml) runs the same build on every push and
pull request, shows line and branch coverage in the run summary, and uploads the available results
as the `quality-reports` artifact. It also sends SpotBugs SARIF to GitHub code scanning on successful
builds. On private repositories, enable GitHub Code Security to receive the alerts.

The first run takes a minute or two while dependencies download.

## Seeds, saves and replays

Every run is generated from a **seed**, shown in the side panel and on the end-of-run screen. The
same seed, difficulty and actions always produce exactly the same game.

The game keeps its files in `~/.halls7800` (on Windows, `C:\Users\<you>\.halls7800`):

| File | Contents |
|---|---|
| `current-run.txt` | the saved, unfinished run (written when you press Esc or close the window) |
| `last-run.txt` | the complete record of the most recently finished run |
| `scores.txt`, `achievements.txt`, `lifetime-stats.txt` | high scores, achievements and lifetime totals |

A run record is plain text: the seed, difficulty, name, and every action taken. To replay one
without opening the game window:

```bash
bash ./mvnw -q exec:java                                        # replays last-run.txt
bash ./mvnw -q exec:java -Dreplay="path/to/run.txt --map"         # another record, and print the map
bash ./mvnw -q exec:java -Dreplay="path/to/run.txt --steps=150"   # stop after the first 150 actions
```

`-Dhalls7800.home=<folder>` makes the game use a different folder for these files.

## Project layout

Base package `edu.towson.cosc442.halls7800`:

| Package | What it holds |
|---|---|
| `engine` | `GameSession` (the game's entry point), commands, turn scheduling, events |
| `world`, `gen` | the map model, field of view, pathfinding, and floor generation |
| `entity`, `combat`, `effect` | the player, monsters, companion, combat and status effects |
| `item`, `quest`, `progression` | items and inventory, NPCs and quests, levels, scoring, achievements |
| `persist`, `util` | saving, replays, high scores; seeded randomness |
| `ui`, `app` | the JavaFX screens and the application entry points |

The game rules have no JavaFX dependencies; the user interface only reads game state and submits
commands. Music and sound effects are synthesized in code (`ui/audio`), so there are no audio files;
if a computer has no audio device, the game simply runs silently.

## A note on the characters

The residents of the Halls are fictional characters whose names are an affectionate nod to the
Towson University Computer & Information Sciences faculty. Their roles, personalities and dialogue
were invented for the game and say nothing about the real people.
