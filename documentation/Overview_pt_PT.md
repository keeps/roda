# Visão Geral

O RODA é um repositório digital que incorpora toda a funcionalidade exigida pelo modelo de referência OAIS. O RODA é capaz de incorporar, gerir e dar acesso a vários tipos de material digital produzido no âmbito da atividade de grandes empresas ou organismos públicos. O RODA é baseado em tecnologias open-source e é suportado pelas normas OAIS, EAD, METS e PREMIS. Para além do disposto, o RODA implementa ainda um workflow de ingestão configurável, que não só valida os pacotes depositados, como também permite a avaliação e selecção do material por parte dos profissionais de arquivo. O RODA permite a recuperação de informação de múltiplas formas – pesquisa básica, pesquisa avançada, navegação por plano de classificação, apresentação de representações em visualizadores especializados ou download de ficheiros. O módulo de administração permite aos gestores do repositório editar metadados descritivos, lançar ações de preservação (e.g. verificações de integridade, migração de formatos, entre outros), controlar os acessos por parte de utilizadores, consultar estatísticas, logs de acesso, entre muitas outras opções.

## O que é que o RODA traz de diferente?

RODA foi desenhado para ser fácil de utilizar por qualquer pessoa (arquivista, bibliotecário, gestor, etc.) e ao mesmo tempo poderoso o suficiente para peritos de preservação executarem tarefas complexas sobre o conteúdo existente.

## Quais as estratégias de preservação que suporta?

A arquitetura e tecnologias do RODA são independentes de qualquer marca de hardware específica. O modelo de dados é suficientemente rico para suportar qualquer estratégia de preservação, por exemplo migração de formatos, emulação ou encapsulamento.

A natureza extensível do RODA permite que este seja melhorado, reconfigurado ou atualizado para reagir a novos requisitos, formatos de ficheiro, ou ferramentas de preservação. Os metadados de preservação são geridos automaticamente pelo repositório. Isto garante a autenticidade do conteúdo digital, providenciando evidências de todos os eventos efetuados no repositório. Ademais, é de notar que todas as ações que são feitas no repositório são registadas por razões de segurança e responsibilização.

No RODA, a gestão de preservação é executada por ações de preservação. O gestor de preservação pode escolher os objectos digitais e as ações de preservação que quer executar. Algumas das ações disponíveis são verificadores da integridade física, conversores de formato, validadores de formato, ferramentas de caracterização, etc.

### Ingestão

A ingestão é composta por um fluxo de trabalho configurável e com múltiplos passos que valida a informação submetida e extrai metainformação técnica dos ficheiros submetidos. O processo de ingestão pode também validar formatos de ficheiro de acordo com as políticas de preservação definidas, e inclui passos de validação da qualidade tanto automáticos como manuais.

### Gestão de informação

Todos os formatos de metainformação descritiva são suportados desde que baseados em XML. De base, o RODA disponibiliza modelos para EAD 2002, EAD 3, e Dublin Core. Isto torna o RODA ideal para instituições de memória de diferentes naturezas, que podem configurar o RODA para suportar os seus formatos de metainformação específicos.

### Armazenamento de arquivo

O armazenamento é gerido no sistema de ficheiros local ou em rede, ou usando o Fedora 4\. Dados e metadados são guardados dentro do AIP (Pacote de Informação de Arquivo) para maior facilidade de replicação e escalabilidadee.

### Acesso

O acesso à informação é providenciado por visualizadores Web embebidos. Múltiplas disseminações (ou derivadas) distintas podes ser disponibilizadas para o mesmo conteúdo, incluindo a que foi originalmente recebida, assim como outras versões alternativas com menor resolução (no caso de imagens).

### Administração

O RODA inclui funcionalidades de adminstração avançadas como gestão de utilizadores, relatórios, configuração de fluxos de ingestão, registo de atividade, gestão de permissões, etc.

### Ações de preservação

O RODA implementa as decisões definidas no planeamento de preservação através da capacidade de executar ações de preservação digital na própria interface Web.

### Gestão de risco

O RODA inclui um directório de riscos que permite aos gestores organizar e mitigar ameaças e oportunidades que circundam o ambiente do repositório.

### Serviços de pesquisa

O RODA tem os seus próprios sistemas de catálogo e pesquisa. O conteúdo por ser encontrado pesquisando pela sua metainformação descritiva, texto integral, características do ficheiro, etc.

## Formatos de ficheiro suportados

O RODA suporta qualquer formato de ficheiro e esquemas de metadados (desde que baseado em XML). Ferramentas de caracterização de ficheiros podem ser executadas durante o processo de ingestão para capturar a metainformação técnica dos ficheiros originais. Ferramentas de conversão de formatos especializadas podem ser executadas sobre o conteúdo digital para o tornar mais adequado para a preservação a longo termo ou para consumo dos utilizadores finais com as tecnologias atuais.

## Confiabilidade - Certificação ISO 16363

Estabelecer a confiança nos repositórios digitais é uma das prioridades da comunidade de preservação digital. A certificação do repositório tem o intuito de assegurar às partes interessadas que as ameaças e oportunidades (como redução de custos) que circundam o ambiente do repositório são identificadas, compreendidas e geridas.

A certificação de um repositório perante a ISO 16363 transcende de grande modo as funcionalidades providenciadas pelo software. Inclui aspetos relacionados com recursos humanos, formação, sustentabilidade financeira, segurança da informação, etc. No entanto, um dos maiores objetivos do RODA é a autenticidade, que é o pré-requisito para a confiabilidade. O desenho e processos que o RODA implementa estão alinhados com as unidades funcionais e a terminologia definida no OAIS, o que simplifica de grande modo a adoção e a usabilidade.
