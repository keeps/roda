
# Visão Geral

O RODA é um repositório digital que incorpora toda a funcionalidade exigida pelo modelo de referência OAIS. O RODA é capaz de incorporar, gerir e dar acesso a vários tipos de material digital produzido no âmbito da atividade de grandes empresas ou organismos públicos. 

O RODA é baseado em tecnologias open-source e é suportado pelas normas OAIS, EAD, METS e PREMIS. Para além do disposto, o RODA implementa ainda um workflow de ingestão configurável, que não só valida os pacotes depositados, como também permite a avaliação e selecção do material por parte dos profissionais de arquivo. 

## O RODA permite a recuperação de informação de múltiplas formas – pesquisa básica, pesquisa avançada, navegação por plano de classificação, apresentação de representações em visualizadores especializados ou download de ficheiros. 

* O módulo de administração permite aos gestores do repositório editar metadados descritivos, executar ações de preservação (e.g. verificações de integridade, migração de formatos, entre outros), controlar os acessos por parte de utilizadores, consultar estatísticas, logs de acesso, entre muitas outras opções.
* O que é que o RODA traz de diferente?
* RODA foi desenhado para ser fácil de utilizar por qualquer pessoa (arquivista, bibliotecário, gestor, etc.) e ao mesmo tempo poderoso o suficiente para peritos de preservação executarem tarefas complexas sobre o conteúdo existente.
* Quais as estratégias de preservação que suporta?
* A arquitetura e tecnologias do RODA são independentes de qualquer marca de hardware específica. O modelo de dados é suficientemente rico para suportar qualquer estratégia de preservação, por exemplo migração de formatos, emulação ou encapsulamento.
* A natureza extensível do RODA permite que este seja melhorado, reconfigurado ou atualizado para reagir a novos requisitos, formatos de ficheiro, ou ferramentas de preservação. Os metadados de preservação são geridos automaticamente pelo repositório. Isto garante a autenticidade do conteúdo digital, providenciando evidências de todos os eventos efetuados no repositório. Ademais, é de notar que todas as ações que são feitas no repositório são registadas por razões de segurança e responsibilização.
* No RODA, a gestão de preservação é executada por ações de preservação. O gestor de preservação pode escolher os objectos digitais e as ações de preservação que quer executar. Algumas das ações disponíveis são verificadores da integridade física, conversores de formato, validadores de formato, ferramentas de caracterização, etc.
* Ingestão
* A ingestão é composta por um fluxo de trabalho configurável e com múltiplos passos que valida a informação submetida e extrai metainformação técnica dos ficheiros submetidos. O processo de ingestão pode também validar formatos de ficheiro de acordo com as políticas de preservação definidas, e inclui passos de validação da qualidade tanto automáticos como manuais.
* Gestão de informação
* Todos os formatos de metainformação descritiva são suportados desde que baseados em XML. De base, o RODA disponibiliza modelos para EAD 2002, EAD 3, e Dublin Core. Isto torna o RODA ideal para instituições de memória de diferentes naturezas, que podem configurar o RODA para suportar os seus formatos de metainformação específicos.
* Armazenamento de arquivo
* O armazenamento é gerido no sistema de ficheiros local ou em rede, ou usando o Fedora 4\. Dados e metadados são guardados dentro do AIP (Pacote de Informação de Arquivo) para maior facilidade de replicação e escalabilidadee.
* Acesso
* O acesso à informação é providenciado por visualizadores Web embebidos. Múltiplas disseminações (ou derivadas) distintas podes ser disponibilizadas para o mesmo conteúdo, incluindo a que foi originalmente recebida, assim como outras versões alternativas com menor resolução (no caso de imagens).

Administração


## O RODA inclui funcionalidades de adminstração avançadas como gestão de utilizadores, relatórios, configuração de fluxos de ingestão, registo de atividade, gestão de permissões, etc.

Ações de preservação

### O RODA implementa as decisões definidas no planeamento de preservação através da capacidade de executar ações de preservação digital na própria interface Web.

Gestão de risco

### O RODA inclui um directório de riscos que permite aos gestores organizar e mitigar ameaças e oportunidades que circundam o ambiente do repositório.

Serviços de pesquisa

O RODA tem os seus próprios sistemas de catálogo e pesquisa. O conteúdo por ser encontrado pesquisando pela sua metainformação descritiva, texto integral, características do ficheiro, etc.

### Formatos de ficheiro suportados

O RODA suporta qualquer formato de ficheiro e esquemas de metadados (desde que baseado em XML). Ferramentas de caracterização de ficheiros podem ser executadas durante o processo de ingestão para capturar a metainformação técnica dos ficheiros originais. Ferramentas de conversão de formatos especializadas podem ser executadas sobre o conteúdo digital para o tornar mais adequado para a preservação a longo termo ou para consumo dos utilizadores finais com as tecnologias atuais.

