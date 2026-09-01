# AGENTS.md — jFileWork

This file provides context for AI coding agents working on this project.

---

## Project Summary

**jFileWork** is a dual-pane file manager for the local file system, inspired by Total Commander.  
It is a local desktop application — no server, no network stack.

| Property        | Value |
|-----------------|-------|
| Language        | Java 26 (Java-only, no Kotlin) |
| UI Framework    | JavaFX 23 (OpenJFX) |
| Build System    | Maven 3.9+ (multi-module) |
| JSON Config     | Jackson 2.18 |
| Logging         | SLF4J 2 + Logback 1.5 |
| Unit Tests      | JUnit 5.11 |
| UI Tests        | TestFX 4.0.18 + Monocle 21.0.2 (headless) |
| Plugin System   | `java.util.ServiceLoader` + isolated `URLClassLoader` |
| Security        | Pragmatic: whitelist + parameter sanitiser (no strong sandbox) |

---

## Maven Module Layout

```
jFileWork/                  ← parent POM (packaging=pom)
├── plugin-api/             ← jfilework-plugin-api   — stable public API for plugins
├── sample-hash-plugin/     ← jfilework-sample-hash-plugin — demo plugin for M5
├── core/                   ← jfilework-core          — file system, commands, config, security
└── ui-javafx/              ← jfilework-ui-javafx     — JavaFX UI (runnable module)
```

**Build order enforced by Maven:** `plugin-api` → `core` → `ui-javafx`

---

## Key Source Locations

| What | Where |
|------|-------|
| Application entry point | `ui-javafx/src/main/java/com/github/uscexp/jfilework/ui/javafx/MainLauncher.java` |
| JavaFX Application class | `ui-javafx/src/main/java/com/github/uscexp/jfilework/ui/javafx/MainApp.java` |
| Main window controller | `ui-javafx/src/main/java/com/github/uscexp/jfilework/ui/javafx/controller/MainWindowController.java` |
| Main window FXML | `ui-javafx/src/main/resources/com/github/uscexp/jfilework/ui/javafx/main-window.fxml` |
| Logging config | `ui-javafx/src/main/resources/logback.xml` |
| UI test base class | `ui-javafx/src/test/java/com/github/uscexp/jfilework/ui/javafx/FxTestBase.java` |
| Headless test config | `ui-javafx/src/test/java/com/github/uscexp/jfilework/ui/javafx/HeadlessTestConfig.java` |
| Development plan | `PLAN.md` |

---

## Root Package Convention

```
com.github.uscexp.jfilework
  ├─ core
  │   ├─ fs           (FileService, FileWatcherService, PathResolver)
  │   ├─ command      (Command, CommandContext, CommandRegistry, CommandExecutor, builtin/*)
  │   ├─ config       (AppConfig, UiConfig, CommandBindingConfig, ConfigService)
  │   ├─ plugin       (PluginManager, PluginClassLoaderFactory)
  │   ├─ security     (CommandPolicy, PathPolicy, ParamSanitizer)
  │   └─ event        (EventBus)
  ├─ ui
  │   └─ javafx
  │       ├─ MainApp, MainLauncher
  │       ├─ controller  (MainWindowController, FilePaneController, ...)
  │       ├─ model       (FileItemViewModel, PaneStateViewModel)
  │       └─ action      (UiActionMapper, ToolbarBuilder, MenuBuilder, KeyBindingService)
  └─ api
      └─ plugin          (CommandPlugin, PluginDescriptor, PluginContext)
```

---

## Build & Run Commands

```bash
# Full build (all modules)
mvn clean install

# Compile only
mvn clean compile

# Run the application (from project root or ui-javafx/)
cd ui-javafx && mvn javafx:run

# Run all tests (unit + UI headless via Monocle)
mvn test

# Run only unit tests
mvn test -Dgroups="!ui"

# Run only UI tests
mvn test -Dgroups="ui"
```

---

## Architecture Decisions

### Command System
Every user action (built-in, OS or plugin) is a `Command`:
```java
public interface Command {
    String id();                                                        // e.g. "file.copy"
    boolean canExecute(CommandContext context);
    CommandResult execute(CommandContext context, Map<String, String> params) throws Exception;
}
```
- Commands are registered in `CommandRegistry` at startup.
- `CommandExecutor` runs them asynchronously on an `ExecutorService`.
- `CommandContext` provides: active/inactive pane, selection snapshot, services, `ProgressSink`, `CancellationToken`.

### UI driven by configuration
Toolbar buttons, menu items and hotkeys are **not hard-coded** — they are loaded from `config/app-config.json`:
```json
{
  "actions": [
    {
      "id":        "copyToOtherPane",
      "label":     "Copy",
      "commandId": "file.copy",
      "params":    { "target": "${inactivePane.path}" },
      "shortcut":  "F5",
      "placement": ["toolbar.main", "menu.file"]
    }
  ]
}
```
- `UiActionMapper` resolves `commandId` → `CommandRegistry` → JavaFX action.
- Missing command → UI element is **disabled** (never an error dialog).
- Parameter templates: `${activePane.path}`, `${inactivePane.path}`, `${selection.paths}`.

### Plugin System
- Plugin JARs are placed in the `plugins/` directory.
- Discovery: `ServiceLoader<CommandPlugin>`.
- Each plugin gets its own `URLClassLoader` (isolation).
- Lifecycle: `initialize(PluginContext)` → commands registered → `destroy()` on shutdown.
- A faulty plugin is caught and logged; the rest of the application continues.

### Security (pragmatic)
- OS commands allowed only when `allowOsCommands: true` in config.
- Executable whitelist (`allowedExecutables`) checked before every `os.exec`.
- `ParamSanitizer` strips shell meta-characters from template output.
- `PathPolicy` ensures resolved paths stay within allowed roots.

---

## UI Test Rules

- All UI tests extend `FxTestBase` (which activates `HeadlessTestConfig`).
- Tests that interact with the UI are tagged `@Tag("ui")`.
- Use `@TempDir` for any file-system fixtures — never touch real user directories.
- Do **not** test business logic in UI tests; that belongs in unit tests.

---

## What NOT to change

| Item | Reason |
|------|--------|
| `plugin-api` interfaces once published | Plugins compiled against them would break |
| `CommandResult` enum values | Used in config and logs |
| FXML `fx:id` values | Referenced by controller `@FXML` fields |
| `HeadlessTestConfig` property keys | Match Monocle's expected system properties |

---

## Current Milestone Status

| Milestone | Status |
|-----------|--------|
| M0 — Setup, Maven modules, JavaFX skeleton, TestFX | ✅ Done |
| M1 — Dual-pane file view, navigation, selection | ✅ Done |
| M2 — Built-in commands (F5–F8, F2) | ✅ Done |
| M3 — Config-driven toolbar / menus / hotkeys | ✅ Done |
| M4 — OS-command adapter + security | ✅ Done |
| M5 — Plugin system + demo plugin | ✅ Done |
| M6 — Polishing, packaging, coverage | 🚧 In Progress (6.1-6.9, 6.10-6.11 done) |

Full milestone details: see [`PLAN.md`](PLAN.md).
