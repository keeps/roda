# Statistics

This page provides information on how to configure new statistics based on the information available on the repository. It is important to acknowledge, that in order to understand this information one must be an advanced user with knowledge of HTML, CSS and Javascript.

Statistics work by sending queries to RODA by means of its API (inspect the API documentation for more information), collecting the results and presenting them as graphics. The entire process is done on the client side by Javascript.

The following sections provide code snippets that one can use to display statistics about the status of the repository. One just needs to include the code snippets in a new HTML page and the included Javascript engine will handle all the communication, workload and presentation. New graphics and statistics can be created by changing the query "data" parameters included in the snippets (e.g. data-source-filters).

## AIP Index

**Total No. of descriptive records**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Total No. of fonds**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
  data-source-filters="level=fonds"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Distribution of description levels**

![Distribution of description levels](images/distribution_of_description_levels_pie.png "Distribution of description levels")

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

## Representations Index

**Total No. of Representations**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedRepresentation"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Distribution of representation types**

![Distribution of representation types](images/distribution_of_representation_types_bar.png "Distribution of representation types")

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

## Files index

**Total No. of files**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.ip.IndexedFile"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Distribution of mimetypes**

![Distribution of mimetypes](images/distribution_of_mimetypes_pie.png "Distribution of mimetypes")

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

**Distribution of PRONOM IDs**

![Distribution of PRONOM IDs](images/distribution_of_pronom_ids_doughnut.png "Distribution of PRONOM IDs")

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

## Job index

**Total No. of Ingest jobs**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.jobs.Job"
  data-source-filters="pluginType=INGEST, state=COMPLETED"
  data-source-facets="pluginType"
  data-view="text"
  data-view-field="totalCount"></span>
```

## Log index

**Total No. of logins**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=SUCCESS"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Total No. of failed logins**

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=FAILURE"
  data-view="text"
  data-view-field="totalCount"></span>
```

**Successful vs failed logins**

![Successful vs failed logins](images/successful_vs_failed_logins_pie.png "Successful vs failed logins")

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

## Other charts

### Line charts

**Description level distribution**

![Description level distribution](images/description_level_distribution_line.png "Description level distribution")

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

### Radar charts

**Pronom format distribution**

![Pronom format distribution](images/pronom_format_distribution_radar.png "Pronom format distribution")

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

### Polar area charts

**Pronom format distribution**

![Pronom format distribution](images/pronom_format_distribution_polararea.png "Pronom format distribution")

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

### Custom function to handle facet data

**Pronom format distribution**

![Pronom format distribution](images/pronom_format_distribution_function.png "Pronom format distribution")

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

### Custom function to create chart

**Bubble chart**

![Bubble chart](images/bubble_chart.png "Bubble chart")

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