### Como é que a informação é guardada no RODA?

O RODA é compatível com as especificações de pacotes de SIP, AIP e DIP publicadas pelo [DILCIS Board](http://dilcis.eu), um comité situado sob a alçada do [DLM Forum](http://dlmforum.eu) que é responsável por definir standards para a troca de informação entre arquivos digitais e sistemas de gestão documental.

### Confiabilidade - Certificação ISO 16363

Estabelecer a confiança nos repositórios digitais é uma das prioridades da comunidade de preservação digital. A certificação do repositório tem o intuito de assegurar às partes interessadas que as ameaças e oportunidades (como redução de custos) que circundam o ambiente do repositório são identificadas, compreendidas e geridas.

### A certificação de um repositório perante a ISO 16363 transcende de grande modo as funcionalidades providenciadas pelo software. Inclui aspetos relacionados com recursos humanos, formação, sustentabilidade financeira, segurança da informação, etc. No entanto, um dos maiores objetivos do RODA é a autenticidade, que é o pré-requisito para a confiabilidade. O desenho e processos que o RODA implementa estão alinhados com as unidades funcionais e a terminologia definida no OAIS, o que simplifica de grande modo a adoção e a usabilidade.

The Ingest process contains services and functions to accept Submission Information Packages (SIPs) from Producers, prepare Archival Information Packages (AIPs) for storage, and ensure that Archival Information Packages and their supporting Descriptive Information become established within the repository. This page lists all the ingest jobs that are currently being executed, and all the jobs that have been run in the past. In the right side panel, it is possible to filter jobs based on their state, user that initiated the job, and start date. By clicking on an item from the table, it is possible to see the progress of the job as well as additional details.

### Validação

Assessment is the process of determining whether records and other materials have permanent (archival) value. Assessment may be done at the collection, creator, series, file, or item level. Assessment can take place prior to donation and prior to physical transfer, at or after accessioning. The basis of assessment decisions may include a number of factors, including the records' provenance and content, their authenticity and reliability, their order and completeness, their condition and costs to preserve them, and their intrinsic value.

### Ações de preservação

Preservation actions are tasks performed on the contents of the repository that aim to enhance the accessibility of archived files or to mitigate digital preservation risks. Within RODA, preservation actions are handled by a job execution module. The job execution module allows the repository manager to run actions over a given set of data (AIPs, representations or files). Preservation actions include format conversions, checksum verifications, reporting (e.g. to automatically send SIP acceptance/rejection emails), virus checks, etc.

### Ações internas

Internal actions are complex tasks performed by the repository as background jobs that enhance the user experience by not blocking the user interface during long lasting operations. Examples of such operations are: moving AIPs, reindexing parts of the repository, or deleting a large number of files.

### Utilizadores e grupos

The user management service enables the repository manager to create or modify login credentials for each user in the system. This service also allows the manager to define groups and permissions for each of the registered users. Managers may also filter users and groups currently being displayed by clicking on the available options in the right side panel. To create a new user, click the button "Add user". To create a new user group, click the button "Add group". To edit an existing user or group, click on an item from the table.

### Registo de atividade

Event logs are special files that record significant events that happen in the repository. For example, a record is kept every time a user logs in, when a download is made or when an modification is made to a descriptive metadata file. Whenever these events occur, the repository records the necessary information in the event log to enable future auditing of the system activity. For each event the following information is recorded: date, involved component, system method or function, target objects, user that executed the action, the duration of action, and the IP address of the user that executed the action. Users are able to filter events by type, date and other attributes by selecting the options available in the right side panel.

### Notificações

A área de notificações permite ao gestor do repositório saber que notificações foram enviadas pelo sistema, e que utilizadores acusaram a sua receção.

### Configurar novas estatisticas

This page shows a dashboard of statistics concerning several aspects of the repository. Statistics are organised by sections, each of these focusing on a particular aspect of the repository, e.g. issues related to metadata and data, statistics about ingest and preservation processes, figures about users and authentication issues, preservation events, risk management and notifications.

### Diretório de riscos

The risk register lists all identified risks that may affect the repository. It should be as comprehensive as possible to include all identifiable threats, and generally contain an estimated probability of each risk event occurring, the severity or possible impact of the risk, and its probable timing or anticipated frequency. Risk mitigation is the process of defining actions to enhance opportunities and reduce threats to repository objectives.

### Rede de informação de representação

Representation information is any information required to understand and render both the digital material and the associated metadata. Digital objects are stored as bitstreams, which are not understandable to a human being without further data to interpret them. Representation information is the extra structural or semantic information, which converts raw data into something more meaningful.

### Format register (deprecated)

O diretório de formatos é um registo de informação técnica sobre formatos digitais com o objetivo de suportar os serviços de preservação implementados pelo repositório.
