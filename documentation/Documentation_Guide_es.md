# FAQ

All static text in RODA, which includes `help pages`, `functionality description`, and static `html pages`, are located under the `[RODA_HOME]/example-config/theme/`.

To update the existing content, you should copy the file you want to update from `[RODA_HOME]/example-config/theme/` to `[RODA_HOME]/config/theme/` and edit it in the destination folder.

## Adding new help pages

To add new topics to the help menu, you need to copy the file `[RODA_HOME]/example-config/theme/README.md` (and all of its translation files, e.g. `README_pt_PT.md`) to `[RODA_HOME]/config/theme/documentation`.

Edit the new `README.md` file in order to include a link to the new help topic to be created:

```
- (Link text)[The_New_Topic_Page.md]
```

After adding the new entry to the Table of Contents, a new [Markdown](https://guides.github.com/features/mastering-markdown/) file should be created and placed under the folder `[RODA_HOME]/config/theme/documentation`. The name of the new file should match the one indicated in the Table of Contents (i.e. `The_New_Topic_Page.md` in this example).

## Editing HTML pages

Some HTML pages (or parts of pages) can be customized by changing the respective HTML page at `[RODA_HOME]/config/theme/some_specific_page.html`. 

Page templates exist under `[RODA_HOME]/example-config/theme/`. These should be copied from their original location into `[RODA_HOME]/config/theme/` as explained in the beginning of this article.

For example, the statistics page can be customized by changing the `[RODA_HOME]/config/theme/Statistics.html` file.
