# Pre-ingest

The **pre-ingest** phase refers to the preparation activities carried out by a _Producer_ before submitting digital materials to a repository. Its main goal is to ensure that **Submission Information Packages (SIPs)** are created in accordance with the repository's requirements, thereby facilitating a smooth and reliable ingest process.

This phase typically includes the following activities:

## Submission Agreement

The **Submission Agreement** defines the formal relationship between the _Producer_ and the _Repository_. It establishes:

- The types of content to be submitted;
- The legal, technical, and procedural requirements;
- The responsibilities of each party;
- The expected structure and format of the SIPs.

This agreement is typically documented in writing and must be accepted by both parties before content can be submitted.

## Content Organisation

As part of the submission agreement process, the _Producer_ is usually authorised to deposit content into specific areas of the repository, based on a **classification plan** (e.g. a hierarchical structure used to organise content within the repository).

The repository provides a base **Classification Scheme** in machine-readable format, which helps Producers prepare their SIPs in line with the repository's internal organisation.

This file can be downloaded and imported into **RODA-in**, a tool designed to assist in the preparation and structuring of SIPs.

📎 [Download classification scheme](/api/v2/classification-plans)

Óptimo. Aqui está a secção revista e integrada no texto anterior, com linguagem clara, alinhada com o OAIS, e actualizada com boas práticas actuais. Inclui agora uma nova subseção sobre ferramentas de linha de comandos e bibliotecas para criação de SIPs:

## SIP Preparation

The _Producer_ is responsible for creating one or more **Submission Information Packages (SIPs)**, in accordance with the specifications set out in the submission agreement. Each SIP must:

- Contain all required content and associated metadata;
- Follow the structural and technical guidelines agreed upon;
- Be packaged in a format accepted by the repository (e.g. E-ARK, BagIt).

To support this task, different tools are available depending on the Producer’s workflow preferences:

### RODA-in

For users preferring a graphical interface, **[RODA-in](http://rodain.roda-community.org)** provides a user-friendly way to:

- Organise content and metadata;
- Assign collections and categories;
- Validate package structure;
- Export SIPs in the appropriate format.

### Command-Line Tools and Libraries

For automated or large-scale workflows, several open-source tools and libraries are available to prepare SIPs via command-line or scripting environments:

- **[Commons-IP](https://github.com/keeps/commons-ip)** - A Java-based command-line tool and library for creating, validating, and converting OAIS Information Packages. Supports multiple packaging formats including E-ARK (v1, v2.0.4, v2.1.0, v2.2.0), BagIt, and Hungarian type 4 SIP.

- **[.NET E-ARK SIP](https://igfej-justica-gov-pt.github.io/dotnet-eark-sip/)** - A CLI tool and .NET library to generate E-ARK compliant SIPs. Ideal for integration with Microsoft-based environments.

- **[eArchiving Tool Box (EATB)](https://github.com/E-ARK-Software/eatb)** - A collection of Python-based tools developed under the E-ARK project for the creation of SIPs and other information packages, supporting scripting and batch workflows.

> 🛠️ These tools are recommended for institutions with high volumes of content or complex automation needs.

## Transfer of Materials

Once SIPs are prepared, they must be transferred to the repository. SIPs are first placed in a **quarantine area** where they await validation and processing by the repository.

There are several supported transfer methods:

### HTTP Transfer

1. Log into the repository's web interface using your credentials.
2. Navigate to **Ingest > Transfer** and access your personal folder (create one if necessary).
3. Upload your SIPs.
4. Notify the repository staff that the material is ready for ingest.

### FTP Transfer

1. Connect to the provided FTP server using your credentials.
2. Optionally create a folder for your ingest batch.
3. Upload your SIPs.
4. Notify the repository that the SIPs are available.

### Physical Media Transfer

1. Save the SIPs to a physical medium (e.g. USB drive, external hard disk).
2. Deliver it to the following address:
   `[Repository address]`

> ⚠️ Ensure media is labelled clearly and that its integrity is verified before delivery.

## Ingest Process

After transfer, the repository will initiate the **Ingest** process. This process includes the following steps:

- **Receipt and validation** of SIPs;
- **Quality assurance** to ensure compliance with format and metadata requirements;
- **Generation of Archival Information Packages (AIPs)** for long-term preservation;
- **Extraction of Descriptive Information** for indexing and search in the repository's catalogue;
- **Update of Archival Storage and Data Management** systems.

The ingest process ensures that all content is correctly archived, discoverable, and preserved according to established policies.

## Notes

- The SIP must not include content or metadata outside the agreed scope.
- The repository may reject SIPs that fail validation during ingest.
- An email may be issued to notify Producers and repository staff of any ingest process.
- Contact repository staff for support with RODA-in or transfer issues.
