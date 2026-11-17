# Feature requests

Two feature requests from the product owner are waiting to be built. They are alternatives:
choose **one** of them and implement it. Each request is followed by the acceptance criteria the
finished feature must meet.

---

## Option A: Throwable Potions

Players keep asking why they can drink a potion of poison but can't throw it at the thing trying
to eat them. Please let players throw potions at monsters. A harmful potion should affect the
monster it hits, and a beneficial one should just be wasted. That makes throwing an unidentified
potion a real gamble. Saves and replays must keep working exactly as they do now.

### Acceptance criteria

1. Any potion in the pack can be thrown with the existing Throw command. It picks its target the
   same way thrown darts do, with the same range, and it is refused under the same conditions
   (for example, when nothing is in range). A successful throw takes one normal turn and removes
   exactly one potion from its stack.
2. A thrown potion never misses and always shatters. It does not stay on the floor afterward. If its flight reaches the target, it shatters on that monster. If something blocks it first, it shatters where its flight stopped without affecting a creature, and the game shows a message saying it fell short.
3. A potion that shatters on a monster affects only that monster:
   - **Poison:** poisoned for 6 turns at strength 2.
   - **Confusion:** confused for 8 turns.
   - **Blindness:** blinded for 10 turns.
   - **Liquid fire:** sets the monster on fire.
   - **Any other kind:** no effect, and the message says the potion was wasted.
4. Existing status-effect rules still apply to thrown potions. Monsters immune to poison or fire
   are unaffected, a wet creature cannot be set on fire, and repeated effects stack exactly as
   they do today.
5. Liquid fire also ignites tall grass on the tile where it shatters. This happens whether it hits
   the monster or falls short.
6. Throwing a harmful potion that affects the visible target identifies that potion kind for the
   rest of the run. Throwing a beneficial potion, or a potion that has no effect, does not
   identify it.
7. A monster hit by a thrown potion becomes awake and hostile toward the player, the same as when
   it is hit by a thrown dart. This applies even if the potion had no effect.
8. Throwing darts and other missiles works exactly as before. Runs that include potion throws
   save, continue and replay with identical results, and any randomness the feature uses keeps a
   given seed's outcomes the same every time.

---

## Option B: Shrine Altars

Players find shrine altars on the lower floors, but right now they're just decoration. Let's make
them useful. Dropping an item on a shrine altar should reveal what it really is, and an altar
should be able to cleanse a curse. Each altar has only limited power before it goes dormant. This
is a pure rules change: no new screens, and existing seeds must generate exactly the same
dungeons.

### Acceptance criteria

1. Every shrine altar on floors 1–9 starts with 3 charges. The altar tiles in the Atrium and the
   Sanctum have no powers. Dropping items on them works like dropping anywhere else.
2. Dropping an unidentified item on a shrine altar with at least 1 charge identifies it and uses
   1 charge:
   - **Weapon or armor:** its upgrade level is revealed.
   - **Potion or scroll:** its whole kind becomes identified for the rest of the run.

   The message reports what was revealed and says plainly whether a weapon or armor is cursed.
3. Dropping a weapon or armor whose curse is already known, on an altar with at least 2 charges,
   lifts the curse and uses 2 charges. Lifting a curse raises the item's upgrade level to +0, the
   same as other ways of removing curses. If the altar has exactly 1 charge, nothing happens to
   the item and no charge is used.
4. An unidentified cursed item is only identified on the drop that reveals it (1 charge). Its
   curse can be lifted on a later drop.
5. Dropping anything else (an item that's already identified and not cursed, gold, or a quest
   item), or dropping anything on an altar with 0 charges, works exactly like a normal drop and
   uses no charge. An altar with 0 charges is dormant.
6. Each altar keeps its own charge count. The count stays the same when the player leaves the
   floor and returns, and when they save and continue.
7. A scroll kind identified at an altar counts as known everywhere a known scroll matters,
   including Scribe Irani's "Lost in Translation" quest progress.
8. Examining a shrine altar in look mode shows how many charges it has left, or says it is
   dormant. Existing seeds still generate identical floors, monsters and items, and run records
   that never drop an item on a shrine altar replay exactly as before.
