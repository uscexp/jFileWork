# jFileWork

A **dual-pane file manager** for the local file system — inspired by Total Commander.  
Built with **Java 26** and **JavaFX 23**, featuring a fully configuration-driven UI and a JAR plugin system.

---

## Project Structure

```
jFileWork/               ← Parent POM (multi-module)
├── plugin-api/          ← Stable public API for external plugins
├── sample-hash-plugin/  ← Demo plugin contributing SHA-256 commands
├── core/                ← File system logic, command engine, config, security
└── ui-javafx/           ← JavaFX dual-pane UI (this is the runnable module)
```

See [`PLAN.md`](PLAN.md) for the full milestone-based development roadmap.

---

## Prerequisites

| Tool        | Version  |
|-------------|----------|
| JDK         | 26+      |
| Maven       | 3.9+     |

---

## Build

```bash
# From the project root
mvn clean install
```

---

## Run

```bash
cd ui-javafx
mvn javafx:run
```

---

## Test

```bash
# All tests (unit + UI headless via Monocle)
mvn test

# Only unit tests (skip UI tag)
mvn test -Dgroups="!ui"

# Only UI tests
mvn test -Dgroups="ui"
```

---

## Configuration

The application is configured via `config/app-config.json`.

### Security for external commands

`os.exec` is disabled by default. To enable it, add a `security` section with an explicit allow-list:

```json
{
  "security": {
    "allowOsCommands": true,
    "allowedExecutables": [
      "C:/Program Files/7-Zip/7z.exe",
      "C:/Program Files/Notepad++/notepad++.exe",
      "C:/Program Files/Git/bin/bash.exe"
    ],
    "allowedRoots": [
      "C:/Users/andreas.eisenhauer"
    ]
  }
}
```

### Example actions

Toolbar button that calls 7-Zip:

```json
{
  "security": {
    "allowOsCommands": true,
    "allowedExecutables": ["C:/Program Files/7-Zip/7z.exe"],
    "allowedRoots": ["C:/Users/andreas.eisenhauer"]
  },
  "actions": [
    {
      "id": "zipWith7z",
      "label": "Zip (7z)",
      "commandId": "os.exec",
      "params": {
        "exe": "C:/Program Files/7-Zip/7z.exe",
        "args": "a archive.7z ${selection.paths}"
      },
      "placement": ["menu.tools"]
    }
  ]
}
```

Open the selected file in Notepad++:

```json
{
  "security": {
    "allowOsCommands": true,
    "allowedExecutables": ["C:/Program Files/Notepad++/notepad++.exe"]
  },
  "actions": [
    {
      "id": "openInNotepadPlusPlus",
      "label": "Open in Notepad++",
      "commandId": "os.exec",
      "params": {
        "exe": "C:/Program Files/Notepad++/notepad++.exe",
        "args": "${selection.paths}"
      },
      "placement": ["menu.tools", "toolbar.main"]
    }
  ]
}
```

Run a Git Bash command inside the active pane:

```json
{
  "security": {
    "allowOsCommands": true,
    "allowedExecutables": ["C:/Program Files/Git/bin/bash.exe"]
  },
  "actions": [
    {
      "id": "gitStatus",
      "label": "Git Status",
      "commandId": "os.exec",
      "params": {
        "exe": "C:/Program Files/Git/bin/bash.exe",
        "args": "-lc \"git status\""
      },
      "placement": ["menu.tools"]
    }
  ]
}
```

---

## Plugins

Plugins are discovered via `ServiceLoader<CommandPlugin>`.

Minimal plugin configuration:

```json
{
  "plugins": {
    "directory": "plugins",
    "enabled": ["sample-hash-plugin"]
  },
  "actions": [
    {
      "id": "hashSelection",
      "label": "Hash Selection",
      "commandId": "plugin.hash.sha256",
      "params": {},
      "placement": ["toolbar.main", "menu.tools"]
    }
  ]
}
```

For development and tests, the plugin loader also accepts subdirectories that contain compiled classes plus `META-INF/services`.

See [`PLUGIN_DEV.md`](PLUGIN_DEV.md) for the plugin development guide.

---

## UX Notes (M6)

- Each pane shows a lightweight type icon (`[D]`, `[T]`, `[I]`, `[A]`, `[F]`) in the first table column.
- Each side now uses tabs, so multiple directories can stay open in parallel.
- Use `View -> New Left Tab`, `View -> New Right Tab`, and `View -> Close Active Tab`.
- The inline filter field above each pane hides non-matching entries by filename.
- Drag & drop between panes triggers copy/move semantics via the command engine.
- `F3` opens quick preview for text and image files.
- Binary files and very large text files show a safe hint instead of loading full content.

---

## Development Milestones

| Milestone | Focus |
|-----------|-------|
| **M0** ✅ | Project setup, Maven modules, JavaFX skeleton, TestFX |
| **M1** ✅ | Dual-pane file view, navigation, selection |
| **M2** ✅ | Built-in commands (F5 Copy, F6 Move, F7 Mkdir, F8 Delete, F2 Rename) |
| **M3** ✅ | Config-driven toolbar, menus, hotkeys, preferences and session persistence |
| **M4** ✅ | OS-command adapter + pragmatic security rules |
| **M5** ✅ | Plugin system (ServiceLoader, isolated loader, demo plugin) |
| **M6** 🚧 | Polishing, tabs, drag & drop, native packaging (6.1-6.5 + 6.7 + 6.10 + 6.11 done) |
