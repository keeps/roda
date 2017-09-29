# Documentation guide

All static texts in RODA, which include `help pages`, `functionality description`, and static `html pages` are located under the `[RODA_HOME]/example-config/theme/`.

To update the exiting content, you should copy the file you want to update from `[RODA_HOME]/example-config/theme/` to `[RODA_HOME]/config/theme/` and edit it on the destination folder.

## Adding new help pages

To add new topics to the help menu, you need to copy the file `[RODA_HOME]/example-config/theme/README.md` (and all of its translation files, e.g. `README_pt_PT.md`) to `[RODA_HOME]/config/theme/documentation`.

Edit the new `README.md` file in order to include a link to the new help topic to be created:

```
- (Link text)[The_New_Topic_Page.md]
```

After adding the new entry to the Table of contents, a new [Markdown](https://guides.github.com/features/mastering-markdown/) file should be created and placed under the folder `[RODA_HOME]/config/theme/documentation`. The name of the new file should match the one indicated on the Table of Contents file (i.e. `The_New_Topic_Page.md` in the example).

## Edit html pages

Some HTML pages (or parts of pages) can be customized by changing the respective HTML page at `[RODA_HOME]/config/theme/some_specific_page.html`. 

Page templates exist under `[RODA_HOME]/example-config/theme/`. These should be copied from its original location into `[RODA_HOME]/config/theme/` as explained in the beginning of this article.

For example, the statistics page can be customized by changing the `[RODA_HOME]/config/theme/Statistics.html` file.


