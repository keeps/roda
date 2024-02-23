<div name="top">

# Publicação de serviços


Depois de desenvolver um plugin usando [roda-plugin-template](), você pode fazer
um pull request para publicá-lo em [https://market.roda-community.org/](https://market.roda-community.org/), 
para que outras pessoas possam encontrar, instalar e usar o serviço.

## Instruções
- Obtenha o código-fonte usando o [guia do programador](./Developers_Guide.md).
- Encontre a pasta [market](../roda-core/roda-core/src/main/resources/config/market) no código fonte no caminho `roda-core/roda-core/src/main/resources/config/market`
- Encontre a pasta que corresponde ao tipo do item que pretende publicar no Marketplace, como por exemplo `services`
- Crie uma pasta com o nome único do fornecedor , como por exemplo `KEEP_SOLUTIONS`
- Adicione o [ficheiro de metadados](#metadata-file) no formato json com as informações do serviço
- Crie um pull request para o repositório git do RODA Community

## Validação
A equipa de desenvolvimento do RODA irá verificar se o pull requeste está em conformidade com as regras da comunidade.
Se estiver em conformidade, o serviço será publicado em [https://market.roda-community.org/](https://market.roda-community.org/)

## Ficheiro de metadados

O Ficheiro de metadados contém as informações necessárias para que o Market e o RODA disponibilizem o serviço para outros utilizadores.

### Requisitos
- O ficheiro deve estar no formato json
- O nome do ficheiro deve ser o classname do serviço e.g. `customDevelopment.json`
- Deve conter os seguintes campos:

| Name            | Description                                                                                            |
|-----------------|--------------------------------------------------------------------------------------------------------|
| id              | O classname do serviço                                                                                 |
| name            | O nome que será exibido no Market e na interface RODA                                                  |
| type            | O Tipo do serviço                                                                                      |
| version         | A versão do serviço                                                                                    |
| description     | A descrição que será exibida no Market e na interface RODA                                             |
| license         | Nome da licensa e endereço para a licensa                                                              |
| homepage        | Uma URL para ou o repositório git do serviço ou uma página Web com detalhes sobre como obter o plug-in |
| vendor          | O nome do fornecedor do serviço                                                                        |
| compatibility   | Lista de versões do RODA suportadas pelo serviço                                                       |
| price           | Custo do serviço                                                                                       |
| plugin          | Deixe este atributo como está no exemplo abaixo                                                        |
| lang            | Linguagens suportadas por este serviço                                                                 |
| region          | Regiões suportadas por este serviço                                                                    |

Exemplo do ficheiro customDevelopment.json
```json
{
    "id": "customDevelopment",
    "name": "Custom development",
    "type": "service",
    "version": "",
    "description": "This service is designed to provide organizations with the flexibility and control they need to achieve their business objectives by creating new Services or integrations that are tailored to their specific requirements. The service covers the full software development life cycle, from initial design and planning to coding, testing, and implementation.",
    "license": {
        "name": "",
        "homepage": "",
    },
    "homepage": "http://docs.roda-enterprise.com/services/custom-development",
    "vendor": {
      "name": "KEEP SOLUTIONS",
      "homepage": "https://keep.pt"
    },
    "compatibility": ["RODA Community 4, RODA Community 5, RODA Enterprise 5"],
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
