# app-config.json Schema (M3)

## Root

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `ui` | Objekt | nein | UI-Basisoptionen |
| `security` | Objekt | nein | Pragmatische Sicherheitsregeln fuer `os.exec` |
| `actions` | Array | nein | Konfigurierbare Actions fuer Menues, Toolbar und Hotkeys |

## `ui`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `theme` | String | nein | `system`, `light`, `dark` (aktuell nur vorbereitet) |
| `showHiddenFiles` | Boolean | nein | versteckte Dateien ein/aus (wird in M6 verdrahtet) |
| `leftStartPath` | String | nein | Startpfad linke Pane |
| `rightStartPath` | String | nein | Startpfad rechte Pane |

## `security`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `allowOsCommands` | Boolean | nein | Aktiviert/deaktiviert `os.exec` global |
| `allowedExecutables` | Array[String] | nein | Absolute Pfade auf explizit erlaubte Programme |
| `allowedRoots` | Array[String] | nein | Optionale erlaubte Wurzelpfade fuer aktive/inaktive/selektierte Dateien |

## `actions[]`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | String | ja | Eindeutige Action-ID |
| `label` | String | ja | Sichtbarer Name in UI |
| `commandId` | String | ja | Z. B. `file.copy`, `file.move` |
| `params` | Objekt | nein | String-Parameter fuer Command |
| `shortcut` | String | nein | JavaFX KeyCombination, z. B. `F5`, `CTRL+R` |
| `placement` | Array[String] | nein | `toolbar.main`, `menu.file`, `menu.tools` |

## Parameter-Templates

- `${activePane.path}`
- `${inactivePane.path}`
- `${selection.paths}` (space-separiert)
- `${prompt:Frage}` (zeigt Eingabedialog zur Laufzeit)

## `os.exec` Parameter

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `exe` | String | ja | Absoluter Pfad des auszufuehrenden Programms |
| `args` | String | nein | Argument-Zeichenkette; wird ohne Shell tokenisiert |

