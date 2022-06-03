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

```
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema  xmlns:xs="http://www.w3.org/2001/XMLSchema"
            xmlns:dc="http://purl.org/dc/elements/1.1/"
            elementFormDefault="qualified"
            attributeFormDefault="unqualified">

  <xs:import namespace="http://www.w3.org/XML/1998/namespace"
    schemaLocation="xml.xsd">
  </xs:import>

  <xs:complexType name="SimpleLiteral">
    <xs:complexContent mixed="true">
      <xs:restriction base="xs:anyType">
        <xs:sequence>
          <xs:any processContents="lax" minOccurs="0" maxOccurs="0"/>
        </xs:sequence>
        <xs:attribute ref="xml:lang" use="optional"/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>

  <xs:element name="any" type="SimpleLiteral" abstract="true"/>

  <xs:element name="title" substitutionGroup="any"/>
  <xs:element name="creator" substitutionGroup="any"/>
  <xs:element name="subject" substitutionGroup="any"/>
  <xs:element name="description" substitutionGroup="any"/>
  <xs:element name="publisher" substitutionGroup="any"/>
  <xs:element name="contributor" substitutionGroup="any"/>
  <xs:element name="date" substitutionGroup="any"/>
  <xs:element name="type" substitutionGroup="any"/>
  <xs:element name="format" substitutionGroup="any"/>
  <xs:element name="identifier" substitutionGroup="any"/>
  <xs:element name="source" substitutionGroup="any"/>
  <xs:element name="language" substitutionGroup="any"/>
  <xs:element name="relation" substitutionGroup="any"/>
  <xs:element name="coverage" substitutionGroup="any"/>
  <xs:element name="rights" substitutionGroup="any"/>

  <xs:group name="elementsGroup">
    <xs:sequence>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:element ref="any"/>
      </xs:choice>
    </xs:sequence>
  </xs:group>

  <xs:complexType name="elementContainer">
    <xs:choice>
      <xs:group ref="elementsGroup"/>
    </xs:choice>
  </xs:complexType>

   <xs:element name="simpledc" type="elementContainer"/>
</xs:schema> 
```

### Indexação

A atividade de _indexação_ é suportada por  [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) que transforma a metainformação em XML em algo que o motor de indexação compreende. Este ficheiro é responsável pela seleção dos dados a indexar, mapeamento dos dados para nomes de campos específicos e instruir o motor  como indexar os dados de acordo com o seu tipo (i.e., texto, número, data, etc.),

O ficheiro de mapa de índices deverá ser adicionado à pasta de configuração do repositório em: `[RODA_HOME]/config/crosswalks/ingest/`.

O seguinte exemplo demonstra um ficheiro de mapa de índices para a metainformação Simple Dublin Core.

