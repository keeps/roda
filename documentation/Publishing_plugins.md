# Publishing plugins

After developing a plugin using the [roda-plugin-template](https://github.com/keeps/roda-plugin-template), you can make 
a pull request to publish it to the [https://market.roda-community.org/](https://market.roda-community.org/), so other 
people can find, install and use the plugin.

## Instructions
- Get the source code using the [development guide](./Developers_Guide.md).
- Find the [market](../roda-core/roda-core/src/main/resources/config/market) folder in path `roda-core/roda-core/src/main/resources/config/market` inside the source code
- Create a folder with a unique vendor name e.g.: `KEEP_SOLUTIONS`
- Add [the metadata file](#metadata-file) in json format with plugin information
- Create a pull request to the RODA Community git repository

## Validation
The RODA development team will check if the pull request complies with the community rules. 
If it complies, the plugin will be published in [https://market.roda-community.org/](https://market.roda-community.org/)

## Metadata file

The metadata file contains the necessary information for the Market and RODA to make the Plugin available for other users.

### Requirements
- The file must be in json format
- The file name must be the classname e.g. `org.roda.core.plugins.external.AIPValidatorPlugin.json`
- It must contain the following fields:

| Name                | Description                                                                                   |
|---------------------|-----------------------------------------------------------------------------------------------|
| id                  | The plugin classname                                                                          |
| name                | The name that will be displayed in the Market and in the RODA interface                       |
| type                | Type of plugin                                                                                |
| version             | The plugin version                                                                            |
| description         | The description that will be displayed in the Market and in the RODA interface                |
| categories          | The categories that the plugin fits into                                                      |
| objectClasses       | The classname of RODA objects that can be targeted by this plugin                             |
| homepage            | A URL to the plugin's git repository or a web page with details on how to obtain the plugin   |
| vendor              | The Plugin vendor name                                                                        |
| minSupportedVersion | Minimum RODA version supported by the Plugin                                                  |
| maxSupportedVersion | Maximum RODA version supported by the Plugin                                                  |

e.g. file org.roda.core.plugins.external.AIPValidatorPlugin.json
```json
{
  "id": "org.roda.core.plugins.external.AIPValidatorPlugin",
  "name": "E-ARK AIP Validator",
  "type": "MISC",
  "version": "1.0",
  "description": "The E-ARK AIP Validator plugin provides a comprehensive evaluation to ensure that AIPs meet the requirements outlined in the E-ARK specification, version 2.0.4.",
  "categories": [
    "validation"
  ],
  "objectClasses": [
    "org.roda.core.data.v2.ip.IndexedAIP",
    "org.roda.core.data.v2.ip.AIP"
  ],
  "homepage": "https://roda-enterprise.keep.pt/plugins/org.roda.core.plugins.external.AIPValidatorPlugin",
  "vendor": "KEEP SOLUTIONS",
  "minSupportedVersion": "4.1.0",
  "maxSupportedVersion": "5.0.0"
}
```

