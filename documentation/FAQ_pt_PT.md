# Questões Frequentes

Questões que nos chegam de utilizadores do roda e as suas respostas.

Caso tenha uma questão que não esteja aqui pode [criar um issue](https://github.com/keeps/roda/issues/new) no GitHub e marcá-lo com a etiqueta "question".

## Visualizadores

### Podemos visualizar os ficheiros diretamente na interface do RODA?

O sistema vem com alguns visualizadores predefinidos para determinados formatos padrão (por exemplo, PDF, imagens, formatos HTML 5, etc.).

Formatos especiais precisam de visualizadores ou conversores especiais para adaptá-los aos visualizadores existentes, por exemplo, Visualizador para o formato SIARD 2. Esses são desenvolvimentos que precisam ser realizados caso a caso.

## Metainformação

### Que formatos de metainformação descritiva é suportada pelo RODA?

Todos os formatos de metainformação descritiva são suportados desde que haja uma gramática em XML Schema (XSD) para os validar. Por omissão, o RODA está configurado com Dublin Core e Encoded Archival Description 2002. Mais esquemas podem ser adicionados.

### O RODA suporta multíplos esquemas de classificação?

O sistema permite a definição de estruturas hierárquicas múltiplas onde podem ser colocados os registos. Cada um dos nós dessa estrutura pode ter uma metainformação descritiva associada. Imagina um sistema de ficheiros/diretórios onde cada diretório pode ter metainformação customizada no formato EAD ou DC (ou qualquer outro tipo). Cada um desses diretórios pode ser um fundo, uma coleção, uma séria ou uma agregação, etc.

### O sistema oferece possibilidades de herdar metainformação de níveis superiores na estrutura?

Atualmente não. Um plugin teria que ser desenvolvido à medida.

### A unidade de descrição pode ser vinculada a um ou mais arquivos noutro arquivo ou sistema?

As unidades de descrições fazem parte do AIP (Archival Information Package), o que significa que representações e ficheiros geralmente estão intimamente ligados à metainformação do registro. No entanto, é possível adicionar hiperligações HTTP a outros recursos que ficam fora do repositório, colocando-os na metainformação descritiva.

### É possível vincular uma descrição de arquivo a uma entidade contextual (por exemplo, autoridade ISAAR)?

O sistema não suporta registos internos de autoridade, no entanto, se você gerir esses registos externamente, poderá vinculá-los editando a metainformação descritiva.

### Como dar suporte a arquivos híbridos (papel e digital)?

É possível ter registos sem representações digitais, ou seja, apenas com metainformação. De uma perspectiva do catálogo, normalmente, é o suficiente para suportar arquivos em papel.

### O aplicativo pode registar o nível de transferência, por exemplo, quem transferiu o quê e quando?

Os SIPs normalmente incluem informações sobre quem, o quê e quando foram criados. O processo de ingestão cria registos de todo o processo de ingestão. No entanto, espera-se que os SIPs sejam colocados num local acessível pelo sistema. Determinar quem copiou SIPs para esses locais está fora do âmbito do sistema.

### Como o sistema pode registar a localização dos arquivos físicos?

Preenchendo o campo de metainformação que tipicamente é <ead:physloc>.

## Pesquisa

### Que atributos de metainformação podemos pesquisar?

A página de pesquisa é totalmente configurável através de ficheiros de configuração. É possível definir atributos, tipos, nomes de rótulos, etc.

### A pesquisa de texto completo é suportada?

Sim, é suportada nativamente pela pesquisa avançada.

### o utilizador pode solicitar documentos analógicos dos arquivos a partir do resultado da pesquisa?

Não. Teria que ser integrado com um sistema externo que tratasse dessas solicitações

### A lista de resultados da pesquisa reflete as permissões aplicadas aos registos apresentados?

Sim. Só pode ver os registos aos quais tem permissões de acesso.

### A auditoria é pesquisável e acessível de uma forma fácil de usar?

Sim. Pode navegar no registo de atividades (conjunto de ações realizadas no repositório) ou na metainformação de preservação (lista de ações de preservação realizadas nos dados) diretamente na interface do RODA.

## Preservação

### Descreva o funcionamento do ambiente de quarentena.

Quando os SIPs estão a ser processados ​​durante a ingestão, caso não sejam aceites, serão movidos para uma pasta especial no sistema de arquivos. O processo de ingestão gera um relatório detalhado que descreve os motivos da rejeição. A partir desse ponto os registos devem ser revistos manualmente.

### Como é que o sistema suporta atividades de preservação?

Esta é uma pergunta complexa que não pode ser respondida em apenas algumas linhas de texto. Dito isto, podemos dizer que o sistema lida com a preservação de várias maneiras:

- Existem ações que realizam verificações regulares de correção dos arquivos ingeridos e avisam os gestores do repositório caso algum problema seja detectado
- O sistema vem com um GUI de gestão de risco incorporada (ou seja, diretório de riscos)
- Existem ações que detetam riscos em ficheiros e adicionam novas ameaças ao diretório de riscos que precisam ser tratadas manualmente (por exemplo, um registo não está suficientemente bem descrito, um ficheiro não segue a política de formato do repositório, um formato de ficheiro é desconhecido ou não há informações de representação, etc.).
- Existem ações que permitem aos gestores de preservação mitigar riscos, por exemplo, realizar conversões de formato (dezenas de formatos suportados).

### Como a aplicação suporta a avaliação, seleção e definição de períodos de retenção?

RODA oferece um worflow para a avaliação e seleção de registos. Por favor, remeta-se a [Política de avaliação e seleção](Disposal.md) para mais informação.

### O sistema regista interações de pesquisa?

Sim. Todas as ações são registadas.

## Requisitos

### Existem requisitos de sistema do lado do cliente para quem consulta os arquivos?

Na verdade, não. Um navegador moderno é suficiente.

## Como fazer

### Como adicionar uma nova linguagem ao sistema?

O [guia de tradução](Translation_Guide.md) contém as instruções para adicionar uma nova linguagem ao sistema.

### Como preparar um sistema para desenvolvimento do RODA?

As instruções de preparação de um ambiente de desenvolvimento do RODA estão disponíveis no [guia de desenvolvimento](Developers_Guide.md).
