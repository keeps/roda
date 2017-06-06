# Guia de documentação

Para criar novos tópicos na página de ajuda, é necessário adicionar um novo item no índice de páginas (ficheiro `README_pt_PT.md`) como no exemplo:

```
- (texto da hiperligação)[A_Nova_Pagina.md]
```

O ficheiro deverá estar em `[RODA_HOME]/config/theme/documentation`. Se não, é necessário copiar o ficheiro `README_pt_PT.md` original para essa pasta e adicionar o novo item a esse ficheiro.

Depois de adicionar um novo item no índice de páginas, um novo ficheiro deverá ser criado (na mesma pasta onde está o `README_pt_PT.md`) com o nome que foi especificado no índice. Esta página de documentação deverá conter texto anotado com [Markdown](https://guides.github.com/features/mastering-markdown/).
