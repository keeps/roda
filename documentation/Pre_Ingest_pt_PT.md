# Instruções de pré-ingestão

O processo de pré-ingestão oferece a capacidade de criar Pacotes de Informação de Submissão (SIP), contendo da dos e metadados (numa estrutura bem definida), a fim de submetê-los ao repositório para serem ingeridos. Os SIPs criados devem cumprir co m as políticas estabelecidas (ou negociados com) o repositório. O processo de pré-ingestão geralmente compreende algumas ou todas das seguintes atividades:

## Acordo de aquisição

Esta atividade consiste na definição dos termos, pré-condições e requisitos que o Produtor deverá cumprir antes de submeter dados ao repositório. Parte desta atividade consiste na criação de um plano de classificação de base onde o produtor poderá depositar os seus itens de informação. Esta atividade consiste na assinatura de um contrato entre o Produtor e o Repositório. Este contrato especifica o tipo de conteúdo e todos os requisitos legais e técnicos que ambas os outorgantes deverão assegurar.

## Plano de classificação

Durante a assinatura do contrato de submissão, o produtor deve concordar com um esquema de classificação base (ou lista de coleções) no qual terá autorização explícita para depositar novas informações.

O esquema de classificação básico é geralmente criado pelo Repositório e pode ser descarregado nesta seção num formato legível por máquina. O ficheiro descarregado pode ser carregado no [RODA-in](http://rodain.roda-community.org) para uma melhor organização e preparação dos SIPs, antes de transferi-los para o repositório a ser ingerido.

[Descarregar o esquema de classificação](/api/v1/classification_plans) (nota: apenas é possível descarregar o esquema de classificãção a partir do RODA)

[Descarregar esquema de classificação](/api/v2/classification-plans) (nota: descarregar o esquema de classificação requer uma instância do RODA)

## Pacotes de Informação de Submissão (SIP)

Esta atividade consiste na preparação de 1 ou mais Pacotes de Informação de Submissão (SIP) compatíveis com os requisitos técnicos e não técnicos definidos no Acordo de Aquisição. Para facilitar a criação destes pacotes os produtores poderão utilizar a ferramenta RODA-in. 

Esta ferramenta e a sua documentação pode ser descarregada em [http://rodain.roda-community.org](http://rodain.roda-community.org "rodain").

## Transferência de materiais

Esta atividade consiste na transferência dos Pacotes de Informação de Submissão (SIP) do Produtor para o Repositório. Os SIPs serão temporariamente guardados numa área de transferência do lado do servidor para serem posteriormente ingeridos.

O Produtor pode enviar os seus SIPs para o repositório de várias formas. Estas incluem, mas não estão limitadas, às seguintes opções:

### Transferência por HTTP

1. Aceda ao site do repositório e utilize as credenciais fornecidas para iniciar sessão.
2. Vá ao menu Ingestão/Transferência e entre na pasta com o seu nome de utilizador (ou crie a pasta, se necessário).
3. Carregue todos os seus SIPs para a nova pasta.
4. Informe o Repositório de que o material está pronto para ser ingerido.

### Transferência por FTP

1. Ligue-se a [ftp://address] e utilize as credenciais fornecidas pelo Arquivo para se autenticar.
2. Crie uma pasta para acomodar os SIPs que pretende incorporar de uma só vez (opcional).
3. Copiar todos os SIPs para a nova pasta.
4. Informe o Repositório de que o material está pronto para ser ingerido.

### Transferência de suportes externos

1. Guarde os SIPs num suporte externo (por exemplo, CD, pen USB, etc.)
2. Entregue-o na seguinte morada: [Endereço do repositório]

## Processo de ingestão

Após a transferência, os SIPs serão selecionados para ingestão pela equipa do Repositório. O processo de ingestão disponibiliza serviços e funções para aceitar os SIPs dos Produtores e preparar os conteúdos para armazenamento e gestão arquivística.

As funções de ingestão incluem a receção dos SIPs, a realização de controlo de qualidade sobre os SIPs, a geração de um Pacote de Informação Arquivística (AIP) que cumpra as normas de formatação de dados e documentação do Repositório, a extração de Informação Descritiva dos AIPs para inclusão no catálogo do Repositório e a coordenação das atualizações ao Armazenamento Arquivístico e à Gestão de Dados.
