# Documentation guide

To create new topics on the help page, the only thing needed is adding a new field and its link in the `README.md` file like the below example:

```
- (Link text)[The_New_Page.md]
```

The file should be in the folder `[RODA_HOME]/config/theme/documentation`. If not, it is necessary to copy the RODA default `README.md` file to that folder and then edit.

After adding the new entry in the table of contents, a new file should be created (in the same folder as the `README.md`) with the custom documentation text annotated using [Markdown](https://guides.github.com/features/mastering-markdown/).

## Edit other pages

Some HTML pages (or parts of pages) can be customized by changing the respective HTML page at `[RODA_HOME]/config/theme/some_specific_page.html`. For example, the statistics page can be customized by changing the `[RODA_HOME]/config/theme/Statistics.html` file.
