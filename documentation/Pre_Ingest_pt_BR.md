# Como é que a informação é guardada no RODA?

A fase de **pré-ingestão** refere-se às atividades de preparação realizadas por um _Produtor_ antes da submissão de materiais digitais no repositório. O seu principal objetivo é garantir que os **Pacotes de Informação de Submissão (SIPs)** sejam criados de acordo com os requisitos do repositório, facilitando assim o processo de ingestão.

Esta fase incluí, tipicamente, as seguintes atividades:

## Acordo de aquisição

O **Acordo de aquisição** define a relação formal entre o _Produtor_ e o _Repositório_. Estabelecendo:

- O tipo de conteúdo a ser submetido;
- Requisitos legais, técnicos e procedimentais;
- As reponsabilidades de cada parte;
- A estrutura e formato esperado dos SIPs.

Este acordo é um documento tipicamente escrito e deve ser aceite por ambas as partes antes do conteúdo ser submetido.

## Organização de Conteúdos

Como parte do acordo de aquisição, o _Produtor_ é usualmente autorizado a depositar conteúdo numa área específica do repositório, com base no **plano de classificação** (uma estrutura hierárquica que é usada para organizar o conteúdo dentro do repositório)

O repositório oferece um **Plano de Classificação** base em formato máquina que facilita aos Produtores alinharem os seus SIPs com a organização interna do repositório.

O ficheiro pode ser descarregado e importado para o **RODA-in**, uma ferramenta desenha para ajudar na preparação e estruturação dos SIPs.

📎 [Descarregar o plano de classificação](/api/v2/classification-plans)

## Preparação do SIP

O _Produtor_ é reponsável por criar um ou mais **Pacotes de Informação de Submissão (SIPs)**, de acordo com o especificado no acordo de aquisição. Cada SIP deve: 

- Conter todo o conteúdo exigido e os metadados associados;
- Seguir as diretrizes estruturais e técnicas acordadas;
- Estar empacotado num formato aceite pelo repositório (por exemplo, E-ARK, BagIt).

Para apoiar esta tarefa, estão disponíveis diferentes ferramentas consoante as preferências de fluxo de trabalho do Produtor:

### RODA-in

Para os utilizadores que preferem uma interface gráfica, o **[RODA-in](http://rodain.roda-community.org)** oferece uma forma intuitiva de:

- Organizar conteúdos e metadados;
- Atribuir coleções e categorias;
- Validar a estrutura do pacote;
- Exportar os SIPs no formato apropriado.

### Ferramentas e bibliotecas de linha de comandos

Para fluxos de trabalho automatizados ou de grande escala, estão disponíveis várias ferramentas e bibliotecas de código aberto para preparar SIPs através da linha de comandos ou ambientes de scripting:

- **[Commons-IP](https://github.com/keeps/commons-ip)** - Uma ferramenta e biblioteca baseada em Java para criar, validar e converter Pacotes de Informação OAIS. Suporta múltiplos formatos de empacotamento, incluindo E-ARK (v1, v2.0.4, v2.1.0, v2.2.0), BagIt e SIP do tipo húngaro 4.

- **[.NET E-ARK SIP](https://igfej-justica-gov-pt.github.io/dotnet-eark-sip/)** - Uma ferramenta de linha de comandos e uma biblioteca .NET para gerar SIPs compatíveis com o E-ARK. Ideal para integração com ambientes baseados em Microsoft.

- **[eArchiving Tool Box (EATB)](https://github.com/E-ARK-Software/eatb)** - Uma coleção de ferramentas baseadas em Python desenvolvidas no âmbito do projeto E-ARK para a criação de SIPs e outros pacotes de informação, com suporte para scripting e fluxos de trabalho em lote.

> 🛠️ Estas ferramentas são recomendadas para instituições com grandes volumes de conteúdo ou necessidades complexas de automação.

## Transferência de Materiais

Depois de preparados, os SIPs devem ser transferidos para o repositório. Os SIPs são inicialmente colocados numa **zona de quarentena**, onde aguardam validação e processamento por parte do repositório.

Existem vários métodos de transferência suportados:

### Transferência por HTTP

1. Inicie sessão na interface web do repositório usando as suas credenciais.
2. Aceda a **Ingestão > Transferência** e entre na sua pasta pessoal (crie uma, se necessário).
3. "Carregue os seus SIPs."
4. Notifique a equipa do repositório de que o material está pronto para ingestão.

### Transferência por FTP

1. Ligue-se ao servidor FTP fornecido usando as suas credenciais.
2. Opcionalmente, crie uma pasta para o seu lote de ingestão.
3. "Carregue os seus SIPs."
4. Notifique o repositório de que os SIPs estão disponíveis.

### Transferência por Suporte Físico

1. Guarde os SIPs num suporte físico (por exemplo, pen USB, disco rígido externo).
2. Entregue-o na seguinte morada:
   `[Morada do repositório]`

> ⚠️ Certifique-se de que o suporte está devidamente identificado e que a sua integridade foi verificada antes da entrega.

## Processo de Ingestão

Após a transferência, o repositório iniciará o processo de **Ingestão**. Este processo inclui os seguintes passos:

- **Receção e validação** dos SIPs;
- **Controlo de qualidade** para garantir a conformidade com os requisitos de formato e metadados;
- **Geração dos Pacotes de Informação Arquivística (AIPs)** para preservação a longo prazo;
- **Extração da Informação Descritiva** para indexação e pesquisa no catálogo do repositório;
- **Atualização dos sistemas de Armazenamento Arquivístico e Gestão de Dados**.

O processo de ingestão garante que todo o conteúdo é corretamente arquivado, disponível para descoberta e preservado de acordo com as políticas estabelecidas.

## Notas

- O SIP não deve incluir conteúdo ou metadados fora do âmbito acordado.
- O repositório pode rejeitar os SIPs que não cumpram a validação durante a ingestão.
- Pode ser enviado um email para notificar os Produtores e a equipa do repositório sobre qualquer processo de ingestão.
- Contacte a equipa do repositório para obter apoio em questões relacionadas com o RODA-in ou transferências.
