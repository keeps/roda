# Risk assessment

RODA comes with a Risk Registry pre-loaded with 80+ preservation risks obtained from the [Digital Repository Audit Method Based on Risk Assessment (DRAMBORA)](http://www.repositoryaudit.eu) toolkit developed by the [Digital Curation Centre (DCC)](http://www.dcc.ac.uk) and DigitalPreservationEurope (DPE).

It also incorporates a Risk Registry that can be managed from the UI and several Risk Assessment plugins that update information on the Risk Registry.

## How to assess and mitigate preservation risks in RODA?

So, you want to start doing risk assesment processes in your repository. For example, you want to start a process to convert files from formats that are not sustainable any more (eg. because a new risk appears that a given file format won't be supported in the future).

Basically you would like to have a workflow for the following hypothetical scenario: 

1. You've created a SIP including a Word 95 .doc file 
1. You've identified a (hypothetical) risk regarding Word 95 .doc file (eg. no software in our institute is able to read that format anymore)
1. As the risk is identified, I would like to start a conversion of every Word 95 .doc files to both DOCX and PDF/A

Well, there are several ways how you can manage new risks and start a preservation action to mitigate them, so we’ll just focus on how we would solve your particular example:

Imagine that I, as a preservation expert, know that Word 95 is a format at risk. I would go to the risk registry and registar that risk, detailing all the things I know about that particular risk and appointing possible actions to mitigate it (e.g. migrating it to a new format).

(Another possibility would be to use a plugin that would make this sort of analysis automatically, however, there is no such plugin at the moment. It would have to be developed.)

You can then use the Search feature to locate all the Word 95 files in the repository. All the file formats have been identified during the ingest process so that task is quite simple. I would then use the available Risk association plugin to set these files as instances of the recently created risk. This serves as documentation of the preservation decisions made by the preservation expert and as rationale for what we are going to do next — this is actually Preservation planning.

The next step would be to migrate the files. You can do pretty much the same thing as before, i.e. select all the word 95 files on the Search menu, and run a preservation action on these to migrate them to, lets say, PDF.

You could then lower the risk level as there are no more word 95 files in the system. Incidences can be marked as “mitigated".

What I just explained is the manual workflow, as we don’t currently have a format-obsolescence-risk-detection-plugin. But that plugin could very well be developed. The mitigation steps would, in that case, be started right from the risk management interface.

In what concerns available conversion plugins,  RODA currently supports the usual suspects (major image, video, word and audio formats). Niche formats will always exist in every institution, and in that case, special purpose plugins will have to be developed.

## Have an idea for a risk assessment plugin?

If you are interested in develop a new Risk Assessment plugin, please contact the product team for further information.
