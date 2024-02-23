<div name="top">

# Publishing Components

After developing a component using the [roda-component-template](), you can make 
a pull request to publish it to the [https://market.roda-community.org/](https://market.roda-community.org/), so other 
people can find, install and use the component.

## Instructions
- Get the source code using the [development guide](./Developers_Guide.md).
- Find the [market](../roda-core/roda-core/src/main/resources/config/market) folder in path `roda-core/roda-core/src/main/resources/config/market` inside the source code
- Find the folder that corresponds to you Marketplace item type e.g.:`components`
- Create a folder in with a unique vendor name e.g.: `KEEP_SOLUTIONS`
- Add [the metadata file](#metadata-file) in json format with component information
- Create a pull request to the RODA Community git repository

## Validation
The RODA development team will check if the pull request complies with the community rules. 
If it complies, the component will be published in [https://market.roda-community.org/](https://market.roda-community.org/)

## Metadata file

The metadata file contains the necessary information for the Market and RODA to make the component available for other users.

### Requirements
- The file must be in json format
- The file name must be the classname e.g. `dropFolders.json`
- It must contain the following fields:

| Name          | Description                                                                                    |
|---------------|------------------------------------------------------------------------------------------------|
| id            | The component  classname                                                                       |
| name          | The name that will be displayed in the Market and in the RODA interface                        |
| type          | Component  type                                                                                |
| version       | Component version                                                                              |
| description   | The description that will be displayed in the Market and in the RODA interface                 |
| license       | Name of the license and url to license                                                         |
| homepage      | A URL to the Marketplace's git repository or a web page with details on how to obtain the item |
| vendor        | The component vendor name and link to vendor's homepage                                        |
| compatibility | List of RODA versions supported by the component                                               |
| price         | component's price                                                                              |
| plugin        | Leave this attribute as shown below                                                            |
| lang          | Languages supported by this component                                                          |
| region        | Regions supported by this component                                                            |


e.g. component file dropFolders.json
```json
{
    "id": "dropFolders",
    "name": "Drop folders",
    "type": "component",
    "version": "",
    "description": "Facilitates the automated, unsupervised ingestion of submission information packages via shared folders, which is crucial for a smooth integration with other data production systems.",
    "license": {
        "name": "",
        "homepage": "",
    },
    "homepage": "http://docs.roda-enterprise.com/components/drop-folders",
    "vendor": {
      "name": "KEEP SOLUTIONS",
      "homepage": "https://keep.pt"
    },
    "compatibility": ["RODA Enterprise 5"],
    "price": "paid",
    "plugin": {
      "objectClasses": [],
      "categories": [],
      "type": "",
    },
    "lang": [],
    "region": [],
  },
```
[(Back to top)](#top)
