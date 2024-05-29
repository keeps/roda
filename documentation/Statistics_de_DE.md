# Statistiken

Diese Seite enthält Informationen darüber, wie neue Statistiken auf der Grundlage der im Repository verfügbaren Informationen konfiguriert werden können. Um diese Informationen zu verstehen, muss man ein fortgeschrittener Benutzer mit Kenntnissen in HTML, CSS und Javascript sein.

Die Statistiken funktionieren, indem Abfragen an RODA über die API gesendet werden (weitere Informationen finden Sie in der API-Dokumentation), die Ergebnisse gesammelt und als Grafiken dargestellt werden. Der gesamte Prozess wird auf der Client-Seite durch Javascript ausgeführt.

Die folgenden Abschnitte enthalten Codeschnipsel, die zur Anzeige von Statistiken über den Status des Repositorys verwendet werden können. Sie brauchen die Code-Ausschnitte in eine neue HTML-Seite einzubinden und die mitgelieferte Javascript-Engine kümmert sich um die gesamte Kommunikation, Arbeitslast und Darstellung. Neue Grafiken und Statistiken können durch Änderung der in den Ausschnitten enthaltenen Datenparameter (z.B. Datenquellenfilter) erstellt werden.

## AIP Index

**Gesamtanzahl der beschreibenden Datensätze**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Gesamtzahl der Bestände**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
  data-source-filters="level=fonds"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Verteilung der Beschreibungsebenen**

![Verteilung der Beschreibungsebenen](images/distribution_of_description_levels_pie.png "Verteilung der Beschreibungsebenen")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
        data-source-facets="level"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="pie"></canvas>
```

## Repräsentationen-Index

**Gesamtanzahl der Repräsentationen**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedRepresentation"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Verteilung der Vertretungsarten**

![Verteilung der Vertretungsarten](images/distribution_of_representation_types_bar.png "Verteilung der Vertretungsarten")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedRepresentation"
        data-source-facets="type"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="bar"></canvas>
```

## Datei-Index

**Gesamtzahl der Dateien**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedFile"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Verteilung der MIME-Typen**

![Verteilung der MIME-Typen](images/distribution_of_mimetypes_pie.png "Verteilung der MIME-Typen")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatMimetype"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="pie"></canvas>
```

**Verteilung der PRONOM IDs**

![Verteilung der PRONOM IDs](images/distribution_of_pronom_ids_doughnut.png "Verteilung der PRONOM IDs")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="doughnut"></canvas>
```

## Stellenindex

**Gesamtzahl der Ingest-Aufträge**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.jobs.Job"
  data-source-filters="pluginType=INGEST, state=COMPLETED"
  data-source-facets="pluginType"
  data-view="text"
  data-view-field="totalCount"></span>
```

## Protokollindex

**Gesamtzahl der Anmeldungen**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=SUCCESS"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Gesamtzahl der fehlgeschlagenen Anmeldungen**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=FAILURE"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Erfolgreiche vs. fehlgeschlagene Anmeldungen**

![Erfolgreiche vs. fehlgeschlagene Anmeldungen](images/successful_vs_failed_logins_pie.png "Erfolgreiche vs. fehlgeschlagene Anmeldungen")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.log.LogEntry"
        data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login"
        data-source-facets="state"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="pie"></canvas>
```

## Andere Diagramme

### Liniendiagramme

**Beschreibungsebenenverteilung**

![Beschreibungsebenenverteilung](images/description_level_distribution_line.png "Beschreibungsebenenverteilung")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
        data-source-facets="level"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="line"></canvas>
```

### Radarkarten

**Pronom-Format Verteilung**

![Pronom-Format Verteilung](images/pronom_format_distribution_radar.png "Pronom-Format Verteilung")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="radar"></canvas>
```

### Polargebietskarten

**Pronom-Format Verteilung**

![Pronom-Format Verteilung](images/pronom_format_distribution_radar.png "Pronom-Format Verteilung")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="polarArea"></canvas>
```

### Benutzerdefinierte Funktion zur Verarbeitung von Facettendaten

**Pronom-Format Verteilung**

![Pronom-Format Verteilung](images/pronom_format_distribution_radar.png "Pronom-Format Verteilung")

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-limit="10"
        data-view-field="facetResults"
        data-view-type="function"
        data-view-type-function="facetCustomDataHandlerChartOptions"></canvas>
<script type="text/javascript">
function facetCustomDataHandlerChartOptions(data, element){
var options = {};
var facet = data.facetResults.length > 0 ? data.facetResults[0] : null;
if (facet) {
options = {
type: "pie",
data: {
labels: facet.values.map(function (value) {
return value.label;
}),
datasets: [{
label: facet.field,
data: facet.values.map(function (value) {
return value.count;
}),
backgroundColor: facet.values.map(function () {
return rgbaRandomOpaqueColorAsString();
})
}]
},
options: {
cutoutPercentage: 90
}
};
}
return options;
}
</script>
```

### Benutzerdefinierte Funktion zur Erstellung eines Diagramms

**Blasendiagramm**

![Blasendiagramm](images/bubble_chart.png "Blasendiagramm")

```html
<canvas class="statistic"
        data-source="function"
        data-function="customDataBubbleChart"></canvas>
<script type="text/javascript">
function customDataBubbleChart(element) {
new Chart(element, {
type: 'bubble',
data: {
datasets: [{
label: "Test dataset",
data: [-3,-2,-1,0,1,2,3].map(function (value) {
return {
x: value,
y: value * value,
r: Math.abs(value) * 10
};
}),
backgroundColor: rgbaRandomOpaqueColorAsString(),
hoverBackgroundColor: rgbaRandomOpaqueColorAsString()
}]
}
});
}
</script>
```
