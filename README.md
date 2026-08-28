# jFileWork

A **dual-pane file manager** for the local file system — inspired by Total Commander.  
Built with **Java 26** and **JavaFX 23**, featuring a fully configuration-driven UI and a JAR plugin system.

---

## Project Structure

```
jFileWork/               ← Parent POM (multi-module)
├── plugin-api/          ← Stable public API for external plugins
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

The application is configured via `config/app-config.json` (created on first run with defaults).

Example — add a custom toolbar button that calls 7-Zip:

```json
{
  "actions": [
    {
      "id": "zipWith7z",
      "label": "Zip (7z)",
      "commandId": "os.exec",
      "params": {
        "exe": "C:/Program Files/7-Zip/7z.exe",
        "args": "a ${activePane.path}/archive.7z ${selection.paths}"
      },
      "placement": ["menu.tools"]
    }
  ]
}
```

---

## Plugins

Drop plugin JARs into the `plugins/` folder. They are discovered automatically via `ServiceLoader`.  
See [`PLUGIN_DEV.md`](PLUGIN_DEV.md) *(available in M5)* for the plugin development guide.

---

## Development Milestones

| Milestone | Focus |
|-----------|-------|
| **M0** ✅ | Project setup, Maven modules, JavaFX skeleton, TestFX |
| **M1** | Dual-pane file view, navigation, selection |
| **M2** | Built-in commands (F5 Copy, F6 Move, F7 Mkdir, F8 Delete, F2 Rename) |
| **M3** | Config-driven toolbar, menus and hotkeys |
| **M4** | OS-command adapter + security rules |
| **M5** | Plugin system (ServiceLoader, demo plugin) |
| **M6** | Polishing, tabs, drag & drop, native packaging |

