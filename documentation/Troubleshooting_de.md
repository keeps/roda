# Fehlerbehebung

Die Fehlersuche ist eine Form der Problemlösung, die häufig bei der Reparatur defekter Produkte angewandt wird. Es handelt sich um eine logische, systematische Suche nach der Ursache eines Problems, um es zu lösen und das Produkt oder den Prozess wieder betriebsbereit zu machen.

In diesem Abschnitt finden Sie häufige Probleme, die dieses Produkt betreffen, und mögliche Lösungen für diese Probleme.

## Fehler: Zu viele offene Dateien

Manchmal werden in den Protokollen Fehler wie diese angezeigt:

```
RODA_HOME/logs/roda-wui.log:pt.gov.dgarq.roda.core.common.RODAClientException: Error connecting to Login service - Too many open files
RODA_HOME/logs/roda-wui.log:Caused by: java.net.SocketException: Too many open files
```

Dies kann passieren, wenn der Server viele Dateien geöffnet hat. Um zu sehen, wie viele Dateien der Server geöffnet hat, ermitteln Sie die PID des Prozesses und führen Sie dann lsof | grep <PID> | wc aus. Auf vielen Computern ist die standardmäßige Höchstzahl der Dateien niedrig, die ein Prozess öffnen kann (z. B. 1024).

Um dieses Limit zu ändern, editieren Sie `/etc/security/limits.conf` und fügen Sie folgendes hinzu:

```
* soft nofile 2048
* hard nofile 2048
```

Dadurch kann jeder ausgeführte Prozess 2048 Dateien geöffnet haben. Sie müssen den Computer neu starten, damit diese Änderungen wirksam werden. Sie können auch den Befehl `ulimit` zur Laufzeit verwenden, diese Änderungen bleiben aber beim nächsten Start nicht erhalten.
