# Guia de documentação

Adicionar novos tópicas à Ajuda

Para criar novos tópicos de ajuda, é necessário adicionar um novo item no índice de páginas (ficheiro `README_pt_PT.md`) como no exemplo seguinte:

## ```
- (texto da hiperligação)[A_Nova_Pagina.md]
```

O ficheiro deverá estar em `[RODA_HOME]/config/theme/documentation`. Se não existir o ficheiro desejado nesta pasta, é necessário copiar o ficheiro `README_pt_PT.md` existente em `[RODA_HOME]/example-config/theme/documentation` para a pasta `theme` e adicionar o novo item a esse ficheiro conforme indicado anteriormente.

Depois de adicionar um novo item no índice de páginas, um novo ficheiro deverá ser criado (na mesma pasta onde está o `README_pt_PT.md`) com o nome que foi especificado no índice. Esta página de documentação deverá conter texto anotado com [Markdown](https://guides.github.com/features/mastering-markdown/).

Atualizar a informação das páginas estáticas

O mesmo procedimento dever ser seguido para qualquer página de texto estático existente no sistema. Ou seja, deverá copiar-se a página respetiva de `[RODA_HOME]/example-config/theme/` para `[RODA_HOME]/config/theme/` e editar o seu conteúdo na nova localização. 

## A alterações deverão manifestar-se de imediato.

O mesmo procedimento dever ser seguido para qualquer página de texto estático existente no sistema. Ou seja, deverá copiar-se a página respetiva de `[RODA_HOME]/example-config/theme/` para `[RODA_HOME]/config/theme/` e editar o seu conteúdo na nova localização. 

A alterações deverão manifestar-se de imediato.

Some HTML pages (or parts of pages) can be customized by changing the respective HTML page at `[RODA_HOME]/config/theme/some_specific_page.html`. 
