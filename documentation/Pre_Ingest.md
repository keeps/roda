# Pre-ingest

The pre-ingest process depicts the ability of a Producer to create Submission Information Packages (SIP) containing both data and metadata (in a well-defined structure) in order to submit them to the repository for ingest. The SIPs created are expected to comply to the policies established by (or negotiated with) the repository. 

The pre-ingest process usually comprises some or all of the following activities:

## Submission agreement

This activity consists of the definition of the terms, pre-conditions and requirements for content, and accompanying information (e.g. metadata, documentation, contracts, etc.), to be sent to the repository by the Producer. It is materialised in a written agreement between the Producer and the Repository that specifies the type of content and all the legal and technical requirements that both parties are expected to comply.

## Classification plan

During the signing of the submission agreement, the Producer must have agreed to a base Classification Scheme (or list of Collections) on which she will have explicit authorisation to deposit new information.

The base Classification Scheme is usually created by the Repository and can be downloaded in this section in machine readable format. The downloaded file can be loaded into RODA-in to better arrange and prepare Submission Information Packages before transferring them to the repository to be ingested.

[Download classification scheme](/api/v1/classification_plans) (note: downloading the classification scheme requires a RODA instance)

## Submission Information Packages (SIP)

This activity consists of preparing of one or more Submission Information Packages (SIP) according to the technical and non-technical requirements defined on the Submission Agreement. To facilitate the creation of SIPs, Producers may take advantage of the RODA-in tool. 

The tool and its documentation are available at [http://rodain.roda-community.org](http://rodain.roda-community.org).


## Transfer of materials

This activity consists of the transfer of Submission Information Packages (SIP) from the Producer to the Repository. SIPs are temporarily stored on a quarantine area waiting to be processed by the repository.

There are several ways Producers can use to transfer their SIPs to the repository. These include, but are not limited to the following options:

### HTTP transfer

1. Connect to the repository Web site and use the credentials provided in order to log in.
2. Access the menu Ingest/Transfer and enter the folder with your username (or create the folder if necessary).
3. Upload all your SIPs to the new folder.
4. Inform the Repository that the material is ready to be ingested.

### FTP transfer

1. Connect to [ftp://address] and use the credentials provided by the Repository in order to log in.
2. Create a folder to hold the SIPs you which to be part of a single ingest batch (Optional).
3. Copy all the created SIPs to the new folder.
4. Inform the Repository that the material is ready to be ingested.

### External media transfer

1. Save SIPs to an external media (e.g. CD, USB disk, etc.)
2. Deliver it at the following address: [Repository address]

## Ingest process

After transfer, SIPs will be selected for ingest by the Repository staff. The Ingest process provides services and functions to accept SIPs from Producers and prepare the contents for archival storage and management.

Ingest functions include receiving SIPs, performing quality assurance on SIPs, generating an Archival Information Package (AIP) which complies with the Repository's data formatting and documentation standards, extracting Descriptive Information from the AIPs for inclusion in the Repository catalogue, and coordinating updates to Archival Storage and Data Management.