```
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
  exclude-result-prefixes="dc">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"
    omit-xml-declaration="yes" />

  <xsl:template match="/">
    <doc>
      <xsl:apply-templates />
    </doc>
  </xsl:template>
  <xsl:template match="simpledc">
    <xsl:if test="count(title)  &gt; 0">
      <xsl:if test="title[1]/text()">
        <field name="title">
          <xsl:value-of select="title[1]/text()" />
        </field>
      </xsl:if>
      <xsl:for-each select="title">
        <xsl:if test="normalize-space(text())!=''">
          <field name="title_txt">
            <xsl:value-of select="text()" />
          </field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="count(description)  &gt; 0">
      <xsl:if test="description[1]/text()">
        <field name="description">
          <xsl:value-of select="description[1]/text()" />
        </field>
      </xsl:if>
      <xsl:for-each select="description">
        <xsl:if test="normalize-space(text())!=''">
          <field name="description_txt">
            <xsl:value-of select="text()" />
          </field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
    <xsl:for-each select="contributor">
      <xsl:if test="normalize-space(text())!=''">
        <field name="contributor_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="coverage">
      <xsl:if test="normalize-space(text())!=''">
        <field name="coverage_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="creator">
      <xsl:if test="normalize-space(text())!=''">
        <field name="creator_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="count(date)  &gt; 0">
      <xsl:if test="count(date)  &lt; 2">
        <xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$"
        select="date[1]/text()">
        <xsl:matching-substring>
          <xsl:variable name="date">
            <xsl:value-of select="regex-group(0)" />
          </xsl:variable>
          <xsl:if test="not(normalize-space($date)='')">
            <field name="dateInitial">
              <xsl:value-of select="$date" />
              <xsl:text>T00:00:00Z</xsl:text>
            </field>
            <field name="dateFinal">
              <xsl:value-of select="$date" />
              <xsl:text>T00:00:00Z</xsl:text>
            </field>
          </xsl:if>
        </xsl:matching-substring>
      </xsl:analyze-string>
      </xsl:if>
      <xsl:if test="count(date)  &gt; 1">
        <xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$" select="date[1]/text()">
          <xsl:matching-substring>
            <xsl:variable name="date">
              <xsl:value-of select="regex-group(0)" />
            </xsl:variable>
            <xsl:if test="not(normalize-space($date)='')">
              <field name="dateInitial">
                <xsl:value-of select="$date" /><xsl:text>T00:00:00Z</xsl:text>
              </field>
            </xsl:if>
          </xsl:matching-substring>
        </xsl:analyze-string>
        <xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$" select="date[2]/text()">
          <xsl:matching-substring>
            <xsl:variable name="date">
              <xsl:value-of select="regex-group(0)" />
            </xsl:variable>
            <xsl:if test="not(normalize-space($date)='')">
              <field name="dateFinal">
                <xsl:value-of select="$date" />
                <xsl:text>T00:00:00Z</xsl:text>
              </field>
            </xsl:if>
          </xsl:matching-substring>
        </xsl:analyze-string>
      </xsl:if>
      <xsl:for-each select="date">
        <xsl:if test="normalize-space(text())!=''">
          <field name="date_txt">
            <xsl:value-of select="text()" />
          </field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>

    <xsl:for-each select="format">
      <xsl:if test="normalize-space(text())!=''">
        <field name="format_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="identifier">
      <xsl:if test="normalize-space(text())!=''">
        <field name="identifier_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="language">
      <xsl:if test="normalize-space(text())!=''">
        <field name="language_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="publisher">
      <xsl:if test="normalize-space(text())!=''">
        <field name="publisher_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="relation">
      <xsl:if test="normalize-space(text())!=''">
        <field name="relation_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="rights">
      <xsl:if test="normalize-space(text())!=''">
        <field name="rights_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="source">
      <xsl:if test="normalize-space(text())!=''">
        <field name="source_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="subject">
      <xsl:if test="normalize-space(text())!=''">
        <field name="subject_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="type">
      <xsl:if test="normalize-space(text())!=''">
        <field name="type_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <field name="level">item</field>
  </xsl:template>
</xsl:stylesheet>
```

