# Formatos de metainformação

RODA suporta qualquer formato de metainformação descritiva (i.e. metainformação descritiva tal como mencionado no OAIS) desde que seja representada por um ficheiro XML. Se possuí um formato de metainformação descritiva que não é baseado em XML (e.g. CSV, JSON. MARC21, etc) terá que converter para XML antes de ser incorporado no RODA. Existem diferentes ferramentas que permitem a conversão da maioria dos formatos para XML.

Assim que a metainformação esteja disponível em XML esta está pronta para ser empacotada no SIP (Submission Information Package) e ingerida no repositório. Alternativamente, também é possível criar o ficheiro de metainformação diretamente no repositório e usando a funcionalidade fornecida pelo Catálogo. Quando o formato de metainformação é desconhecido para o RODA, o repositório tentará suportar, o melhor possível, sem precisar de nenhuma reconfiguração do sistema, contudo as seguintes limitação serão aplicadas:

#### Validação

Se nenhum esquema XML for fornecido para o formato de metainformação, o repositório irá apenas verificar se o ficheiro de metainformação está bem formado, contudo o repositório não irá verificar se é um formato válido pois não conhece a gramática correta para o fazer.

#### Indexação

O repositório irá indexar todos os elementos de texto e o atributos encontrados no ficheiro de metainformação, contudo uma vez que o repositório não sebe o mapeamento correto entre os elementos XML e a estrutura interna do modelo de dados,  apenas a pesquisa básica será possível.

#### Visualização

Quando nenhum mapeamento de visualização estiver configured, um visualizador genérico será usado para mostrar a metainformação. Todos os elementos de texto e os atributos serão mostrado sem nenhum ordem em particular e XPath será usado para obter as etiquetas dos elementos.

#### Edição

RODA precisa de ficheiros de configuração para perceber como os ficheiros de metainformação devem ser mostrado no âmbito da edição. Se nenhum ficheiro de configuração for fornecido, o repositório irá mostrar uma área de texto onde o utilizador tem a possibilidade de editar o XML diretamente.

De forma a suportar novos formatos de metainformação, o repositório deverá ser configurado de acordo. As seguintes secções descrevem em detalhe o conjunto de ações necessárias para habilitar o RODA com esquemas de metainformação novos.

## Ficheiros de melhorias de metainformação

Para melhorar a experiência com a metainformação no RODA existem 4 ficheiros que precisam de ser adicionados à pasta de configurações do repositório. As seguintes secções descrevem e fornecem exemplos desses ficheiros.

### Validação

