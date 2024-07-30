# Políticas de avaliação e seleção

## Tabela de seleção

As tabelas de seleção definem os requisitos mínimos para as acções de manutenção, conservação ou eliminação que a ser executadas nas futuras ou atuais entidades intelectuais deste repositório. Uma entidade intelectual apenas pode ser eliminada se fizer parte de um processo de eliminação gerido pela tabela de seleção associado a essa entidade. É a tabela de seleção dessa entidade intelectual que determina por quanto tempo uma entidade é conservada e consequentemente como é gerida no fim do seu prazo de conservação.

### 1. O que é uma tabela de seleção?

[MoReq2010®](https://moreq.info/) afirma que "Tabelas de seleção são vitais na gerência de registos porque o MoReq2010® especifica que um registo num MCRS só pode ser destruído como parte de um processo de seleção governado pela tabela de seleção atribuída a esse registo. É a tabela de seleção do registo que determina por quanto tempo esse registo deve ser conservado e como o mesmo deve ser eliminado no final do seu período de retenção."

O RODA suporta três tipos de ações de eliminação:

1. Conservar permanentemente:
2. Rever no final do período de conservação;
3. Destruir no final do período de conservação.

O cálculo do período de retenção utiliza o identificador do elemento acionador de conservação e adiciona o seu valor ao período de conservação. Os valores possíveis para o período de conservação são:

1. Sem período de conservação;
2. Dias;
3. Semanas;
4. Meses;
5. Anos.

### 2. O que categoriza uma tabela de seleção?

Os atributos seguintes categorizam uma tabela de seleção:

| *Campo* | *Descrição* | *Obrigatório* |
| --------- |---------- | ------------- |
| Título | O nome identificador ou título de uma tabela de seleção | Verdadeiro |
| Descrição | Descrição da tabela de seleção | Falso |
| Mandato | Referência textual a um instrumento legal ou de outro tipo que confere autoridade a uma tabela de seleção | Falso |
| Notas de Âmbito | Orientação a utilizadores autorizados que indica como melhor aplicar uma entidade particular e indica quaisquer políticas ou limitações do seu uso | Falso |
| Ação de Eliminação | Código que descreve a ação a tomar quando o registo for eliminado (Valores possíveis: Conservar permanentemente, Rever, Destruir) | Verdadeiro |
| Identificador do elemento acionador da conservação | O campo dos metadados descritivos utilizado para calcular o período de conservação | Verdadeiro (Se o Código da ação de eliminação não for Conservar permanentemente) |
| Prazo de conservação | Número de dias, semanas, meses ou anos especificado para conservar um registo após o período de conservação ser acionado | Verdadeiro (Se o Código da ação de eliminação não for Conservar permanentemente) |

### 3. Ciclo de vida dos registos

#### Ciclo de vida de conservação permanente

Este tipo de tabela de seleção, sem acionador de conservação, previne o cálculo de uma data de início da conservação e do subsequente período de conservação.

![Ciclo de vida de conservação permanente](images/permanent_retention_life_cycle.png "Ciclo de vida de conservação permanente")

#### Ciclo de vida de revisão

Quando a ação de eliminação de um registo é rever, o mesmo não é sujeito a destruição imediata. Em vez disso, o resultado da revisão deve incluir a aplicação de uma tabela de seleção ao registo baseada na decisão da revisão. A nova tabela de seleção substituirá a tabela de seleção previamente associada ao registo e especificará então o destino final do registo, ou pode ser utilizada para marcar outra revisão mais tarde,  ou para conservar o registo permanentemente.

![Ciclo de vida de revisão](images/review_life_cycle.png "Ciclo de vida de revisão")

#### Ciclo de vida de destruição

A destruição de registos não está sujeita a nenhuma limitação particular. Como os registos são destruídos vai depender da natureza do conteúdo dos seus componentes. O RODA permite podar metadados descritivos utilizando [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html). Todos os ficheiros associados ao registo são destruídos deixando o registo num estado destruído.

![Ciclo de vida de destruição](images/destruction_life_cycle.png "Ciclo de vida de destruição")

## Condições de eliminação

### 1. O que é uma condição de eliminação?

As condições de eliminação são um conjunto de requisitos que determinam a tabela de seleção para cada uma entidades intelectuais deste repositório. As condições de eliminação podem ser aplicadas a qualquer momento com o de manter a consistência do repositório. As condições de eliminação também podem ser aplicadas durante o processo de ingestão, sendo que são aplicadas segundo uma propriedade que define a prioridade de cada uma das regras. Se uma entidade intelectual nao for coberta por nenhuma regra, não será associada a nenhuma tabela de seleção.

### 2. O que categoriza uma condição de eliminação?

Os atributos seguintes categorizam uma condição de eliminação:

| *Campo* | *Descrição* | *Obrigatório* |
| --------- |---------- | ------------- |
| Ordem | Ordem de prioridade das regras que serão aplicadas no processo de ingestão ou no processo de aplicação | Verdadeiro |
| Título | O nome identificador ou título da condição de eliminação | Verdadeiro |
| Descrição | Descrição da condição de eliminação | Falso |
| Tabela de seleção | Tabela de seleção que será associada ao registo | Verdadeiro |
| Método de seleção | Condição que acionará a condição de eliminação (Valores possíveis: Filho de, campo de metainformação) | Verdadeiro |

### 3. Método de seleção

O método de seleção é o mecanismo responsável por corresponder as condições com os registos no repositório e aplicar a tabela de seleção.

Existem dois tipos de métodos de seleção disponíveis no RODA:

* Filho de: se o registo estiver diretamente abaixo de um determinado AIP.
* Campo de metainformação: se o registo tiver um valor de metainformação descritiva.

### 4. Como funciona?

Condições de eliminação podem ser aplicadas durante o processo de ingestão via um plugin ou a qualquer momento ao repositório. AIP com tabelas de seleção manualmente associadas têm a opção de as substituir ou manter como estão.

## Suspensões de Eliminação

### 1. O que é uma suspensão de substituição?

Suspensões de eliminação são ordens legais ou administrativas que interrompem o processo normal de eliminação e previnem a destruição de uma entidade intelectual enquanto a suspensão de eliminação for mantida. Onde a suspensão de eliminação for associada com um registo individual, esta previne a destruição desse registo enquanto a suspensção de eliminação estiver ativa. Quando a suspensão de eliminação for levantada, o processo de eliminação de registos continua.

### 2. O que categoriza uma suspensão de eliminação?

Os atributos seguintes categorizam uma suspensão de eliminação:

| *Campo* | *Descrição* | *Obrigatório* |
| --------- |---------- | ------------- |
| Título | O nome identificador ou título da suspensão de eliminação | Verdadeiro |
| Descrição | Descrição da suspensão de eliminação | Falso |
| Mandato | Referência textual a um instrumento legal ou de outro tipo que confere autoridade a uma suspensão de eliminação | Falso |
| Notas de Âmbito | Orientação a utilizadores autorizados que indica como melhor aplicar uma entidade particular e indica quaisquer políticas ou limitações do seu uso | Falso |

### 3. Como funciona?

Quando uma suspensão de eliminação é associada a um registo isto previne que o registo seja destruído pelo processo de eliminação de registos e impede também que o registo seja apagado. Para ganhar controlo sob o registo a suspensão de eliminação tem de ser dissociada ou levantada.