O resultado produzido é um [documento Solr](https://wiki.apache.org/solr/UpdateXmlMessages) pronto a ser indexado pelo RODA. Ver o exemplo abaixo:

```
<doc>
  <field name="title">{{title}}</field>
  <field name="title_txt">{{title}}</field>
  <field name="description">{{description}}</field>
  <field name="description_txt">{{description}}</field>
  <field name="creator_txt">{{creator}}</field>
</doc>
```

### Visualização

A atividade de _visualização_ é suportada por [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) que transforma a metainformação em XML num ficheiro HTML com o propósito de visualização. O resultado desta ação é um ficheiro HTML que será mostrado ao utilizador quando estiver a navegar por um AIP no catálogo.

O ficheiro de mapeamento da visualização deverá ser adicionado à pasta de configuração em `[RODA_HOME]/config/crosswalks/dissemination/html/`.

O seguinte exemplo demonstra a transformação de um ficheiro de metainformação no formato Simple Dublin Core em HTML para ser visualizado:

```
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
    exclude-result-prefixes="dc">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"
        omit-xml-declaration="yes" />
    <xsl:param name="i18n.title" />
    <xsl:param name="i18n.description" />
    <xsl:param name="i18n.contributor" />
    <xsl:param name="i18n.coverage" />
    <xsl:param name="i18n.creator" />
    <xsl:param name="i18n.date" />
    <xsl:param name="i18n.format" />
    <xsl:param name="i18n.identifier" />
    <xsl:param name="i18n.language" />
    <xsl:param name="i18n.publisher" />
    <xsl:param name="i18n.relation" />
    <xsl:param name="i18n.rights" />
    <xsl:param name="i18n.source" />
    <xsl:param name="i18n.subject" />
    <xsl:param name="i18n.type" />

    <xsl:template match="/">
        <div class="descriptiveMetadata">
            <xsl:apply-templates />
        </div>
    </xsl:template>
    <xsl:template match="simpledc">
        <xsl:if test="normalize-space(string-join(title/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.title" />
                </div>
                <xsl:for-each select="title">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(description/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.description" />
                </div>
                <xsl:for-each select="description">
          <xsl:if test="normalize-space(text())!=''">
            <div class="value">
              <xsl:value-of select="text()" />
            </div>
          </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(contributor/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.contributor" />
                </div>
                <xsl:for-each select="contributor">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(coverage/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.coverage" />
                </div>
                <xsl:for-each select="coverage">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(creator/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.creator" />
                </div>
                <xsl:for-each select="creator">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(date/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.date" />
                </div>
                <xsl:for-each select="date">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(format/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.format" />
                </div>
                <xsl:for-each select="format">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(identifier/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.identifier" />
                </div>
                <xsl:for-each select="identifier">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(language/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.language" />
                </div>
                <xsl:for-each select="language">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(publisher/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.publisher" />
                </div>
                <xsl:for-each select="publisher">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(relation/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.relation" />
                </div>
                <xsl:for-each select="relation">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(rights/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.rights" />
                </div>
                <xsl:for-each select="rights">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(source/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.source" />
                </div>
                <xsl:for-each select="source">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(subject/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.subject" />
                </div>
                <xsl:for-each select="subject">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(type/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.type" />
                </div>
                <xsl:for-each select="type">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
```

### Edição

A atividade de _edição_ é suportada por um ficheiro de configuração que instrui o repositório em como criar o formulário para a edição da metainformação existente. O ficheiro de configuração serve o propósito de fornecer um modelo para criar um item novo de metainformação com alguns atributos predefinidos devidamente pré-preenchidos.

O modelo de formulário deverá ser adicionado à pasta de configuração `[RODA_HOME]/config/templates/`. O exemplo seguinte mostra como o ficheiro modelo pode ser combinado com anotações que será usadas para apresentação no editor de metainformação.

```
{{~field name="title"   order='2' auto-generate='title' label="{'en': 'Title'}" xpath="//*:title/string()"}}
{{~field name="id"      order='1' auto-generate='id' label="{'en': 'ID'}" xpath="//*:identifier/string()"}}
{{~field name="creator"   label="{'en': 'Creator'}" xpath="//*:creator/string()"}}
{{~field name="dateInitial" order='3' type='date' label="{'en': 'Initial date'}" xpath="//*:date[1]/string()"}}
{{~field name="dateFinal"   order='4' type='date' label="{'en': 'Final date'}" xpath="//*:date[2]/string()"}}
{{~field name="description" type='text-area' label="{'en': 'Description'}" xpath="//*:description/string()"}}
{{~field name="producer"  label="{'en': 'Producer'}" xpath="//*:publisher/string()"}}
{{~field name="rights"    label="{'en': 'Rights'}" xpath="//*:rights/string()"}}
{{~field name="language"  auto-generate='language' label="{'en': 'Language'}" xpath="//*:language/string()"~}}
<?xml version="1.0"?>

<simpledc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="../schemas/dc.xsd">
  <title>{{title}}</title>
  <identifier>{{id}}</identifier>
  <creator>{{creator}}</creator>
  <date>{{dateInitial}}</date>
  <date>{{dateFinal}}</date>
  <description>{{description}}</description>
  <publisher>{{producer}}</publisher>
  <rights>{{rights}}</rights>
  <language>{{language}}</language>
</simpledc>
```

O modelo de formulário é baseado no [Handlebars](http://handlebarsjs.com). Cada campo que é esperado ser mostrado no editor de metainformação deve ser identificado no início do ficheiro por _campo_ (p.e. `{{~field name="title"~}}`). Existem diferentes opções que podem ser usadas para modificar a forma como cada campo é mostrado. Estas opções são pares de chave-valor, p.e. `label="Title of work"`, onde a chave é o nome da opção e o valor é o valor para aquela opção.

As opções disponíveis que alteram o comportamento dos campos são:

*   **order** - ordem pela qual os campos serão mostrado no editor da metainformação
*   **label** - a etiqueta do campo a mostrar à esquerda do valor no editor. O formato é um JSON. As chaves são o identificador do idioma. Exemplo: {"en": "Title", ""pt_PT": "Título"}"
*   **labeli18n** - chave i18n para a etiqueta do campo (ver secção "Internacionalização de texto")
*   **description** - texto com alguma ajuda relacionada com o campo
*   **value** - o valor pre-definido do campo
*   **mandatory** - Se marcado como verdadeiro (true) a etiqueta é colocada em negrito para chamar à atenção
*   **hidden** - Se marcado a verdadeiro (true) o campo é escondido
*   **xpath** - o xpath para qual o campo deverá vincular no documento XML da metainformação.
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
