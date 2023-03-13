# Guia do programador

Este é um guia rápido e simples sobre como começar a programar no RODA.

## Obtenha o código-fonte

Pode obter facilmente o código-fonte ao clonar o projeto para a sua máquina (apenas precisa de ter instalado o git):

```bash
$ git clone https://github.com/keeps/roda.git
```

Se planeia contribuir para o RODA, primeiro precisa de bifurcar o repositório para a sua conta de GitHub e depois de cloná-lo para a sua máquina. Para aprender como fazê-lo, por favor, leia este [artigo GitHub](https://help.github.com/articles/fork-a-repo).


<!-- Aviso: alterar este título quebrará as ligações -->
## Como compilar e executar

O RODA usa o sistema de compilação [Apache Maven](http://maven.apache.org/). Sendo um projeto Maven de múltiplos módulos, na raiz **pom.xml** está declarada toda a informação importante para todos os módulos no RODA, como:

* Módulos a serem incluídos no ciclo de compilação predefinido
* Repositórios Maven a serem usados
* Gestão de dependências (os números de versão são declarados aqui e são herdados pelos submódulos)
* Gestão de plugins (números de versão são declarados aqui e são herdados pelos submódulos)
* Perfis disponíveis (Existem muitos perfis utilizáveis. Um que apenas inclui os projetos principais (**core**), outro que inclui projetos de interface do utilizador (**wui**), outro que compila a imagem docker do RODA wui (**wui,roda-wui-docker**), e outros que, por exemplo, podem incluir projetos de plugins externos que podem ser integrados no RODA (**all**)).

### Dependências

Os pré-requisitos para compilar o RODA são:

* Cliente Git
* Apache Maven
* Oracle Java 8

Para instalar todas as dependências nos sistemas baseados em Debian execute:

```bash
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer oracle-java8-set-default git maven ant
```

### Compilação

Para compilar, vá até à pasta de fontes do RODA e execute o comando:

```bash
$ mvn clean package
```

Use o seguinte comando para ignorar os testes unitários (mais rápido).

```bash
$ mvn clean package -Dmaven.test.skip=true
```


Após uma compilação bem-sucedida, a aplicação web RODA estará disponível em `roda-ui/roda-wui/target/roda-wui-VERSION.war`. Para implementá-la, simplesmente, coloque-a no seu servlet container favorito (ex. Apache Tomcat) e já está.

## Como preparar o ambiente de desenvolvimento

### Software necessário

Para além do software necessário para compilar o RODA, necessita:

* Eclipse for Java ([Página de download](http://www.eclipse.org/downloads/))
* Eclipse Maven Plugin ([Download e instruções de instalação](http://www.eclipse.org/m2e/))

Opcionalmente, pode instalar as seguintes ferramentas:

* O Google Plugin for Eclipse ([Download e instruções de instalação](https://developers.google.com/eclipse/docs/getting_started)) é útil para desenvolver e testar desenvolvimentos da interface gráfica de utilizador.

**NOTE:** Esta não é uma lista restritiva de software a ser usado para desenvolver o RODA (uma vez que outros softwares, como IDEs, podem ser usados no lugar do sugerido.)

### Como importar o código no Eclipse

1. Inicie o Eclipse
2. Selecione "Ficheiro > Importar". Depois, selecione "Maven > Projetos Maven Existentes" e clique em "Seguinte"
3. No "Diretório de Raiz", procure o diretório do código-fonte do RODA no seu sistema de ficheiros e selecione "Abrir"
4. Opcionalmente, pode adicioná-lo a um "Working set"
5. Clique em "Terminar"


## Estrutura do código

O RODA está estruturado da seguinte maneira:

### /

* **pom.xml** - configuração Maven base do projeto
* **code-style** - checkstyle e ficheiros formatadores de código Eclipse
* **roda-common/** - este módulo contém componentes comuns usados por outros módulos/projetos
  * **roda-common-data** - este módulo contém todos os objetos de modelo relacionados com o RODA que são usados em todos os outros módulos/projetos
  * **roda-common-utils** - este módulo contém serviços base para serem usados por outros módulos/projetos

### /roda-core/

  * **roda-core** - este módulo contém serviços de modelação, indexação e armazenamento, com especial atenção nos seguintes pacotes:
    * **common** - este pacote contém serviços relacionados com o roda-core
    * **storage** - este pacote contém uma abstração de armazenamento (inspirada no OpenStack Swift) e algumas implementações (neste momento, um sistema de ficheiros e a implementação baseada em Fedora 4)
    * **model** - este pacote contém a totalidade da lógica que remonta os objetos do RODA (ex. operações CRUD, etc.), compilados sobre a abstração de armazenamento do RODA
    * **index** - este pacote contém a totalidade da lógica de indexação para os objetos de modelo do RODA, trabalhando em conjunto com o modelo do RODA através do padrão Observável
    * **migration** - este pacote contém a totalidade da lógica de migração (ex. sempre que ocorre uma alteração no objeto de modelo, pode ser necessária uma migração)
  * **roda-core-tests** - este módulo contém testes para o módulo roda-core. Além disso, este módulo pode ser adicionado como uma dependência aos outros projetos que, por exemplo, têm plugins e que querem testá-los mais facilmente.

### /roda-ui/

* **roda-wui**- este módulo contém a aplicação web Web User Interface (WUI) e os serviços web REST. Basicamente, os componentes que permitem a interação programática com o RODA.

### /roda-common/

* **roda-common-data** - este módulo contém todos os objetos de modelo relacionados com o RODA usados em todos os outros módulos/projetos
* **roda-common-utils** - este módulo contém serviços base para serem usados por outros módulos/projetos


## Contributo

### Código-fonte

1. [Bifurque o projeto RODA GitHub](https://help.github.com/articles/fork-a-repo)
2. Altere o código e avance para o projeto bifurcado
3. [Submeta um pull request](https://help.github.com/articles/using-pull-requests)

Para aumentar a quantidade de alterações ao seu código que serão aceites e adicionadas à fonte do RODA, aqui está uma lista de coisas que deve examinar antes de submeter uma contribuição. Por exemplo:

* Ter testes unitários (que cubram pelo menos 80% do código)
* Ter documentação (pelo menos 90% da API pública)
* Concordar com o acordo de licença do contribuidor, que certifica que qualquer código contribuído é trabalho original e que os direitos de autor são transferidos para o projeto

### Traduções

Se gostaria de traduzir o RODA para um novo idioma, por favor, leia o [Guia de Tradução](Translation_Guide.md).

### Plugins externos

Para criar novos plugins e usá-los no RODA, é necessário:

1. Criar um novo projeto Maven que depende do roda-core e declarar o nome qualificado da classe do plugin em _pom.xml_
2. A classe do plugin deve estender a classe **AbstractPlugin** e implementar os métodos necessários
3. Depois de criar o plugin, é necessário gerar um ficheiro jar
4. Esse ficheiro jar deve, então, ser incluído na pasta de instalação base do RODA, mais especificamente em **config/plugins/PLUGIN_NAME/**
5. Publicar plugin no market ([ver instruções](./Publishing_plugins.md))

## API REST

O RODA é completamente controlado através de uma API REST. Isto é fantástico para desenvolver serviços externos ou integrar outras aplicações com o repositório. A documentação do API está disponível em [https://demo.roda-community.org/api-docs/](https://demo.roda-community.org/api-docs/).

### Desenvolver integrações de terceiros

Se estiver interessado em desenvolver uma integração com o RODA através da API REST, por favor, contacte a equipa do produto para mais informações, deixando uma questão ou comentário em https://github.com/keeps/roda/issues.