RODA usa um [esquema XML](http://www.w3.org/standards/xml/schema) para validar a estrutura e os tipos de dados dos ficheiros de metainformação. A validação do esquema é usada durante o processo de ingestão para verificar se a metainformação incluída no SIP é válida de acordo com as restrições estabelecidas assim como quando a metainformação é editada através do catálogo.

Poderá usar um esquema padrão para o propósito da validação ou criar um específico com o grau de controlo que desejar, tais como verificar a instalação do repositório, campos obrigatórios, vocabulários controlados, etc.

Os ficheiros dos esquemas de validação devem ser colocado na pasta de configuração em `[RODA_HOME]/config/schemas/`.

A maioria dos formatos de metainformação são publicados com documentação e o esquema XML. O seguinte exemplo representa um esquema para o Simple Dublin Core formato de metainformação:

Distribuição de mimetypes

### ```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatMimetype"
        data-view="chart"
        data-view-field="facetResults"
        data-view-type="pie"></canvas>
```

Distribuição de PRONOM IDs

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-field="facetResults"
        data-view-type="doughnut"></canvas>
```

Índice de processos

No. total de processos de ingestão

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.jobs.Job"
  data-source-filters="pluginType=INGEST, state=COMPLETED"
  data-source-facets="pluginType"
  data-view="text"
  data-view-field="totalCount"></span>
```

Índice de registros

### No. total de logins

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=SUCCESS"
  data-view="text"
  data-view-field="totalCount"></span>
```

No. total de logins falhados

```html
<span class="statistic"
  data-source="index"
  data-source-class="org.roda.core.data.v2.log.LogEntry"
  data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login, state=FAILURE"
  data-view="text"
  data-view-field="totalCount"></span>
```

Login com sucesso vs falhados

### ```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.log.LogEntry"
        data-source-filters="actionComponent=org.roda.wui.api.controllers.UserLogin, actionMethod=login"
        data-source-facets="state"
        data-view="chart"
        data-view-field="facetResults"
        data-view-type="pie"></canvas>
```

Outros gráficos

Gráficos de linhas

Distribuição de nivel descritivos

```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedAIP"
        data-source-facets="level"
        data-view="chart"
        data-view-field="facetResults"
        data-view-type="line"></canvas>
```

Gráficos de radar

*   Distribuição de formatos PRONOM
*   ```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-field="facetResults"
        data-view-type="radar"></canvas>
```
*   Gráficos polares
*   Distribuição de formatos PRONOM
*   ```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
        data-view-field="facetResults"
        data-view-type="polarArea"></canvas>
```
*   Função personalizada para manipular dados de facetas
*   Distribuição de formatos PRONOM
*   ```html
<canvas class="statistic"
        data-source="index"
        data-source-class="org.roda.core.data.v2.ip.IndexedFile"
        data-source-facets="formatPronom"
        data-view="chart"
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
*   Função personalizada para criar gráfico

*   Gráfico de bolhas
*   ```html
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
*   **title** - gera o título
*   **level** - adiciona o nível de descrição atual
*   **parentid** - adiciona os identificadores do pai, caso exista
*   **language** - adiciona o idioma do sistema, baseado no local. Exemplo: "Português" ou "English"

*   **type** - o tipo do campo. Os valores possíveis são:

*   **text** - o campo de texto
*   **text-area** - Área de texto. Maior que o campo de texto.
*   **date** - campo de texto com um selector de datas (date picker)
*   **list** - lista com valores possiveis (combo box)

*   **options** - lista com valores possíveis para o campo. Esta lista é um JSonArray, Exemplo: `options="['final','verificado','rascunho']"`
*   **optionsLables** - Mapa com etiquetas para cada opção. A chave deverá corresponder a uma das opções especificada na lista "options". A entrada é outro mapa, mapeando o idioma (chave) com a etiqueta (valor). Exemplo: `optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'verificado': {'en':'Revised', 'pt_PT':'Verificado'},'rascunho': {'en':'Draft', 'pt_PT':'Rascunho'}}"`
*   **optionsLabelI18nKeyPrefix** - I18n prefixo. Todas as chaves começados pelo prefixo são usadas para construir a lista. Exemplo:

    `optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.level"`

    Ficheiro de propriedades:

        crosswalks.dissemination.html.ead.level.fonds=Fundo
        crosswalks.dissemination.html.ead.level.class=Classe
        crosswalks.dissemination.html.ead.level.collection=Coleção
        crosswalks.dissemination.html.ead.level.recordgrp=Grupo de registos
        crosswalks.dissemination.html.ead.level.subgrp=Subgrupo
        crosswalks.dissemination.html.ead.level.subfonds=Subfundo
        crosswalks.dissemination.html.ead.level.series=Série
        crosswalks.dissemination.html.ead.level.subseries=Subsérie
        crosswalks.dissemination.html.ead.level.file=Documento composto
        crosswalks.dissemination.html.ead.level.item=Documento

    Resultado:

        <select>
        <option value="fonds">Fundo</option>
        <option value="class">Classe</option>
        <option value="collection">Coleção</option>
        <option value="recordgrp">Grupo de registos</option>
        (...)
        </select>

#### Exemplo completo do campo "list"

    {{~field
      name="statusDescription"
      order="470"
      type="list"
      value="final"
      options="['final','revised','draft']"
      optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'revised': {'en':'Revised', 'pt_PT':'Verificado'},'draft': {'en':'Draft', 'pt_PT':'Rascunho'}}"
      optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.statusDescription"
      label="{'en': 'Status description', 'pt_PT': 'Estado da descrição'}"
      xpath="/*:ead/*:archdesc/*:odd[@type='statusDescription']/*:p/string()"
    ~}}

