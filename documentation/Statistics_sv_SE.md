# Statistik

Den här sidan ger information om hur du konfigurerar ny statistik baserat på den information som finns tillgänglig i e-arkivet. För att förstå denna information måste man vara en avancerad användare med kunskaper inom HTML, CSS och Javascript.

Statistik fungerar genom att frågor skickas till RODA med hjälp av dess API (läs API-dokumentationen för mer information), samla in resultaten och presentera dem grafiskt. Hela processen görs på klientsidan av Javascript.

Följande avsnitt tillhandahåller kodsnuttar som man kan använda för att visa statistik om e-arkivets statistik. Man behöver bara inkludera kodsnuttarna på en ny HTML-sida, så kommer medföljande Javascript-motorn att hantera all kommunikation, arbetsbelastning och presentation. Ny grafer och statistik kan skapas genom att ändra frågeparametrarna för "data" som ingår i utdragen (t.ex. datakälla-filter).

## AIP Index

**Totala antalet beskrivande arkivobjekt**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Totala antalet arkiv**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
  data-source-filters="level=fonds"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Distribution av beskrivandenivåer**

![Distribution of description levels](images/distribution_of_description_levels_pie.png "Distribution av beskrivandenivåer")

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

## Representationsindex

**Total No. of Representations** (totala antalet representationer)

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedRepresentation"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Distribution av representationstyper**

![Distribution of representation types](images/distribution_of_representation_types_bar.png "Distribution av representationstyper")

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

## Filindex

**Totalt antal filer**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedFile"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Distribution av mimetyper**

![Distribution of mimetypes](images/distribution_of_mimetypes_pie.png "Distribution av mimetyper")

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

**Distribution av PRONOM ID:s**

![Distribution of PRONOM IDs](images/distribution_of_pronom_ids_doughnut.png "Distribution av PRONOM ID:s")

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

## Jobbindex

**Totalt antal inleveransjobb**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.jobs.Job"
  data-source-filters="pluginType=INGEST, state=COMPLETED"
  data-source-facets="pluginType"
  data-view="text"
  data-view-field="totalCount"></span>
```

## Loggindex

**Totala antal inloggningar**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=SUCCESS"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Totala antalet misslyckade inloggningsförsök**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=FAILURE"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Lyckade vs misslyckade inloggningar**

![Successful vs failed logins](images/successful_vs_failed_logins_pie.png "Lyckade vs misslyckade inloggningar")

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

## Andra diagram

### Linjediagram

**Beskrivningsnivådistribution**

![Description level distribution](images/description_level_distribution_line.png "Beskrivningsnivådistribution")

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

### Radardiagram

**Pronomformatdistribution**

![Pronom format distribution](images/pronom_format_distribution_radar.png "Pronomformatdistribution")

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

### Polärdiagram

**Pronomformatdistribution**

![Pronom format distribution](images/pronom_format_distribution_polararea.png "Pronomformatdistribution")

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

### Anpassad funktion för att hantera facet-data

**Pronomformatdistribution**

![Pronom format distribution](images/pronom_format_distribution_function.png "Pronomformatdistribution")

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

### Anpassade funktioner för att skapa diagram

**Bubbeldiagram**

![Bubble chart](images/bubble_chart.png "Bubbeldiagram")

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
