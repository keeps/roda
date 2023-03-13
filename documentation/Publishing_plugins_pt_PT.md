# Publicação de plugins

Depois de desenvolver um plugin usando [roda-plugin-template](https://github.com/keeps/roda-plugin-template), você pode fazer
um pull request para publicá-lo em [https://market.roda-community.org/](https://market.roda-community.org/), 
para que outras pessoas possam encontrar, instalar e usar o plug-in.

## Instruções
- Obtenha o código-fonte usando o [guia do programador](./Developers_Guide.md).
- Encontre a pasta [market](../roda-core/roda-core/src/main/resources/config/market) no código fonte no caminho `roda-core/roda-core/src/main/resources/config/market`
- Crie uma pasta com o nome único do fornecedor , como por exemplo `KEEP_SOLUTIONS`
- Adicione o [ficheiro de metadados](#metadata-file) no formato json com as informações do plugin
- Crie um pull request para o repositório git do RODA Community

## Validação
A equipa de desenvolvimento do RODA irá verificar se o pull requeste está em conformidade com as regras da comunidade.
Se estiver em conformidade, o plugin será publicado em [https://market.roda-community.org/](https://market.roda-community.org/)

## Ficheiro de metadados

O Ficheiro de metadados contém as informações necessárias para que o Market e o RODA disponibilizem o Plugin para outros utilizadores.

### Requisitos
- O ficheiro deve estar no formato json
- O nome do ficheiro deve ser o classname do plugin e.g. `org.roda.core.plugins.external.AIPValidatorPlugin.json`
- Deve conter os seguintes campos:

| Name                | Description                                                                                           |
|---------------------|-------------------------------------------------------------------------------------------------------|
| id                  | O classname do plugin                                                                                 | 
| name                | O nome que será exibido no Market e na interface RODA                                                 |
| type                | O Tipo do plugin                                                                                      |
| version             | A versão do plugin                                                                                    |
| description         | A descrição que será exibida no Market e na interface RODA                                            |
| categories          | As categorias em que o plugin se encaixa                                                              |
| objectClasses       | O classname dos objetos do RODA que podem ser alvo do plugin                                          |
| homepage            | Uma URL para ou o repositório git do plugin ou uma página Web com detalhes sobre como obter o plug-in |
| vendor              | O nome do fornecedor do plugin                                                                        |
| minSupportedVersion | Versão RODA mínima suportada pelo Plugin                                                              |
| maxSupportedVersion | Versão RODA máxima suportada pelo Plugin                                                              |

Exemplo do ficheiro org.roda.core.plugins.external.AIPValidatorPlugin.json
```json
{
  "id": "org.roda.core.plugins.external.AIPValidatorPlugin",
  "name": "E-ARK AIP Validator",
  "type": "MISC",
  "version": "1.0",
  "description": "The E-ARK AIP Validator plugin provides a comprehensive evaluation to ensure that AIPs meet the requirements outlined in the E-ARK specification, version 2.0.4.",
  "categories": [
    "validation"
  ],
  "objectClasses": [
    "org.roda.core.data.v2.ip.IndexedAIP",
    "org.roda.core.data.v2.ip.AIP"
  ],
  "homepage": "https://roda-enterprise.keep.pt/plugins/org.roda.core.plugins.external.AIPValidatorPlugin",
  "vendor": "KEEP SOLUTIONS",
  "minSupportedVersion": "4.1.0",
  "maxSupportedVersion": "5.0.0"
}
```

