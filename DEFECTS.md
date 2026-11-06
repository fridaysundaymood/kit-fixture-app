# Deliberate defects

These are test inputs, not production behavior. The Android defects are selected by the `defects` flavor; the database defects require applying the separate SQL migration to a disposable local stack.

| ID | Intended detector | Location and behavior |
| --- | --- | --- |
| L1 | indonesian-english-linter RawIdDisplay | `app/src/defects/java/dev/androidkit/fixture/FixtureBehavior.kt:8` displays `status.id`. |
| L2 | indonesian-english-linter IdInTranslation | `app/src/defects/res/values-in/strings.xml:3` embeds the canonical `todo` id. |
| L3 | indonesian-english-linter ExceptionToUi | `app/src/defects/java/dev/androidkit/fixture/FixtureBehavior.kt:10` shows `error.message` in a Snackbar. |
| L4 | indonesian-english-linter LocalizedExceptionSwallowed | `app/src/defects/java/dev/androidkit/fixture/FixtureBehavior.kt:11` logs the localized message instead of showing it. |
| L5 | indonesian-english-linter MissingLocaleKey / format checks | `app/src/defects/res/values/strings.xml` and `app/src/defects/res/values-in/strings.xml:4`: a missing Indonesian key and incompatible `%d` / `%s` arguments. |
| L6 | indonesian-english-linter LocalizedDataLeak | `app/src/defects/java/dev/androidkit/fixture/FixtureBehavior.kt:9` supplies localized status to sync. |
| UI-UNLABELED | maestro-flow / emu-lab | `app/src/main/java/dev/androidkit/fixture/MainActivity.kt` settings icon has null contentDescription in defects. |
| UI-STALE | maestro-flow / emu-lab | `e2e/maestro/stale-add-task.yaml` selects obsolete `New task`; the app says `Add task`. |
| STATE-LEAK | emu-lab | `app/src/androidTestDefects/java/dev/androidkit/fixture/StateLeakTest.kt`: test B depends on test A's preferences. |
| CRASH | emu-lab | `app/src/main/java/dev/androidkit/fixture/MainActivity.kt` debug settings crash action. |
| DEFECT-RLS-001 | supa-multiuser-harness / supa-release-gate-mcp | `supabase/defects/20260103000000_cross_board_read_and_counter_race.sql:2` permits cross-board reads. |
| DEFECT-RACE-001 | supa-multiuser-harness | `supabase/defects/20260103000000_cross_board_read_and_counter_race.sql:5` replaces atomic increment with read/modify/write. |
