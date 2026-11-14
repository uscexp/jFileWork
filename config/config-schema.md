# app-config.json Schema (M5)

## Root

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `ui` | Objekt | nein | UI-Basisoptionen |
| `security` | Objekt | nein | Pragmatische Sicherheitsregeln fuer `os.exec` |
| `plugins` | Objekt | nein | Plugin-Discovery und Aktivierung |
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

## `plugins`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `directory` | String | nein | Plugin-Ordner; enthaelt JARs oder Entwicklungs-Unterordner mit `META-INF/services` |
| `enabled` | Array[String] | nein | Liste aktivierter Plugin-IDs; leer = alles laden |

## `actions[]`

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | String | ja | Eindeutige Action-ID |
| `label` | String | ja | Sichtbarer Name in UI |
| `commandId` | String | ja | Z. B. `file.copy`, `file.move` |
| `params` | Objekt | nein | String-Parameter fuer Command |
| `shortcut` | String | nein | JavaFX KeyCombination, z. B. `F5`, `CTRL+R` |
| `placement` | Array[String] | nein | `toolbar.main`, `menu.file`, `menu.tools` |
| `icon` | String | nein | Pfad zum Icon-Bild (z. B. `/image/copy.png`); bei `os.exec` ohne `icon` wird automatisch das Icon der EXE-Datei verwendet |

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

## Icon-Reduzierung bei Toolbar-Buttons

Toolbar-Buttons zeigen standardmssig ein Icon mit dem `label` als Tooltip. Die Auflsung folgt dieser Prioritt:

1. `icon` aus der Action-Konfiguration (z. B. `/image/copy.png`) — wenn gesetzt, hat das Vorrang
2. Bei `commandId: "os.exec"` ohne konfiguriertes `icon`: das von der `exe`-Datei extrahierte System-Icon (unter Windows via `FileSystemView`)
3. Fallback: nur der `label` als Button-Text, kein Icon

Beispiel `os.exec` Action ohne eigenes `icon` (verwendet automatisch das EXE-Icon):

```json
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
```


