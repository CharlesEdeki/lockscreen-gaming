# Lock Arcade

Plays a random retro mini-game over your Android lock screen when the screen turns on.

## Layout

| Module | Type | Purpose |
|---|---|---|
| `core:engine` | pure Kotlin/JVM | `Game` interface, fixed-timestep `GameRunner`, gestures, `GameCatalog` |
| `games:*` | pure Kotlin/JVM | One module per game; no Android dependencies, unit-tested on the JVM |
| `feature:lockscreen` | Android library | `GameView` renderer, `LockGameActivity`, `ScreenWatchService` |
| `app` | Android app | Setup screen and the catalog wiring (`ArcadeApp`) |

## Add a game (about 5 minutes)

1. Copy `games/dodge` to `games/<name>`, rename the package and class.
2. Implement `Game` (draw with `Canvas2D.rect`, read `InputState`). The virtual screen is 180x320.
3. Add `include(":games:<name>")` to `settings.gradle.kts` and `implementation(project(":games:<name>"))` to `app/build.gradle.kts`.
4. Add `<Name>Game.info` to the list in `ArcadeApp.kt`.

## Build without installing anything (GitHub Actions)

Push the repo to GitHub. The workflow in `.github/workflows/build.yml` runs the tests and builds
`app-debug.apk`; download it from the run's "Artifacts" section, copy it to your phone and install it
(allow "install unknown apps" for your browser/file manager).

## Build locally

Needs the Android SDK (Android Studio installs it) and JDK 17+.

```
gradle wrapper          # once, to generate ./gradlew
./gradlew :core:engine:test :games:snake:test :games:bricks:test :games:dodge:test   # JVM-only, fast
./gradlew :app:installDebug
```

Then open the app, grant "draw over other apps", and turn it on.

## Notes

- The service holds a low-priority foreground notification; Android requires it to listen for screen-on.
- Games only launch while the keyguard is locked, and "Unlock" dismisses the game and asks for your PIN/fingerprint as normal. It does not bypass your secure lock.
- Some manufacturers (Xiaomi, Oppo, Samsung battery settings) kill background services; exempt the app from battery optimisation.
