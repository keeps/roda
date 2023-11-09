<div align="left" name="top">
  
<picture>
  <!-- {% comment %} --> 
  <source media="(prefers-color-scheme: dark)" srcset="https://github-production-user-asset-6210df.s3.amazonaws.com/631728/248834537-254df003-ff11-4f33-8d20-65b6da12126a.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://github-production-user-asset-6210df.s3.amazonaws.com/631728/248834502-9c7e7fc7-c94b-4bbe-8c9c-961b5197c411.svg">
  <!-- {% endcomment %}-->
  <img src="https://github-production-user-asset-6210df.s3.amazonaws.com/631728/248834502-9c7e7fc7-c94b-4bbe-8c9c-961b5197c411.svg" height="150">
</picture>

  <br><br>
</div>


[![GitHub latest release](https://img.shields.io/github/v/release/keeps/roda?sort=semver&color=informational)](https://github.com/keeps/roda/releases/latest)
![Docker Pulls](https://img.shields.io/docker/pulls/keeps/roda)
[![GitHub contributors](https://img.shields.io/github/contributors/keeps/roda)](https://github.com/keeps/roda/graphs/contributors)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/m/keeps/roda)
<!-- {% comment %} --> 
[![testing](https://github.com/keeps/roda/actions/workflows/CI.yml/badge.svg)](https://github.com/keeps/roda/actions/workflows/CI.yml?label=testing) 
<!-- {% endcomment %}-->

RODA (Repository of Authentic Digital Records) is a **long-term digital repository** solution that delivers functionalities for all the main functional units of the OAIS reference model. RODA is capable of ingesting, managing, and providing access to various types of digital content produced by large corporations and public bodies.

<div align="center" style="border: 1px solid rgba(50,50,50,.1); border-radius: 2px; padding: 5px; margin-top: 20px; margin-bottom: 15px">
    <a href="#rocket-features">Features</a> &nbsp;&bull;&nbsp;
    <a href="#desktop_computer-trying-out-roda">Trying out RODA</a> &nbsp;&bull;&nbsp;
    <a href="#books-documentation">Documentation</a> &nbsp;&bull;&nbsp;
    <a href="#art-editions">Editions</a> &nbsp;&bull;&nbsp;
    <a href="#shopping-marketplace">Marketplace</a> &nbsp;&bull;&nbsp;
    <a href="#bellhop_bell-support">Support</a> &nbsp;&bull;&nbsp;
    <a href="#writing_hand-contributing">Contributing</a>
</div>

# :nerd_face: Overview

RODA was developed using **open-source technologies** and it is supported by standards such as the Open Archival Information System (OAIS), Metadata Encoding and Transmission Standard (METS), Encoded Archival Description (EAD), Dublin Core (DC), [E-ARK Information Package specifications](https://dilcis.eu/) and PREMIS (Preservation Metadata).

It implements **multiple ingest workflows** that not only validate Submission Information Packages (SIP), but also checks their content for virus, handle file format identification, extract technical metadata, and migrate file formats to more “preservable” alternatives.

RODA enables users to access digital information in several forms, such as searching and browsing via an **online catalogue**, as well as providing **REST APIs** for systems integration. Discovery services are provided over both descriptive metadata and textual content (automatically extracted from an array of document-based formats). Online consultation of ingested objects, preservation formats and dissemination derivatives are also possible via the repository catalogue.

Administration interfaces allow repository managers to maintain **representation information** updated, **manage risks** and to execute **preservation actions** on all digital assets available in the repository.

RODA ensures that **ingested data remains authentic** by recording PREMIS metadata for every action performed on a digital object. It records provenance information in archival metadata standards such as EAD or Dublin Core and **ensures integrity and availability** by frequently monitoring data and making sure that it has not been tampered with. All interactions between users and the repository (human and software) are logged for **security and accountability** reasons.

To experience RODA firsthand, please visit the **RODA demo site** at :link: [https://demo.roda-community.org](https://demo.roda-community.org).

# :rocket: Features

## Conforms to open standards

RODA is compliant with several open descriptive metadata standards such as EAD, and Dublin Core, PREMIS for preservation metadata and METS for structural metadata.

It can support more standards by means of an advanced templating system that supports validation, indexing, viewing, and editing of descriptive metadata.

SIP, AIP and DIP formats are also based on open specifications managed by the DILCIS Board to avoid technology lock-in (i.e., EARK Information Packages).

## Vendor independent

RODA is built on top of enduring open-source technologies. The entire infrastructure required to support RODA is also vendor independent. This means that you may use the hardware and the Linux distribution that best fit your institutional needs.

Because the product itself is open source, you don’t have to rely on a single vendor for support. The entire source code of the system is available on GitHub for inspection.

## Authenticity

RODA uses PREMIS preservation metadata to create a trust chain between all generations of digital assets.

Preservation metadata, together with the establishment of trust of its surrounding environment (ISO 16363 and ISO 14721) ensures that the repository service is reliable, and digital assets remain authentic over time.

RODA also supports plugins that assess the validity of digital signatures and re-sign archived PDFs when the lifetime of digital signatures is coming to an end.

## Support for multiple formats

RODA can ingest all sorts of content independent of its format. Format migration plugins are available to cope with decaying formats such as text documents, raster images, relational databases, video, and audio.

Normalization of file formats is possible by setting up adequate ingest workflows. Files are then preserved in formats more adequate for long-term archiving.

Representation information networks can be managed within the repository itself, letting you opt for the right preservation strategy at the right time.

## Advanced ingest workflows

RODA supports the ingest of digital material as well as any associated metadata in several distinct formats.

Tools and libraries are provided to enable Producers to create packages in the supported Submission Information Package formats (SIP). Ingest workflows can be customised by the user to implement institutional policies and handle special collections of data. Ingest workflows can be enhanced via the use of open source or commercial plugins.

## Embedded preservation actions

Preservation actions can be executed right from the user interface over any selection of digital objects in the repository.

The task execution engine enables the repository to parallelise the task execution process to take full advantage of the existing CPUs.

Preservation actions include format conversions, checksum verifications, virus checks, various, risk assessment, etc.

## Scalable

RODA is ideal for large organisations with millions of digital objects. It’s service-oriented nature allows it to be highly scalable, enabling the distribution of load between several servers.

The use of horizontally scalable indexing services enables discovery services to be spread across multiple servers for greater performance.

RODA also takes advantage of all the CPUs available in each server to mass process thousands of objects simultaneously for stellar performance.

## Copes with the rapid changing nature of technology

The pluggable architecture of RODA makes it easy to add more functionality to the system without affecting its core functionality.

This includes adding new preservation capabilities such as format converters, risk assessment tools, and more.

The system stores data in a well-documented open AIP format that can be easily inspected by users and processed by third-party repository systems.

## Retention and disposal policies

RODA incorporates robust and comprehensive functionality for the definition and implementation of retention and disposal policies.

This feature empowers users to establish clear and concise guidelines for determining how long records need to be kept and how they should be disposed of, in accordance with national legislation or internal regulations.

With RODA's retention and disposal policies, organizations can confidently preserve their valuable digital assets for the long-term while mitigating risks associated with data privacy and security.

## Integration with data production systems

RODA exposes all its functionality via well-documented REST API. Convenient Java libraries are available on GitHub to allow developers interact with RODA via its Core APIs. Several tools exist to create and manipulate SIPs and submit them to RODA’s for ingest.

In fact, RODA’s ability to integrate data from other records management systems include: 1) packaging data available on a filesystem via RODA-in tool, 2) extract data from relational databases using DBPTK, and 3) custom integrations with original systems via APIs or using drop folders.

[(Back to top)](#top)

# :desktop_computer: Trying out RODA

## Online demo

To experience RODA without having to install it on your local machine, please visit the RODA online demo available at :link: [https://demo.roda-community.org](https://demo.roda-community.org).

## Testing environment

To install RODA on your local environment, please refer to our detailed installation guide, available at [Installation instructions](deploys/README.md).

We want to draw your attention the fact that these instructions are tailored for a single-node instance, specifically intended for testing purposes. It is essential to note that this configuration is not designed for production use.

We recommend exploring available commercial services, which include installation, maintenance & support, training, data migration, among others. This ensures that your instance of RODA is fully optimized and tailored to your specific needs.

## Production environment

Each production environment is unique, and therefore requires a customized approach. RODA offers the flexibility to be tailored to the specific needs of each digital preservation service.

Considerations such as data volume, quantity of files, format heterogeneity, infrastructure type, target user community, high availability requirements and system integration need to be taken into account while designing the right architecture for your particular problem.

Should you require assistance in preparing RODA for production environments, we recommend seeking professional assistance. Vendors may provide services such as installation, maintenance, support, training, data migration, system integration, custom development, digital preservation consultancy, among others.

To obtain more information on how to get professional assistance in your region, please contact [KEEP SOLUTIONS](https://www.keep.pt/en/contacts-proposals-information-telephone-address).

[(Back to top)](#top)

# :books: Documentation

RODA is accompanied by a comprehensive collection of articles that are constantly being updated. You may find the most recent version of the documentation [here](documentation/README.md).

All articles included in the documentation are written in [Markdown](https://www.markdownguide.org/), providing you with the convenience of easy conversion to a wide range of formats including PDF, ePub, HTML, and more.

[(Back to top)](#top)

# :art: Editions

RODA is an open-source solution, which means that anyone can download its source code, compile it and have it running on their own institution in a matter of hours. While this flexibility is a major advantage, organizations may also benefit from enlisting the expertise of IT, software, and digital preservation specialists to tailor the solution to their unique needs by adding specialized features and integrations.

[KEEP SOLUTIONS](https://www.keep.pt), the maintainer of the RODA open-source project, offers three software distributions specially designed to meet the rigorous demands of mid to large-sized institutions with vast collections of digital records, providing the capability to efficiently manage digital assets in large-scale production environments. These software suites are called “Community”, “Enterprise” and “Enterprise HA”.

## Community Edition

The RODA Community Edition is a software solution designed for mid-sized organizations seeking to implement and customize the product to meet their specific requirements.

As its name suggests, this edition is community-driven and provides organizations the necessary tools to manage the product on their own.

However, it should be noted that the RODA Community Edition does not include commercial plugins, integrations, or professional support services. Those products and services must be acquired separately.

For more information about how to acquire commercial services from KEEP SOLUTIONS, please visit [KEEP SOLUTIONS](https://www.keep.pt) website.


## Enterprise Edition

The Enterprise Edition is designed for large-scale production environments and provides a comprehensive suite of tools and services for organizations seeking a secure and high-performance preservation solution.

This edition includes a set of commercial plugins and professional support services to assist organisations with deployment and ongoing operations to ensure optimal performance, stability and security.

For more information about Enterprise Editions, please visit the [RODA Enterprise](https://www.roda-enterprise.com/) web page.

## Enterprise HA Edition

For organizations requiring uninterrupted access to their digital assets and the ability to scale their system without causing downtime, the RODA Enterprise HA Edition provides a high-availability distributed setup.

This offering includes unlimited scalability and the ability to upgrade the system with minimal disruption, ensuring organizations always maintain seamless access to their critical digital assets.

For more information about Enterprise Editions, please visit the [RODA Enterprise](https://www.roda-enterprise.com/) web page.

## Enterprise SM Edition

For organizations seeking comprehensive control over their infrastructure and operations, while simultaneously harnessing the benefits of warranty, security patches, commercial plugins, components, and professional services. 

Under this model, the organization's IT department assumes the critical responsibility for implementing, monitoring, and maintaining the system. [Shadowing support](https://docs.roda-enterprise.com/services/05-shadowing-support/) can be enlisted to assist with maintenance and support tasks. Moreover, for additional features, custom development services can be engaged, ensuring that the system continues to evolve to meet the ever-changing demands of the business. 

By minimizing reliance on third-party providers for installation, configuration, and ongoing support, this solution effectively preserves data security and system integrity.

For more information about Enterprise Editions, please visit the [RODA Enterprise](https://www.roda-enterprise.com/) web page.

[(Back to top)](#top)

# :shopping: Marketplace

RODA 5 introduced the [RODA Marketplace](https://market.roda-community.org), a digital platform where users can browse and purchase RODA plugins, components and services. It is a centralized location where developers can showcase their products, and users can easily discover and acquire the software or services they need.

In the Marketplace, developers can [publish their plugins](./documentation/Publishing_plugins.md), components and services, providing information such as descriptions, pricing, and links to more documentation. Users can then search for these products based on criteria such as functionality, category, and price range. Once they find a product they are interested in, they can purchase it from its original developer.

The marketplace is beneficial for both developers and users. For developers, they offer a centralized location to reach a wide audience, which can help increase their visibility and sales. For users, the marketplace provides a convenient way to discover and purchase useful extensions for their RODA environments.

For more information on available RODA extensions, please visit [RODA Marketplace](https://market.roda-community.org/).

[(Back to top)](#top)

# :bellhop_bell: Support

## Community support

RODA is a community-driven project that relies on the contributions of various people to continually improve and evolve. As a user, you may encounter issues or have questions about how to use RODA effectively. Fortunately, the RODA project has a community-driven support system in place.

The way to get community-driven support is by posting your questions on the [RODA GitHub Discussions](https://github.com/keeps/roda/discussions) board and your bug reports on the [RODA GitHub issues](https://github.com/keeps/roda/issues) page.

GitHub is a popular web-based platform used for version control and collaboration on software development projects. The RODA GitHub repository is where the project is hosted, and it's also where you can find helpful resources and get support from other users and developers. To get started, simply create a GitHub account and navigate to the [RODA repository](https://github.com/keeps/roda). From there, you can browse the issues section or create a new issue to post your question on the Discussion board.

Posting your question on GitHub allows other community members to see it, and they may offer solutions, insights, or workarounds to help you resolve your issue. Additionally, the RODA developers and maintainers actively monitor the GitHub repository, and they may also chime in with advice or solutions.

For information about the maintenance and security policy, view security advisories, or report vulnerabilities, check out the [RODA Community Security page](https://github.com/keeps/roda/security).

## Professional support

Professional support services ensure the smooth operation of RODA in production environments. These services include helpdesk and support, service level agreements (SLA), ticketing platform for better communication, access to security patches, monitoring, and access statistics.

These services ensure organizations have access to the resources and support they need to effectively manage and maintain a production level RODA environment, maximizing investment and ensuring the success of digital preservation operations.

To obtain more information on how to get professional support in your region, please contact [KEEP SOLUTIONS](https://www.keep.pt/en/contacts-proposals-information-telephone-address).

[(Back to top)](#top)

# :writing_hand: Contributing

Community contributions play a vital role in the success of open source projects. They bring diverse perspectives, knowledge, and skills to the table, which can lead to innovative solutions and better outcomes.

By contributing code, translations, documentation, testing, bug fixes, and feedback, community members help to ensure the software is reliable, secure, and up-to-date.

## Source-code

Developers can contribute to RODA in many ways: fixing bugs, adding new features, developing new plugins, adding tests, etc.

To start developing new features in RODA, please check the [Developer guide](documentation/Developers_Guide.md) which has information on:

- How to get the source code
- How to build and run
- How to set up the development environment
- Code structure
- How to contribute
- etc.

## Translations

RODA is translated in several languages such as English :uk:, Portuguese :portugal:, Swedish :sweden:, Hungarian :hungary:, Spanish :es:, Croatian :croatia:, German (Austria) :austria:, etc. Translations are maintained in [Transifex](https://explore.transifex.com/roda-1/roda2/) and updated using the [Transifex Client](http://docs.transifex.com/client/).

Transifex is a cloud-based localization management platform that facilitates the translation and localization of software applications, websites, and digital content. It allows for the coordination of multilingual content creation and translation by providing a collaborative workspace for translators, project managers, and developers.

For more information, check our [Translation guide](documentation/Translation_Guide.md).

[(Back to top)](#top)
