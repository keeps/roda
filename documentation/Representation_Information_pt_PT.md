# Informação de Representação



*O conteúdo aqui apresentado é uma cópia integral do artigo denominado "OAIS 7: Representation Information" publicado em [Blog Alan's Notes on Digital Preservation](https://alanake.wordpress.com/2008/01/24/oais-7-representation-information/ ).*


A Informação de Representação é um conceito crucial, uma vez que é através da Informação de Representação que um dado objeto consegue ser compreendido, aberto e visualizado. A própria Informação de Representação apenas pode ser interpretada se existir uma base de conhecimento adequada.

O conceito de Informação de Representação também está inextricavelmente ligado ao conceito de Comunidade Designada, porque a maneira como definimos a Comunidade Designada (e a sua Base de Conhecimento associada) determina a quantidade de Informação de Representação que é necessária. "O OAIS deve compreender a Base de Conhecimento da sua Comunidade Designada para compreender a Informação de Representação mínima que deve ser mantida... Ao longo do tempo, a evolução da Base de Conhecimento da Comunidade Designada pode necessitar de atualizações à Informação de Representação para garantir a compreensão contínua" (2.2.1).


O próprio Objeto de Dados, num repositório digital, é simplesmente uma sequência de bits. O que a Informação de Representação faz é converter (ou dizer-nos como converter) estes bits em algo mais significativo. Descreve os conceitos de formato ou de estrutura dos dados que devem ser aplicados às sequências de bits que, por sua vez, resultam em valores mais significativos, como caracteres, pixéis, tabelas, etc.

Isto é denominado de **informação de estrutura**. Idealmente, a Informação de Representação também deve conter **informação semântica**, por exemplo, em que língua humana está escrito o texto, o que qualquer terminologia científica significa, e assim por diante (4.2.1.3.1). Ao incluir tanto a estrutura como a informação semântica, estamos a preparar-nos o melhor possível para o futuro.

A preservação da IR é mais facilmente realizada quando a Informação de Representação é expressa de uma forma facilmente compreensível, "como ASCII" (4.2.1.3.2). O que o Modelo está a dizer aqui é que seria estúpido guardar a Informação de Representação num formato de ficheiros exclusivo ou com fraco suporte, ainda que o próprio Objeto de Dados esteja em tal formato. A Informação de Representação pode ser impressa em papel, se isso ajudar.

## Qual é o mínimo que qualquer Informação de Representação precisa de atingir?

A Informação de Representação dever permitir ou possibilitar a recriação de propriedades significativas do objeto de dados original. Essencialmente, isto significa que a Informação de Representação deve ser capaz de recriar uma cópia do original.

## Redes de representação

A Informação de Representação pode conter referências para outra Informação de Representação. E como a própria Informação de Representação é um Objeto de Informação, com o seu próprio Objeto de Dados e a Informação de Representação relacionada, pode ser desenvolvida uma rede inteira de Informação de Representação. Isto é chamado de rede de Representação (4.3.1.3.2). Por exemplo, a Informação de Representação para um objeto pode simplesmente afirmar que um registo está em ASCII. Dependendo de como definirmos a nossa Comunidade Designada, poderemos ter de contribuir com Informação de Representação adicional, como o que a norma ASCII é na verdade.

## Informação de Representação na ingestão

Um SIP pode aparecer com Informação de Representação muito pobre - talvez apenas um ou dois manuais impressos ou alguns PDFs na pasta da documentação (ver a especificação E-ARK SIP).

O OAIS precisa de muito mais. Mas isto não deve impedir um OAIS de aceitar algo. É possível ser demasiado técnico em relação à Rede de Representação. Só porque um SIP chegou com apenas 4 dos 700 campos de metadados obrigatórios não é uma razão válida o suficiente para o rejeitar, se for um registo de valor permanente.

## Software de representação

A Informação de Representação pode ser **software executável**, se isso for útil. O exemplo dado no Modelo (4.2.1.3.2) é onde a Informação de Representação existe como um ficheiro PDF. Em vez de ter Informação de Representação adicional que define o que é um PDF, é mais útil simplesmente usar um visualizador de PDF.

No entanto, o OAIS necessita de monitorar isto cuidadosamente porque um dia deixarão de existir os visualizadores de PDF, e a Informação de Representação original precisaria então de ser migrada para uma nova forma.

## Software de acesso e emulação

A curto prazo, a maioria dos registos são provavelmente abertos por exatamente o mesmo software no qual foram criados. Alguém que abra um documento arquivado, mas que foi recentemente criado, irá provavelmente usar a sua própria app MS Word. Então isto leva à possibilidade de as redes de Representação estendidas poderem ser abandonadas, e nós simplesmente usarmos o software original, ou algo muito semelhante.
O OAIS chama a isto Software de Acesso, e alerta contra o mesmo, porque significa que temos de tentar manter o software a funcionar. No entanto, apercebo-me que isto é o principal objetivo da emulação HW. Se tiver a única frase "documento MS Word" como a sua Informação de Representação, e mantiver uma cópia de trabalho da app do Word em HW e OS emulados, então não precisa de nenhuma rede de Representação. Pelo menos, até tudo dar para o torto.

Informação de Representação na prática

## Peguemos num ficheiro de imagem JPEG como exemplo. Penso que podemos partir do princípio de que a nossa Comunidade Designada atual compreende o que é um JPEG: na terminologia do OAIS, a Base de Conhecimento da Comunidade Designada inclui o conceito e o termo "JPEG". Logo, a nossa Informação de Representação para esse ficheiro poderia, em teoria, simplesmente ser uma afirmação a dizer que é uma imagem JPEG. Isso é suficiente. Para o formato JPEG, e para muitos outros, a Informação de Representação mais útil é na verdade uma app que pode visualizá-la e abri-la. (Qualquer PC atual será capaz de abrir o JPEG de qualquer das maneiras.)

No entanto, a longo prazo, temos de nos preparar para um mundo no qual a Comunidade Designada se afastou dos JPEGs. Logo, também devemos adicionar uma ligação ao website da norma JPEG, para explicar o que é um JPEG. E poderíamos incluir informação sobre quais apps de software podem abrir uma imagem JPEG. Adicionalmente, deveria existir uma ligação na rede de Representação para um lugar na web onde podemos descarregar um visualizador de JPEG.

Isto significa que a rede de Representação muda ao longo do tempo. Temos de ser capazes de atualizá-la de acordo com o desenvolvimento da tecnologia.

Isto significa que a rede de Representação muda ao longo do tempo. Temos de ser capazes de atualizá-la de acordo com o desenvolvimento da tecnologia.
