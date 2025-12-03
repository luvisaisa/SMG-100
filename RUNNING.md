# Running The Completionist

## Recommended Method (Best Terminal Support)

For the best terminal experience with arrow key navigation on macOS:

```bash
./run.sh
```

This script:
1. Builds the application
2. Runs it directly (bypassing Gradle) for optimal JLine3 terminal support
3. Provides full arrow key navigation support in zsh

## Alternative Method (Via Gradle)

You can also run through Gradle, though terminal support may be slightly degraded:

```bash
./gradlew run
```

## Building Distribution

To create a standalone distribution:

```bash
./gradlew installDist
```

The distribution will be available at:
```
app/build/install/app/
```

You can then run it directly:
```bash
./app/build/install/app/bin/app
```

## Troubleshooting Arrow Keys

If arrow keys show escape sequences like `^[[A^[[B` instead of navigating:

1. **Use the run script**: `./run.sh` gives JLine3 direct terminal access
2. **Check TERM variable**: Ensure `echo $TERM` shows `xterm-256color` or similar (not `dumb`)
3. **Build and run directly**: Use `./gradlew installDist` then run the binary directly

The application uses JLine3 with JNA for native terminal support on macOS.
