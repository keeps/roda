# Representation Information



*The content herein is a verbatim copy of the article named "OAIS 7: Representation Information" posted at the [Blog Alan's Notes on Digital Preservation](https://alanake.wordpress.com/2008/01/24/oais-7-representation-information/ ).*


Representation Information is a crucial concept, as it is only through our understanding of the Representation Information that a Data Object can be opened and viewed. The Representation Information itself can only be interpreted with respect to a suitable Knowledge Base.

The Representation Information concept is also inextricably tied in with the concept of the Designated Community, becuase how we define the Designated Community (and its associated Knowledge Base) determines how much Representation Information we need. “The OAIS must understand the Knowledge Base of its Designated Community to understand the minimum Representation Information that must be maintained… Over time, evolution of the Designated Community’s Knowledge Base may require updates to the Representation Information to ensure continued understanding” (2.2.1).


The Data Object itself, in a digital repository, is simply a string of bits. What the Representation Information does is convert (or tell us how to convert) these bits to something more meaningful. It describes the format or data structure concepts which should be applied to the bit sequences which in turn result in more meaningful values, such as characters, pixels, tables etc.

This is termed **structure information**. Ideally Representation Information should also contain **semantic information**, eg what human language the text is written in, what any scientific terminology means, and so on (4.2.1.3.1). By including both structure and semantic information we are future-proofing ourselves as much as possible.

Preservation of RI is most easily done when the Representation Information is expressed in an easily understandable form, “such as ASCII” (4.2.1.3.2). What the Model is saying here is that it would be stupid to save the Representation Information in, say, a proprietary or weakly-supported file format, even if the Data Object itself is in such a format. The Representation Information can be printed out onto paper, if that helps.

## What’s the minimum any Representation Information needs to achieve?

The Representation Information must enable or allow the recreation of the significant properties of the original data object. Essentially, that means Representation Information should be able to recreate a copy of the original.

## Representation networks

Representation Information may contain references to other Representation Information. And as the Representation Information is itself an Information Object, with its own Data Object and related Representation Information, a whole net of Representation Information may build up. This is called a Representation network (4.3.1.3.2). For example, the Representation Information for one object might simply state that a record is in ASCII. Depending on how we define our Designated Community, we might have to contribute additional Representation Information, such as what the ASCII standard actually is.

## Representation Information at ingest

A SIP may turn up with very poor Representation Information – perhaps just a printed manual or two or some PDFs on the documentation folder (see E-ARK SIP specification). 

The OAIS needs much more. But this should not deter an OAIS from accepting stuff. It is possible to get too anoraky about Representation networks. Just because a SIP has arrived with only 4 metadata fields completed out of a mandatory 700 is not really a good enough reason to reject it, if it’s a record of permanent value.

## Representation software

The Representation Information can be **executable software**, if that’s helpful. The example given in the Model (4.2.1.3.2) is where the Representation Information exists as a PDF file. Rather than having further Representation Information which defines what a PDF is, it’s more helpful to simply use a PDF viewer, instead. 

The OAIS needs to track this carefully though, because one day there will be no such things as PDF viewers, and the original Representation Information would then need to be migrated to a new form.

## Access software and emulation

In the short term, most records are probably opened by the exact same software in which they were created. Someone opening an archived but recently-created Word document is likely to use their own MS Word app. So this leads to the possibility that extended Representation networks can be abandoned, and we simply use the original software, or soemthing very much like it. 

OAIS calls this Access software, and warns against it, because it means we have to try to keep working software. It strikes me though that this is the whole point of HW emulation. If you have the single phrase “MS Word document” as your Representation Information, and you keep a working copy of the Word app on emulated HW and OS, then you need no Representation network at all. At least, not until it all goes wrong!

## Representation Information in practice

Let’s take a JPEG image file as an example. I think we can take it as read that our current Designated Community understands what a JPEG is: in OAIS terminology, the Designated Community’s Knowledge Base includes the concept and term “JPEG”. So our Representation Information for that file could in theory simply be a statement saying it’s a JPEG image. That’s enough. For the JPEG format, and for many others, the most useful Representation Information is actually an app which can open and view it. (Any current PC will be able to open the JPEG anyway.)

For the longer term, though, we need to prepare for a world in which the Designated Community has moved away from JPEGs. So we should add a link to the JPEG standard’s website, too, to explain what a JPEG is. And we could include info about what software apps can open a JPEG image. More usefully, there should be a link on the Representation network to a place on the web where we can actually download a JPEG viewer.

This means that the Representation network changes over time. We must be able to update it as technology develops.
