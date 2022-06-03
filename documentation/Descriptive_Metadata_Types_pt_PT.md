# Tipo de metainformação descritiva

Na criação de novas entidades intelectuais, um dos passos é a escolha do "tipo" de metainformação descritiva.

O "tipo" refere-se ao esquema de metainformação descritiva a ser usada na descrição daquele registo. Por omissão o RODA suporta:

* **[EAD 2002](https://www.loc.gov/ead/)**: Encoded Archival Description (EAD) versão 2002 é um padrão XML para codificar metainformação descritiva para arquivos, mantido pelo Technical Subcommittee for Encoded Archival Standards of the Society of American Archivists, em parceria com Library of Congress. É geralmente usado por arquivos para descrever tanto documentos nado-digitais como analógicos.
* **[Dublin Core](https://www.dublincore.org/schemas/xmls/)**: A *Dublin Core (DC) Metadata Initiative* suporta inovação no desenho e boas praticas da metainformação. Atualmente, recomenda esquemas de metainformação descritiva, incluindo o *Simple DC XML schema, version 2002-12-12*, que define termos para o *Simple Dublin Core*, i.e. os 15 elementos do *namespace* http://purl.org/dc/elements/1.1/, sem utilização de esquemas de codificação nem refinamentos de elementos.
* **[Key-value](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/schemas/key-value.xsd)**: é um esquema de metadados interno ao RODA que uma definição simples de metadados descritivos em chave-valor, emq ue a chave define o elemento (e.g. "título") e o valor o conteúdo do elemento.
*  **Other**: Tipo genérico de XML sem esquema associado.

Novos "tipos" de metainformação descritiva podem ser adicionados ao RODA seguindo a documentação em [Formatos de metadados](Metadata_Formats.md).

| Tipo de metainformação descritiva | Validação           | Indexação         | Visualização         | Edição      |
|---------------------------|----------------------|------------------|-----------------------|--------------|
| EAD 2002                  | Validação com esquema    | Regras de indexação   | Regras de visualização   | Formulário de edição |
| Dublin Core               | Validação com esquema    | Regras de indexação   | Regras de visualização   | Formulário de edição |
| Key-value                 | Validação com esquema    | Regras de indexação   | Regras de visualização   | Formulário de edição |
| Outros                     | Verificação da forma | Indexação genérica | Visualização genérica | Edição em XML     |

Legenda:
* **Validação com esquema**: O repositório fornece um esquema XML que valida a estrutura e os tipos de dados do ficheiro de metadados. O esquema de validação será usado durante o processo de ingestão para verificar se os metadados incluídos no SIP são válidos de acordo com as restrições estabelicidades, assim como aquando da edição dos metadados via o catálogo.
* **Verificação da forma**: O repositório irá verificar apenas se o ficheiro XML está bem-formado e uma vez que não há nenhum esquema XML definido o repositório não irá verificar se o ficheiro é válido.
* **Regras de indexação**: O repositório tem um XSLT por omissão que transforma o metadado em algo que o sistema de indexação consegue perceber, permitindo assim que haja pesquisa avançada sobre os metadados descritivos.
* **Indexação genérica**: O repositório irá indexar todos os elementos de texto e o valor dos atributos do ficheiro de metadados, contudo como o repositório não tem conhecimento do mapeamento correto entre os elementos do ficheiro e o modelo interno apenas pesquisas básicas serão possíveis.
* **Regras de visualização**: O repositório fornece um XSLT por omissão que transforma o metadado num ficheiro HTML que será mostrado quando o utilizador estiver a navegar pelos AIP presentes no catálogo.
* **Visualização genérica**: O repositório oferece um visualizador genérico dos metadados. Todos os elementos de texto e os atributos serão mostrados sem nenhuma ordem em particular e o seu XPATH será usado para os rótulos.
* **Formulário de edição**: O repositório tem um ficheiro de configuração que contém as instruções de como o formulário para a edição dos metadados deverá ser mostrado.
* **Edição em XML**: O repositório irá mostrar uma área de texto onde o utilizador consegue editar o ficheiro XML diretamente.
