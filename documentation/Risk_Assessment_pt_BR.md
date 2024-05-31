# Avaliação de riscos

O RODA vem com um sistema de gestão de riscos pré-carregado com mais de 80 riscos de preservação obtidos do [Digital Repository Audit Method Based on Risk Assessment (DRAMBORA)](http://www.repositoryaudit.eu) desenvolvido pelo [Digital Curation Centre (DCC)](http://www.dcc.ac.uk) e pela DigitalPreservationEurope (DPE).

Também incorpora um Registo de Riscos que pode ser gerido a partir do UI e vários plugins de Avaliação de Riscos que atualizam a informação no Registo de Riscos.

## Como avaliar e mitigar riscos de preservação no RODA?

Então, quer começar a fazer processos de avaliação de riscos no seu repositório. Por exemplo, quer começar um processo para converter ficheiros de formatos que já não são suportados(ex. porque apareceu um novo risco de um dado formato de ficheiro não ser suportado no futuro).

Basicamente, você gostaria de ter um fluxo de trabalho para o seguinte cenário hipotético:

1. Criou um SIP que inclui um ficheiro .doc do Word 95
1. Identificou um risco (hipotético) relativamente ao ficheiro .doc do Word 95 (ex. já nenhum software no nosso instituto é capaz de ler esse formato)
1. Como o risco foi identificado, gostaria de iniciar uma conversão de cada ficheiro .doc Word 95 para DOCX e PDF/A

Bem, há várias maneiras de conseguir gerir novos riscos e de começar uma ação de preservação para os mitigar, logo vamos apenas focar-nos em como deveríamos resolver o seu exemplo específico:

Imagine que eu, como um especialista de preservação, sei que o Word 95 é um formato em risco. Eu iria ao registo dos riscos e registaria esse risco, detalhando todas as coisas que sei sobre esse risco em particular e apontando possíveis ações para o mitigar (ex. migrá-lo para um novo formato).

(Outra possibilidade seria usar um plugin que faria automaticamente este tipo de análises, no entanto, de momento não existe tal plugin. Teria de ser desenvolvido.)

Pode então usar a funcionalidade de Procura para localizar todos os ficheiros Word 95 no repositório. Todos os formatos de ficheiros foram identificados durante o processo de ingestão, logo essa tarefa é bastante simples. Eu então usaria o plugin de associação de Riscos disponível para definir estes ficheiros como instâncias do risco recentemente criado. Isto serve como documentação das decisões de preservação criadas pelo especialista de preservação e como fundamentação para o que vamos fazer a seguir - isto é na verdade planeamento de Preservação.

O próximo passo seria migrar os ficheiros. Pode fazer praticamente a mesma coisa de antes, i.e. selecionar todos os ficheiro .doc do Word 95 no menu de Pesquisa, e executar uma ação de preservação nestes para migrá-los, por exemplo, para PDF.

Podem então diminuir o nível de risco porque já não há mais ficheiros de Word 95 no sistema. As incidências podem ser marcadas como "mitigadas".

O que acabei de explicar é o fluxo de trabalho manual, uma vez que não temos atualmente um plugin de deteção de risco de obsolescência de formatos. Mas esse plugin pode muito bem ser desenvolvido. Os passos para a mitigação seriam, neste caso, iniciados logo a partir da interface de gestão de riscos.

No que diz respeito aos plugins de conversão disponíveis, o RODA atualmente suporta os suspeitos do costume (formatos mais convencionais de imagem, vídeo, texto e aúdio). Formatos de nicho irão sempre existir em cada instituição, e nesse caso, serão desenvolvidos plugins específicos.

## Tem uma ideia para um plugin de avaliação de riscos?

Se estiver interessado em desenvolver um novo plugin de Avaliação de Riscos, por favor contacte a equipa do produto para mais informações.
