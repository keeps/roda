# Dokumentationsleitfaden

Der gesamte Text in RODA, einschließlich der `Hilfe-Seiten`, der `Funktionsbeschreibung` und der `HTML-Seiten`, befindet sich im Verzeichnis `[RODA_HOME]/example-config/theme/`.

Um den vorhandenen Inhalt zu aktualisieren, sollten Sie die zu aktualisierende Datei von `[RODA_HOME]/example-config/theme/` nach `[RODA_HOME]/config/theme/` kopieren und im Zielordner bearbeiten.

## neue Hilfeseite anlegen

Um neue Themen zum Hilfemenü hinzuzufügen, müssen Sie die Datei `[RODA_HOME]/example-config/theme/README.md` (und alle ihre Übersetzungsdateien, z.B. `README_pt_PT.md`) nach `[RODA_HOME]/config/theme/documentation` kopieren.

Bearbeiten Sie die neue Datei `README.md`, um einen Link auf das neu zu erstellende Thema einzufügen:

```
- (Link text)[The_New_Topic_Page.md]
```

Nachdem ein neues Eintrags zum Inhaltsverzeichnis hinzugefügt wird, wird eine neue [Markdown](https://guides.github.com/features/mastering-markdown/)-Datei erstellt und im Ordner `[RODA_HOME]/config/theme/documentation` abgelegt. Der Name der neuen Datei sollte mit dem des Inhaltsverzeichnisses übereinstimmen (d.h. in diesem Beispiel `Die_Neue_Themen_Seite.md`).

## HTML-Seiten bearbeiten

Einige HTML-Seiten (oder Teile der Seiten) können durch Ändern der entsprechenden HTML-Seite unter `[RODA_HOME]/config/theme/some_specific_page.html` angepasst werden.

Seitenvorlagen existieren unter `[RODA_HOME]/example-config/theme/`. Diese sollten von ihrem ursprünglichen Speicherort nach `[RODA_HOME]/config/theme/` kopiert werden, wie am Anfang dieses Artikels beschrieben.

Zum Beispiel kann die Statistikseite durch Ändern der Datei `[RODA_HOME]/config/theme/Statistics.html` angepasst werden.
