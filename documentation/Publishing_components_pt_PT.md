<div name="top">

# Publicação de componentes

Depois de desenvolver um plugin usando [roda-plugin-template](), você pode fazer
um pull request para publicá-lo em [https://market.roda-community.org/](https://market.roda-community.org/), 
para que outras pessoas possam encontrar, instalar e usar o componente.

## Instruções
- Obtenha o código-fonte usando o [guia do programador](./Developers_Guide.md).
- Encontre a pasta [market](../roda-core/roda-core/src/main/resources/config/market) no código fonte no caminho `roda-core/roda-core/src/main/resources/config/market`
- Encontre a pasta que corresponde ao tipo do item que pretende publicar no Marketplace, como por exemplo `components`
- Crie uma pasta com o nome único do fornecedor , como por exemplo `KEEP_SOLUTIONS`
- Adicione o [ficheiro de metadados](#metadata-file) no formato json com as informações do componente
- Crie um pull request para o repositório git do RODA Community

## Validação
A equipa de desenvolvimento do RODA irá verificar se o pull requeste está em conformidade com as regras da comunidade.
Se estiver em conformidade, o componente será publicado em [https://market.roda-community.org/](https://market.roda-community.org/)

## Ficheiro de metadados

O Ficheiro de metadados contém as informações necessárias para que o Market e o RODA disponibilizem o componente para outros utilizadores.

### Requisitos
- O ficheiro deve estar no formato json
- O nome do ficheiro deve ser o classname do componente e.g. `dropFolders.json`
- Deve conter os seguintes campos:

| Name             | Description                                                                                               |
|------------------| --------------------------------------------------------------------------------------------------------- |
| id               | O classname do componente                                                                                 |
| name             | O nome que será exibido no Market e na interface RODA                                                     |
| type             | O Tipo do componente                                                                                      |
| version          | A versão do componente                                                                                    |
| description      | A descrição que será exibida no Market e na interface RODA                                                |
| license          | Nome da licensa e endereço para a licensa                                                                 |
| homepage         | Uma URL para ou o repositório git do componente ou uma página Web com detalhes sobre como obter o plug-in |
| vendor           | O nome do fornecedor do componente                                                                        |
| compatibility    | Lista de versões do RODA suportadas pelo componente                                                       |
| price            | Custo do componente                                                                                       |
| plugin           | Deixe este atributo como está no exemplo abaixo                                                           |
| lang             | Linguagens suportadas por este componente                                                                 |
| region           | Regiões suportadas por este componente                                                                    |

Exemplo do ficheiro dropFolders.json
```json
{
    "id": "dropFolders",
    "name": "Drop folders",
    "type": "component",
    "version": "",
    "description": "Facilitates the automated, unsupervised ingestion of submission information packages via shared folders, which is crucial for a smooth integration with other data production systems.",
    "license": {
        "name": "",
        "homepage": "",
    },
    "homepage": "http://docs.roda-enterprise.com/components/drop-folders",
    "vendor": {
      "name": "KEEP SOLUTIONS",
      "homepage": "https://keep.pt"
    },
    "compatibility": ["RODA Enterprise 5"],
    "price": "paid",
    "plugin": {
      "objectClasses": [],
      "categories": [],
      "type": "",
    },
    "lang": [],
    "region": [],
  },
```
[(Back to top)](#top)