O seguinte é um exemplo de como as tags podem ser usadas:

    {{~file name="title" order="1" type="text" label="Template title" mandatory="true" auto-generate="title"~}}

## Ativar o novo formato

Depois de adicionar todos os arquivos descritos na seção anterior, é preciso habilitá-los no repositório. Para realizar isso, as atividades a seguir precisam ser feitas.

### Ativar o novo formato de metadados

Depois de adicionar os arquivos descritos anteriormente à sua pasta de configuração, você deve ativar o novo formato no arquivo de configuração principal do RODA.

Edite o ficheiro `[RODA_HOME]/config/roda-wui.properties` e adicione uma nova entrada tal como demonstrado no exemplo seguinte com o nome da metainformação descritiva adicionada. Fazendo com que o RODA esteja consciente do novo formato de metainformação.

```
ui.browser.metadata.descriptive.types = dc
ui.browser.metadata.descriptive.types = ead_3
ui.browser.metadata.descriptive.types = ead_2002
```

### Internacionalização de texto

De forma a que o novo esquema de metainformação seja integrado de forma perfeita, deverá fornecer a informação sobre a internacionalização (i18n) para que o RODA saiba como mostrar a informação necessária ao utilizador na melhor forma possível.

Edite o ficheiro `[RODA_HOME]/config/i18n/ServerMessages.properties` e adicione as seguintes entradas se necessárias, tendo em atenção que a última parte da chave corresponde ao código fornecido no ficheiro `[RODA_HOME]/config/roda-wui.properties` descrito na secção anterior:

```
ui.browse.metadata.descriptive.type.dc=Dublin Core
ui.browse.metadata.descriptive.type.ead.3=Encoded Archival Description 3
ui.browse.metadata.descriptive.type.ead.2002=Encoded Archival Description 2002
```

Por fim deverá ser disponibilizadas as traduções para os nomes dos campos para que possam ser processadas pelo RODA durante a atividade de visualização. As traduções devem estar no ficheiro em `[RODA_HOME]/config/i18n/ServerMessages_pt_PT.properties` e serem adicionadas as entradas necessárias, assegurando que a última parte da chave corresponde com o `xsl:params` incluído no mapeamento da visualização.

O exemplo a seguir descreve como os nomes de campo no exemplo Simple Dublin Core devem ser exibidos na interface de utilizador.

```
crosswalks.dissemination.html.dc.title=Título
crosswalks.dissemination.html.dc.description=Descrição
crosswalks.dissemination.html.dc.contributor=Contribuição
crosswalks.dissemination.html.dc.coverage=Cobertura
crosswalks.dissemination.html.dc.creator=Autor
crosswalks.dissemination.html.dc.date=Data
crosswalks.dissemination.html.dc.format=Formato
crosswalks.dissemination.html.dc.identifier=Identificador
crosswalks.dissemination.html.dc.language=Língua
crosswalks.dissemination.html.dc.publisher=Editor
crosswalks.dissemination.html.dc.relation=Relação
crosswalks.dissemination.html.dc.rights=Direitos
crosswalks.dissemination.html.dc.source=Fonte
crosswalks.dissemination.html.dc.rights=Assuntos
crosswalks.dissemination.html.dc.type=Tipo
```

As terminações das chaves anteriores devem corresponder às entradas xsl:param da seguinte forma:

```
<xsl:param name="i18n.title" />
<xsl:param name="i18n.description" />
<xsl:param name="i18n.contributor" />
<xsl:param name="i18n.coverage" />
<xsl:param name="i18n.creator" />
<xsl:param name="i18n.date" /><xsl:param name="i18n.format" />
<xsl:param name="i18n.identifier" />
<xsl:param name="i18n.language" />
<xsl:param name="i18n.publisher" />
<xsl:param name="i18n.relation" />
<xsl:param name="i18n.rights" />
<xsl:param name="i18n.source" />
<xsl:param name="i18n.subject" />
<xsl:param name="i18n.type" />
```

### Recarregar configuração

Depois de alterar os arquivos de configuração, deve-se reiniciar o RODA para que suas alterações se tornem efetivas. Pode-se fazer isso reiniciando o container ou o servidor de aplicação.
