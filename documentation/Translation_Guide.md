# Translation guide

There is a new easier way to translate RODA to another language, with no need for technical skills, using Transifex. To check how to translate RODA using just source code please refer to the section "Translate using source code" below.

## Translate using Transifex

To start, go the site [https://www.transifex.com/roda-1/roda2/](https://www.transifex.com/roda-1/roda2/) and click the button **"Help translate RODA"**. 

If you don't have an account create one or select one of the easy sign-up options. After log-in, add yourself to the RODA Community team and start translating RODA into one of the languages already being worked on. 

If your language is not on the list, click the button "Request language" for us to evaluate adding this new language to RODA.


## Translate using source code

To translate RODA to a new language one needs to get the source files (as explained in the [Developer guide](Developers_Guide.md)) and add new message files for the new language.
The existing message files containing localizable texts are listed below on RODA-WUI project (roda-ui/roda-wui).
 
* src/main/resources/config/i18n/*.properties
* src/main/resources/config/i18n/client/*.properties
* src/main/resources/config/theme/*.html (these files, depending on the locale, may have a suffix in name like _Welcome_pt_PT.html_)

When changing client properties, it is necessary to [recompile the project](Developers_Guide.md) to update them.

### Add a new language

To add a new language (French, for example) to RODA-IN, one needs create a new text file  **src/main/resources/messages_fr.properties** or copy one of the existing ones in the same directory (like messages_en.properties) and replace the English sentences and words by the French versions, like in the following example.

Extract from English version of **src/main/resources/messages_en.properties** 

```properties
...
MainPanel.action.UPDATE=Update plan
MainPanel.action.CREATE_SIP=Create
MainPanel.error.CREATE_SIP=An error occurred while creating package: %1$s
...
```

Extract from French version **src/main/resources/messages_fr.properties** 

```properties
...
MainPanel.action.UPDATE=Actualiser le plan
MainPanel.action.CREATE_SIP=Créer
MainPanel.error.CREATE_SIP=Une erreur s'est produite lors de la création de package: %1$s
...
```

#### RODA-WUI

To add a new language (French, for example) to RODA-WUI, besides creating new message files like explained before, one needs to do the following steps for the new language appears in the interface:

1. Edit file [src/main/java/org/roda/wui/RodaWUI.gwt.xml](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/java/org/roda/wui/RodaWUI.gwt.xml) and change it accordingly to the instructions in [GWT guide](https://developers.google.com/web-toolkit/doc/latest/DevGuideI18nLocale#LocaleModule).

2. [Recompile the project](Developers_Guide.md) and you are ready to go.
