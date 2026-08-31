# jFileWork Plugin Development

## Ziel

Plugins erweitern `jFileWork` um neue Commands. Sie werden ueber `ServiceLoader` gefunden und in `CommandRegistry` registriert.

## Maven-Abhaengigkeit

Ein Plugin braucht nur `jfilework-plugin-api`:

```xml
<dependency>
    <groupId>com.github.uscexp</groupId>
    <artifactId>jfilework-plugin-api</artifactId>
    <version>0.0.1-Alpha-SNAPSHOT</version>
</dependency>
```

## Einstiegspunkte

Implementiere `com.github.uscexp.jfilework.api.plugin.CommandPlugin`:

```java
public class MyPlugin implements CommandPlugin {
    @Override
    public PluginDescriptor descriptor() {
        return new PluginDescriptor("my-plugin", "My Plugin", "1.0.0", "1.0");
    }

    @Override
    public void initialize(PluginContext context) {
        context.logger().info("Plugin initialised");
    }

    @Override
    public List<PluginCommand> commands() {
        return List.of(new MyCommand());
    }
}
```

Command-Implementierung:

```java
public class MyCommand implements PluginCommand {
    @Override
    public String id() {
        return "plugin.my.command";
    }

    @Override
    public PluginCommandResult execute(PluginCommandContext context, Map<String, String> params) {
        return PluginCommandResult.success("Done", 1);
    }
}
```

## ServiceLoader-Registrierung

Lege die Datei an:

`META-INF/services/com.github.uscexp.jfilework.api.plugin.CommandPlugin`

Inhalt:

```text
com.example.myplugin.MyPlugin
```

## Verfuegbare Plugin-API Typen

- `CommandPlugin`
- `PluginDescriptor`
- `PluginContext`
- `PluginCommand`
- `PluginCommandContext`
- `PluginCommandResult`
- `PluginCommandStatus`
- `PluginProgressSink`
- `PluginCancellationToken`

## Konfiguration in der App

Das Plugin muss im Plugin-Ordner liegen und optional in `config/app-config.json` aktiviert sein:

```json
{
  "plugins": {
    "directory": "plugins",
    "enabled": ["my-plugin"]
  },
  "actions": [
    {
      "id": "runMyPluginCommand",
      "label": "Run My Plugin Command",
      "commandId": "plugin.my.command",
      "params": {},
      "placement": ["menu.tools", "toolbar.main"]
    }
  ]
}
```

## Kompatibilitaet

- `PluginDescriptor.pluginApiVersion` muss aktuell `1.0` sein.
- Inkompatible oder fehlerhafte Plugins werden protokolliert und uebersprungen.
- Jede Plugin-Location bekommt einen eigenen `URLClassLoader`.

## Beispiel

Siehe `sample-hash-plugin/` fuer ein vollstaendiges Beispielplugin mit `plugin.hash.sha256`.

