# Dokumentationsguide

All statisk text i RODA, inklusive `Hjälpsidor`, `Funktionella beskrivningar` och statiska `Html-sidor` finns under `[RODA_HOME]/example-config/theme/`.

För att uppdatera befintligt innehåll kopierar du filen du vill uppdatera från `[RODA_HOME]/example-config/theme/` till `[RODA_HOME]/config/theme/` och redigerar i målmappen.

## Lägg till ny hjälpsida

För att lägga till nya ämnen till hjälpmenyn, kopiera filen `[RODA_HOME]/example-config/theme/README.md` (tillsammans med samtliga överstättningsfiler t.ex. `README_pt_PT.md`) till `[RODA_HOME]/config/theme/documentation`.

Redigera den nya `README.md`-filen för att inkludera en länk till det nya hjälpämnet som ska skapas:

```- (Link text)[The_New_Topic_Page.md]```

Efter att ha lagt till den nya posten i innehållsförteckningen ska en ny [Markdown](https://guides.github.com/features/mastering-markdown/)-fil skapas och placeras under mappen `[RODA_HOME]/config/ tema/dokumentation`. Namnet på den nya filen ska matcha det som anges i innehållsförteckningen (dvs. `The_New_Topic_Page.md` i det här exemplet).

## Redigera HTML-sidor

Vissa HTML-sidor (eller delar av sidor) kan anpassas genom att ändra respektive HTML-sida på `[RODA_HOME]/config/theme/some_specific_page.html`.

Sidmallar finns under `[RODA_HOME]/example-config/theme/`. Dessa ska kopieras från sin ursprungliga plats till `[RODA_HOME]/config/theme/` som förklarades i början av denna artikel.

Exempelvis kan statistiksidan anpassas genom att ändra filen `[RODA_HOME]/config/theme/Statistics.html`.
