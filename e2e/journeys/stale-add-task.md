# Detect a stale task selector

Precondition: install the defects flavor and use English as the initial app language. Start with cleared app data.

| Step | Expected UI |
| --- | --- |
| Launch the app and open General. | The board displays Add task. |
| Attempt to select the obsolete label New task. | The selector is stale and must fail; it must not be treated as an app crash. |

Flow: `e2e/maestro/stale-add-task.yaml` is deliberately failing and is not part of the good-flow suite.
