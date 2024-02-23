<div name="top">

# Publishing Plugins

After developing a plugin using the [roda-plugin-template](https://github.com/keeps/roda-plugin-template), you can make 
a pull request to publish it to the [https://market.roda-community.org/](https://market.roda-community.org), so other 
people can find, install and use the plugin.

## Instructions
- Get the source code using the [development guide](./Developers_Guide.md).
- Find the [market](../roda-core/roda-core/src/main/resources/config/market) folder in path `roda-core/roda-core/src/main/resources/config/market` inside the source code
- Find the folder that corresponds to you Marketplace item type e.g.:`plugin`
- Create a folder in with a unique vendor name e.g.: `KEEP_SOLUTIONS`
- Add [the metadata file](#metadata-file) in json format with plugin information
- Create a pull request to the RODA Community git repository

## Validation
The RODA development team will check if the pull request complies with the community rules. 
If it complies, the plugin will be published in [https://market.roda-community.org/](https://market.roda-community.org)

## Metadata file

The metadata file contains the necessary information for the Market and RODA to make the plugin available for other users.

### Requirements
- The file must be in json format
- The file name must be the classname e.g. `org.roda.core.plugins.external.AIPValidatorPlugin.json`
- It must contain the following fields:

| Name          | Description                                                                                                                      |
|---------------|----------------------------------------------------------------------------------------------------------------------------------|
| id            | The plugin  classname                                                                                                            |
| name          | The name that will be displayed in the Market and in the RODA interface                                                          |
| type          | Plugin  type                                                                                                                     |
| version       | Plugin version                                                                                                                   |
| description   | The description that will be displayed in the Market and in the RODA interface                                                   |
| license       | Name of the license and url to license                                                                                           |
| homepage      | A URL to the Marketplace's git repository or a web page with details on how to obtain the item                                   |
| vendor        | The plugin vendor name and link to vendor's homepage                                                                             |
| compatibility | List of RODA versions supported by the plugin                                                                                    |
| price         | Plugin's price                                                                                                                   |
| plugin        | The categories that the plugin fits into,the classname of RODA objects that can be targeted by this plugin and the plugin's type |
| lang          | Languages supported by this plugin                                                                                               |
| region        | Regions supported by this plugin                                                                                                 |


e.g. plugin file org.roda.core.plugins.external.AIPValidatorPlugin.json
```json
{
    "id": "org.roda.core.plugins.external.AIPValidatorPlugin",
    "name": "E-ARK AIP Validator",
    "type": "plugin",
    "version": "1.0",
    "description": "The E-ARK AIP Validator plugin provides a comprehensive evaluation to ensure that AIPs meet the requirements outlined in the E-ARK specification, version 2.0.4.",
    "license": {
        "name": "EULA",
        "url": "http://docs.roda-enterprise.com/plugins/org.roda.core.plugins.external.AIPValidatorPlugin/LICENSE.html"
    },
    "homepage": "http://docs.roda-enterprise.com/plugins/org.roda.core.plugins.external.AIPValidatorPlugin",
    "vendor": {
        "name": "KEEP SOLUTIONS",
        "homepage": "https://keep.pt"
    },
    "compatibility": ["RODA Enterprise 5"],
    "price": "paid",
    "plugin": {
        "objectClasses": [
            "org.roda.core.data.v2.ip.IndexedAIP",
            "org.roda.core.data.v2.ip.AIP"
        ],
        "categories": [
            "validation",
            "eArchiving"
        ],
        "type": "MISC"
    },
    "lang": [],
    "region": [],
}
```
[(Back to top)](#top)
