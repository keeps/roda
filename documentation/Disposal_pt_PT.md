# Avaliação e seleção

## Tabela de seleção

Por favor recorra a *Ajuda* > *Manual de utilização* > *Política de avaliação e seleção* para mais informação à cerca de tabelas de seleção.

### 1. Configurar o RODA para mostrar valores na lista de identificadores do elemento acionador da conservação

O identificador do elemento acionador da conservação é povoado usando os itens de pesquisa avançada. A partir desses campos aqueles que tiverem como tipo de dados `date_interval` serão usados para o cálculo do período de conservação.
Por favor recorra a *Ajuda* > *Manual de utilização* > *Pesquisa avançada* para mais informações à cerca de como adicionar itens de pesquisa avançada.

## Regra de eliminação

Por favor recorra a *Ajuda* > *Manual de utilização* > *Política de avaliação e seleção* para mais informação à cerca de condições de eliminação.

### 1. Configurar o RODA para mostrar valores no método de seleção 'campo de metainformação'

O campo de metainformação é povoada usando os itens de pesquisa avançada. A partir desses campos aqueles que tiverem como tipo de dados `text` serão usados. RODA pode ser configurado para ignorar campos da pesquisa anterior. Para o fazer, altere o `roda-wui.properties` para adicionar o campo à lista a ignorar. Por omissão, o RODA mostra todos os campos do tipo `text`.

```javaproperties
ui.disposal.rule.blacklist.condition = description
```

Por favor recorra a *Ajuda* > *Manual de utilização* > *Pesquisa avançada* para mais informação à cerca de como adicionar itens de pesquisa avançada.

Por favor recorra a *Ajuda* > *Configuração* > *Formatos de metainformação* para mais informação à cerca da configuração de metainformação descritiva no RODA.
