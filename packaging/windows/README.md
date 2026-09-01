# Windows Packaging

This directory contains the helper script for building a custom runtime image with `jlink` and a Windows installer with `jpackage`.

## Prerequisites

- JDK 26 with `jlink` and `jpackage` available on `PATH` or via `JAVA_HOME`
- Maven 3.9+
- Windows 10/11

## Full build

From the repository root:

```powershell
.\packaging\windows\build-installer.ps1
```

The script will:

1. Build the Maven modules.
2. Copy runtime dependencies for `ui-javafx`.
3. Stage the application JAR plus dependencies.
4. Build a custom runtime image with `jlink`.
5. Package a Windows installer with `jpackage`.

The installer is written to:

```text
ui-javafx\target\packaging\installer
```

## Prepare-only mode

If you want to verify the staging logic without invoking `jlink`/`jpackage`, use:

```powershell
.\packaging\windows\build-installer.ps1 -SkipMaven -PrepareOnly
```

This is useful for quick checks when the build artifacts already exist.

## Maven profile

The `ui-javafx` module provides a `windows-installer` profile that copies runtime dependencies into:

```text
ui-javafx\target\packaging\runtime-libs
```

Use it together with the script when you want a reproducible packaging layout.

