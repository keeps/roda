<div name="top">

# Publishing Services

After developing a service using the [roda-service-template](), you can make 
a pull request to publish it to the [https://market.roda-community.org/](https://market.roda-community.org/), so other 
people can find, install and use the service.

## Instructions
- Get the source code using the [development guide](./Developers_Guide.md).
- Find the [market](../roda-core/roda-core/src/main/resources/config/market) folder in path `roda-core/roda-core/src/main/resources/config/market` inside the source code
- Find the folder that corresponds to you Marketplace item type e.g.:`services`
- Create a folder in with a unique vendor name e.g.: `KEEP_SOLUTIONS`
- Add [the metadata file](#metadata-file) in json format with service information
- Create a pull request to the RODA Community git repository

## Validation
The RODA development team will check if the pull request complies with the community rules. 
If it complies, the service will be published in [https://market.roda-community.org/](https://market.roda-community.org/)

## Metadata file

The metadata file contains the necessary information for the Market and RODA to make the service available for other users.

### Requirements
- The file must be in json format
- The file name must be the classname e.g. `customDevelopment.json`
- It must contain the following fields:

| Name          | Description                                                                                    |
|---------------|------------------------------------------------------------------------------------------------|
| id            | The service  classname                                                                         |
| name          | The name that will be displayed in the Market and in the RODA interface                        |
| type          | Service  type                                                                                  |
| version       | Service version                                                                                |
| description   | The description that will be displayed in the Market and in the RODA interface                 |
| license       | Name of the license and url to license                                                         |
| homepage      | A URL to the Marketplace's git repository or a web page with details on how to obtain the item |
| vendor        | The service vendor name and link to vendor's homepage                                          |
| compatibility | List of RODA versions supported by the service                                                 |
| price         | Service's price                                                                                |
| plugin        | Leave this attribute as shown below                                                            |
| lang          | Languages supported by this service                                                            |
| region        | Regions supported by this service                                                              |

e.g. service file customDevelopment.json
```json
{
    "id": "customDevelopment",
    "name": "Custom development",
    "type": "service",
    "version": "",
    "description": "This service is designed to provide organizations with the flexibility and control they need to achieve their business objectives by creating new Services or integrations that are tailored to their specific requirements. The service covers the full software development life cycle, from initial design and planning to coding, testing, and implementation.",
    "license": {
        "name": "",
        "homepage": "",
    },
    "homepage": "http://docs.roda-enterprise.com/services/custom-development",
    "vendor": {
      "name": "KEEP SOLUTIONS",
      "homepage": "https://keep.pt"
    },
    "compatibility": ["RODA Community 4, RODA Community 5, RODA Enterprise 5"],
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