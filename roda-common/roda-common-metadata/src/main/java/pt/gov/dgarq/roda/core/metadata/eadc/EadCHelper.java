package pt.gov.dgarq.roda.core.metadata.eadc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlException;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.Acqinfos;
import pt.gov.dgarq.roda.core.data.eadc.Archref;
import pt.gov.dgarq.roda.core.data.eadc.Archrefs;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTable;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableBody;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableGroup;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableHead;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableRow;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronitem;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist;
import pt.gov.dgarq.roda.core.data.eadc.ControlAccess;
import pt.gov.dgarq.roda.core.data.eadc.ControlAccesses;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.Index;
import pt.gov.dgarq.roda.core.data.eadc.Indexentry;
import pt.gov.dgarq.roda.core.data.eadc.ItemList;
import pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages;
import pt.gov.dgarq.roda.core.data.eadc.Materialspecs;
import pt.gov.dgarq.roda.core.data.eadc.Note;
import pt.gov.dgarq.roda.core.data.eadc.Notes;
import pt.gov.dgarq.roda.core.data.eadc.P;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescGenreform;
import pt.gov.dgarq.roda.core.data.eadc.ProcessInfo;
import pt.gov.dgarq.roda.core.data.eadc.Relatedmaterial;
import pt.gov.dgarq.roda.core.data.eadc.Relatedmaterials;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.MetadataHelperUtility;
import pt.gov.dgarq.roda.x2014.eadcSchema.Acqinfo;
import pt.gov.dgarq.roda.x2014.eadcSchema.AcqinfoP;
import pt.gov.dgarq.roda.x2014.eadcSchema.Arrangement;
import pt.gov.dgarq.roda.x2014.eadcSchema.AvLevel.Enum;
import pt.gov.dgarq.roda.x2014.eadcSchema.Bioghist;
import pt.gov.dgarq.roda.x2014.eadcSchema.C;
import pt.gov.dgarq.roda.x2014.eadcSchema.Chronitem;
import pt.gov.dgarq.roda.x2014.eadcSchema.Chronlist;
import pt.gov.dgarq.roda.x2014.eadcSchema.Controlaccess;
import pt.gov.dgarq.roda.x2014.eadcSchema.Custodhist;
import pt.gov.dgarq.roda.x2014.eadcSchema.Date;
import pt.gov.dgarq.roda.x2014.eadcSchema.Did;
import pt.gov.dgarq.roda.x2014.eadcSchema.Dimensions;
import pt.gov.dgarq.roda.x2014.eadcSchema.EadCDocument;
import pt.gov.dgarq.roda.x2014.eadcSchema.Extent;
import pt.gov.dgarq.roda.x2014.eadcSchema.Genreform;
import pt.gov.dgarq.roda.x2014.eadcSchema.Langmaterial;
import pt.gov.dgarq.roda.x2014.eadcSchema.Language;
import pt.gov.dgarq.roda.x2014.eadcSchema.Materialspec;
import pt.gov.dgarq.roda.x2014.eadcSchema.Physdesc;
import pt.gov.dgarq.roda.x2014.eadcSchema.Physfacet;
import pt.gov.dgarq.roda.x2014.eadcSchema.Processinfo;
import pt.gov.dgarq.roda.x2014.eadcSchema.ProcessinfoP;
import pt.gov.dgarq.roda.x2014.eadcSchema.ProcessinfoPArchref;
import pt.gov.dgarq.roda.x2014.eadcSchema.Row;
import pt.gov.dgarq.roda.x2014.eadcSchema.Table;
import pt.gov.dgarq.roda.x2014.eadcSchema.Tbody;
import pt.gov.dgarq.roda.x2014.eadcSchema.Tgroup;
import pt.gov.dgarq.roda.x2014.eadcSchema.Thead;
import pt.gov.dgarq.roda.x2014.eadcSchema.Unitdate;
import pt.gov.dgarq.roda.x2014.eadcSchema.Unitid;
import pt.gov.dgarq.roda.x2014.eadcSchema.UnitidWithOptionalAttributes;
import pt.gov.dgarq.roda.x2014.eadcSchema.Unittitle;

/**
 * This is an helper class for manipulating a EAD-C XML document. It provides
 * methods to read {@link SimpleDescriptionObject}s and
 * {@link DescriptionObject}s from an EAD-C document and methods to write
 * {@link DescriptionObject}s to EAD-C documents.
 * 
 * @author Rui Castro
 */
public class EadCHelper {
  static final private Logger logger = Logger.getLogger(EadCHelper.class);

  private final EadCDocument eadcDocument;

  /**
   * Creates a new instance of a {@link EadCHelper} for the EAD-C XML inside the
   * given {@link File}.
   * 
   * @param eadcFile
   *          the EAD-C XML file.
   * 
   * @return a {@link EadCHelper} for the given EAD-C XML file.
   * 
   * @throws IOException
   *           if some I/O error occurs.
   * @throws FileNotFoundException
   *           if the specified File cannot be found.
   * @throws EadCMetadataException
   *           if the EAD-C XML document is invalid.
   */
  public static EadCHelper newInstance(File eadcFile) throws EadCMetadataException, FileNotFoundException, IOException {
    FileInputStream eadcInputStream = new FileInputStream(eadcFile);
    EadCHelper instance = newInstance(eadcInputStream);
    eadcInputStream.close();
    return instance;
  }

  /**
   * Creates a new instance of a {@link EadCHelper} for the EAD-C XML inside the
   * given {@link InputStream}.
   * 
   * @param eadcInputStream
   *          the EAD-C XML {@link InputStream}.
   * 
   * @return a {@link EadCHelper} for the given EAD-C XML {@link InputStream}.
   * 
   * @throws IOException
   *           if some I/O error occurs.
   * @throws EadCMetadataException
   *           if the EAD-C XML document is invalid.
   */
  public static EadCHelper newInstance(InputStream eadcInputStream) throws EadCMetadataException, IOException {

    try {

      EadCDocument document = EadCDocument.Factory.parse(eadcInputStream);
      if (document.validate()) {
        return new EadCHelper(document);
      } else {
        throw new EadCMetadataException("Error validating XML document");
      }

    } catch (XmlException e) {
      logger.debug("Error parsing EAD-C - " + e.getMessage(), e);
      throw new EadCMetadataException("Error parsing EAD-C - " + e.getMessage(), e);
    }
  }

  /**
   * Constructs a new {@link EadCHelper} with a new EAD-C document.
   */
  public EadCHelper() {
    this(EadCDocument.Factory.newInstance());
  }

  /**
   * Constructs a new {@link EadCHelper} with a new EAD-C document and sets the
   * information inside the given {@link DescriptionObject}.
   * 
   * @param dObject
   *          the {@link DescriptionObject}.
   */
  public EadCHelper(DescriptionObject dObject) {
    this();
    setDescriptionObject(dObject);
  }

  /**
   * Constructs a new {@link EadCHelper} for the given EAD-C document.
   * 
   * @param eadcDocument
   *          the EAD-C document.
   */
  protected EadCHelper(EadCDocument eadcDocument) {

    this.eadcDocument = eadcDocument;

    if (getEadC() == null) {
      getEadcDocument().addNewEadC();
    }
  }

  /**
   * Gets the current EAD-C document.
   * 
   * @return the eadcDocument
   */
  public EadCDocument getEadcDocument() {
    return eadcDocument;
  }

  /**
   * Gets the current EAD-C.
   * 
   * @return the current {@link C}.
   */
  public C getEadC() {
    return getEadcDocument().getEadC();
  }

  /**
   * Gets a {@link SimpleDescriptionObject} from the current
   * {@link EadCDocument}.
   * 
   * @return a {@link SimpleDescriptionObject}.
   */
  public SimpleDescriptionObject getSimpleDescriptionObject() {
    return getSimpleDescriptionObject(new SimpleDescriptionObject(), null, 0);

  }

  /**
   * Gets a {@link SimpleDescriptionObject} from the current
   * {@link EadCDocument}.
   * 
   * @param rodaObject
   *          the {@link RODAObject} that should be returned as a
   *          {@link SimpleDescriptionObject}.
   * @param subElementsCount
   *          the number of sub-elements for the {@link SimpleDescriptionObject}
   *          .
   * 
   * @return a {@link SimpleDescriptionObject}.
   * 
   * @throws InvalidDescriptionLevel
   *           if the level being set is not valid
   */
  public SimpleDescriptionObject getSimpleDescriptionObject(RODAObject rodaObject, String parentPID,
    int subElementsCount) throws InvalidDescriptionLevel {

    C c = getEadC();

    SimpleDescriptionObject sdo = new SimpleDescriptionObject(rodaObject);

    sdo.setParentPID(parentPID);
    sdo.setSubElementsCount(subElementsCount);

    // SimpleDescriptionObject must have this fields
    // @level,
    // did/unitid/@repositorycode,
    // did/unitid,
    // did/unittitle,
    // did/unitdate.

    // @level
    sdo.setLevel(new DescriptionLevel(c.getLevel().toString()));

    Did did = c.getDid();
    if (did != null) {

      // FIXME fully support several unitid occurrences
      // did/unitid/@repositorycode
      Unitid unitid = null;
      if (did.getUnitidList().size() > 0 && did.getUnitidList().get(0) != null) {
        unitid = did.getUnitidList().get(0);

        if (StringUtils.isNotBlank(unitid.getRepositorycode())) {

          String repositoryCode = unitid.getRepositorycode();

          int indexOfDivider = repositoryCode.indexOf("-");
          if (indexOfDivider >= 0) {

            sdo.setCountryCode(repositoryCode.substring(0, indexOfDivider));
            if (indexOfDivider + 1 < repositoryCode.length()) {
              sdo.setRepositoryCode(repositoryCode.substring(indexOfDivider + 1));
            }

          } else {
            logger.warn("Invalid countryRepositoryCode '" + unitid.getRepositorycode() + "'");
          }
        }

        // did/unitid
        sdo.setId(unitid.getStringValue());

      }

      // FIXME fully support several unittitle occurrences
      Unittitle unittitle = null;
      if (did.getUnittitleList().size() > 0 && did.getUnittitleList().get(0) != null) {
        unittitle = did.getUnittitleList().get(0);
        // did/unittitle
        sdo.setTitle(unittitle.getStringValue());
      }

      // FIXME fully support several unitdate occurrences
      // did/unitdate
      Unitdate unitdate = null;
      if (did.getUnitdateList().size() > 0 && did.getUnitdateList().get(0) != null) {
        unitdate = did.getUnitdateList().get(0);

        String normalDate = unitdate.getNormal();
        if (normalDate != null) {
          String[] dates = normalDate.split("/");
          if (dates.length > 0) {
            // dateInitial
            sdo.setDateInitial(dates[0]);
            if (dates.length > 1) {
              // dateFinal
              sdo.setDateFinal(dates[1]);
            } else {
              // dateFinal is equal to dateInitial
              sdo.setDateFinal(dates[0]);
            }
          }
        }
      }
    }

    String description = null;

    // FIXME fully support several scopecontent occurrences
    // scopecontent/p
    if (c.getScopecontentList().size() > 0 && c.getScopecontentList().get(0) != null) {
      description = c.getScopecontentList().get(0).getP();
    }

    // FIXME fully support several bioghist occurrences
    // bioghist
    Bioghist bioghist = null;
    if (c.getBioghistList().size() > 0 && c.getBioghistList().get(0) != null) {
      bioghist = c.getBioghistList().get(0);
      // bioghist/p
      if (bioghist.getP() != null && bioghist.getP().length() > 0) {

        if (description != null) {
          description += "\n\n" + bioghist.getP();
        } else {
          description = bioghist.getP();
        }
      }
    } // End of bioghist

    sdo.setDescription(description);

    return sdo;
  }

  /**
   * Gets a {@link DescriptionObject} from the current {@link EadCDocument}.
   * 
   * @return a {@link DescriptionObject}.
   */
  public DescriptionObject getDescriptionObject() {
    return getDescriptionObject(new SimpleDescriptionObject(), null, 0);
  }

  /**
   * Gets a {@link DescriptionObject} from the current {@link EadCDocument}.
   * 
   * @param rodaObject
   *          the {@link RODAObject} that should be returned as a
   *          {@link DescriptionObject}.
   * @param subElementsCount
   *          the number of sub-elements for the {@link DescriptionObject}.
   * 
   * @return a {@link DescriptionObject}.
   */
  public DescriptionObject getDescriptionObject(RODAObject rodaObject, String parentPID, int subElementsCount) {

    return getDescriptionObject(getSimpleDescriptionObject(rodaObject, parentPID, subElementsCount));
  }

  /**
   * Gets a {@link DescriptionObject} from the current {@link EadCDocument}.
   * 
   * @param simpleDO
   *          the {@link SimpleDescriptionObject} that should be returned as a
   *          {@link DescriptionObject}.
   * 
   * @return a {@link DescriptionObject}.
   */
  public DescriptionObject getDescriptionObject(SimpleDescriptionObject simpleDO) {

    DescriptionObject dObject = new DescriptionObject(simpleDO);

    C c = getEadC();

    // SimpleDescriptionObject already has this fields
    // @level,
    // did/unitid/@repositorycode,
    // did/unitid,
    // did/unittitle,
    // did/unitdate.

    Did did = c.getDid();
    if (did != null) {

      // FIXME fully support several abstract occurrences
      // did/abstract
      if (did.getAbstractList().size() > 0 && did.getAbstractList().get(0) != null) {
        dObject.setAbstract(did.getAbstractList().get(0).getStringValue());
      }

      // FIXME fully support several physdesc occurrences
      // did/physdesc
      Physdesc physdesc = null;
      if (did.getPhysdescList().size() > 0 && did.getPhysdescList().get(0) != null) {
        physdesc = did.getPhysdescList().get(0);

        // did/physdesc/p
        if (physdesc.getP() != null) {
          dObject.setPhysdesc(physdesc.getP());
        }

        // did/physdesc/dimensions
        Dimensions dimensions = physdesc.getDimensions();
        if (dimensions != null) {

          // did/physdesc/dimensions/@unit
          dObject.setPhysdescDimensions(new PhysdescElement(dimensions.getStringValue(), dimensions.getUnit()));
        }

        // did/physdesc/physfacet
        Physfacet physfacet = physdesc.getPhysfacet();
        if (physfacet != null) {
          // did/physdesc/physfacet/@unit
          dObject.setPhysdescPhysfacet(new PhysdescElement(physfacet.getStringValue(), physfacet.getUnit()));
        }

        // did/physdesc/date
        Date date = physdesc.getDate();
        if (date != null) {

          String normalDate = date.getNormal();
          if (normalDate != null) {
            String[] dates = normalDate.split("/");
            if (dates.length > 0) {
              // dateInitial
              dObject.setPhysdescDateInitial(dates[0]);
              if (dates.length > 1) {
                // dateFinal
                dObject.setPhysdescDateFinal(dates[1]);
              }
            }
          }
        }

        // did/physdesc/extent
        Extent extent = physdesc.getExtent();
        if (extent != null) {
          // did/physdesc/extent/@unit
          dObject.setPhysdescExtent(new PhysdescElement(extent.getStringValue(), extent.getUnit()));
        }

        // did/physdesc/genreform
        Genreform genreform = physdesc.getGenreform();
        if (genreform != null) {
          dObject.setPhysdescGenreform(new PhysdescGenreform(genreform.getSource(), genreform.getAuthfilenumber(),
            genreform.getNormal(), genreform.getStringValue()));
        }

      } // End of did/physdesc

      // did/materialspec
      if (did.getMaterialspecList().size() > 0) {
        pt.gov.dgarq.roda.core.data.eadc.Materialspec[] materialspecs = new pt.gov.dgarq.roda.core.data.eadc.Materialspec[did
          .getMaterialspecList().size()];

        for (int i = 0; i < did.getMaterialspecList().size(); i++) {
          Materialspec eadMaterialspec = did.getMaterialspecList().get(i);
          pt.gov.dgarq.roda.core.data.eadc.Materialspec materialspec = new pt.gov.dgarq.roda.core.data.eadc.Materialspec();

          // did/materialspec/@label
          if (eadMaterialspec.getLabel() != null) {
            materialspec.setAttributeLabel(eadMaterialspec.getLabel());
          }

          // did/materialspec/text()
          if (eadMaterialspec.getStringValue() != null) {
            materialspec.setText(eadMaterialspec.getStringValue());
          }

          materialspecs[i] = materialspec;
        }

        dObject.setMaterialspecs(new Materialspecs(materialspecs));
      }

      // FIXME fully support several origination occurrences
      // did/origination
      if (did.getOriginationList().size() > 0 && did.getOriginationList().get(0) != null) {
        dObject.setOrigination(did.getOriginationList().get(0).getStringValue());
      }

      // FIXME fully support several langmaterial occurrences
      // did/langmaterial
      Langmaterial langmaterial = null;
      if (did.getLangmaterialList().size() > 0 && did.getLangmaterialList().get(0) != null) {
        langmaterial = did.getLangmaterialList().get(0);

        List<Language> languageList = langmaterial.getLanguageList();

        String[] languages = null;

        if (languageList != null) {

          languages = new String[languageList.size()];

          for (int i = 0; i < languageList.size(); i++) {
            languages[i] = languageList.get(i).getStringValue();
          }
        }

        dObject.setLangmaterialLanguages(new LangmaterialLanguages(languages));
      }

    } // End of did

    // FIXME fully support several processinfo occurrences
    // processinfo
    Processinfo eadProcessinfo = null;
    if (c.getProcessinfoList().size() > 0 && c.getProcessinfoList().get(0) != null) {
      eadProcessinfo = c.getProcessinfoList().get(0);
      ProcessInfo processinfo = new ProcessInfo();

      // processinfo/@altrender
      if (eadProcessinfo.getAltrender() != null) {
        processinfo.setAttributeAltrender(eadProcessinfo.getAltrender().getStringValue());
      }

      // processinfo/note
      if (eadProcessinfo.getNote() != null) {
        Note note = new Note();

        // processinfo/note/@altrender
        if (eadProcessinfo.getNote().getAltrender() != null) {
          note.setAttributeAltrender(eadProcessinfo.getNote().getAltrender().getStringValue());
        }

        // processinfo/note/p/text()
        if (eadProcessinfo.getNote().getP() != null) {
          note.setP(new P(eadProcessinfo.getNote().getP()));
        }

        processinfo.setNote(note);
      }

      // processinfo/p
      if (eadProcessinfo.getPList().size() > 0) {
        List<ProcessinfoP> eadPList = eadProcessinfo.getPList();
        P[] pList = new P[eadPList.size()];
        P p;
        ProcessinfoP eadP;

        for (int i = 0; i < eadPList.size(); i++) {
          eadP = eadPList.get(i);
          p = new P();

          // processinfo/p/@altrender
          if (eadP.getAltrender() != null) {
            p.setAttributeAltrender(eadP.getAltrender().getStringValue());
          }

          // processinfo/p/note
          if (eadP.getNote() != null) {
            Note note = new Note();

            // processinfo/p/note/@altrender
            if (eadP.getNote().getAltrender() != null) {
              note.setAttributeAltrender(eadP.getNote().getAltrender().getStringValue());
            }

            // processinfo/p/note/p/text()
            if (eadP.getNote().getP() != null) {
              note.setP(new P(eadP.getNote().getP()));
            }

            // just one note at the moment
            p.setNotes(new Notes(new Note[] {note}));
          }

          // processinfo/p/archref
          if (eadP.getArchrefList().size() > 0 && eadP.getArchrefList().get(0) != null) {
            ProcessinfoPArchref eadArchref = eadP.getArchrefList().get(0);
            Archref archref = new Archref();
            List<UnitidWithOptionalAttributes> eadUnitidList = eadArchref.getUnitidList();
            pt.gov.dgarq.roda.core.data.eadc.Unitid[] unitids = new pt.gov.dgarq.roda.core.data.eadc.Unitid[eadUnitidList
              .size()];

            // processinfo/p/archref/unitid
            for (int j = 0; j < eadUnitidList.size(); j++) {
              UnitidWithOptionalAttributes eadUnitid = eadUnitidList.get(j);
              pt.gov.dgarq.roda.core.data.eadc.Unitid unitid = new pt.gov.dgarq.roda.core.data.eadc.Unitid();

              // processinfo/p/archref/unitid/@altrender
              if (eadUnitid.getAltrender() != null) {
                unitid.setAttributeAltrender(eadUnitid.getAltrender().getStringValue());
              }

              // processinfo/p/archref/unitid/text()
              unitid.setText(eadUnitid.getStringValue());

              unitids[j] = unitid;
            }
            archref.setUnitids(unitids);

            // processinfo/p/archref/note
            if (eadArchref.getNote() != null) {
              Note note = new Note();

              // processinfo/p/archref/note/@altrender
              if (eadArchref.getNote().getAltrender() != null) {
                note.setAttributeAltrender(eadArchref.getNote().getAltrender().getStringValue());
              }

              // processinfo/p/archref/note/p/text()
              if (eadArchref.getNote().getP() != null) {
                note.setP(new P(eadArchref.getNote().getP()));
              }
              archref.setNote(note);
            }

            p.setArchrefs(new Archrefs(new Archref[] {archref}));
          }

          pList[i] = p;
        }

        processinfo.setpList(pList);
      }

      dObject.setProcessinfo(processinfo);
    }

    // controlaccess
    if (c.getControlaccessList().size() > 0) {
      ControlAccess[] controlaccesses = new ControlAccess[c.getControlaccessList().size()];
      for (int i = 0; i < c.getControlaccessList().size(); i++) {
        Controlaccess eadControlaccess = c.getControlaccessList().get(i);
        ControlAccess controlaccess = new ControlAccess();
        if (eadControlaccess.getEncodinganalog() != null) {
          controlaccess.setAttributeEncodinganalog(eadControlaccess.getEncodinganalog());
        }
        if (eadControlaccess.getFunctionList().size() > 0 && eadControlaccess.getFunctionList().get(0) != null) {
          controlaccess.setFunction(eadControlaccess.getFunctionList().get(0));
        }
        if (eadControlaccess.getHeadList().size() > 0 && eadControlaccess.getHeadList().get(0) != null) {
          controlaccess.setHead(eadControlaccess.getHeadList().get(0));
        }
        if (eadControlaccess.getSubjectList().size() > 0 && eadControlaccess.getSubjectList().get(0) != null) {
          controlaccess.setSubject(eadControlaccess.getSubjectList().get(0));
        }
        if (eadControlaccess.getPList().size() > 0 && eadControlaccess.getPList().get(0) != null) {
          controlaccess.setP(eadControlaccess.getPList().get(0));
        }
        controlaccesses[i] = controlaccess;
      }
      dObject.setControlaccesses(new ControlAccesses(controlaccesses));
    }

    // FIXME fully support several odd occurrences
    // odd
    if (c.getOddList().size() > 0 && c.getOddList().get(0) != null) {
      dObject.setOdd(c.getOddList().get(0).getStringValue());
    }

    // FIXME fully support several bioghist occurrences
    // bioghist
    Bioghist bioghist = null;
    if (c.getBioghistList().size() > 0 && c.getBioghistList().get(0) != null) {
      bioghist = c.getBioghistList().get(0);

      // bioghist/p
      if (bioghist.getP() != null) {
        dObject.setBioghist(bioghist.getP());
      }

      // bioghist/chronlist
      Chronlist chronlist = bioghist.getChronlist();
      if (chronlist != null) {

        List<Chronitem> chronitemList = chronlist.getChronitemList();

        BioghistChronitem[] chronitems = new BioghistChronitem[chronitemList.size()];

        int index = 0;
        for (Chronitem chronitem : chronitemList) {

          String dateInitial = null, dateFinal = null;
          String normalDate = chronitem.getDate().getNormal();
          if (normalDate != null) {
            String[] dates = normalDate.split("/");
            if (dates.length > 0) {
              // dateInitial
              dateInitial = dates[0];
              if (dates.length > 1) {
                // dateFinal
                dateFinal = dates[1];
              }
            }
          }

          chronitems[index] = new BioghistChronitem(chronitem.getEvent(), dateInitial, dateFinal);

          index++;
        }

        dObject.setBioghistChronlist(new BioghistChronlist(chronitems));
      }
    } // End of bioghist

    // FIXME fully support several custodhist occurrences
    // custodhist/p
    Custodhist custodhist = null;
    if (c.getCustodhistList().size() > 0 && c.getCustodhistList().get(0) != null) {
      custodhist = c.getCustodhistList().get(0);
      dObject.setCustodhist(custodhist.getP());
    }

    // acqinfo
    if (c.getAcqinfoList().size() > 0) {
      pt.gov.dgarq.roda.core.data.eadc.Acqinfo[] acqinfos = new pt.gov.dgarq.roda.core.data.eadc.Acqinfo[c
        .getAcqinfoList().size()];

      for (int i = 0; i < c.getAcqinfoList().size(); i++) {
        Acqinfo eadAcqinfo = c.getAcqinfoList().get(i);
        pt.gov.dgarq.roda.core.data.eadc.Acqinfo acqinfo = new pt.gov.dgarq.roda.core.data.eadc.Acqinfo();

        // acqinfo/@altrender
        if (eadAcqinfo.getAltrender() != null) {
          acqinfo.setAttributeAltrender(eadAcqinfo.getAltrender().getStringValue());
        }

        // acqinfo/p
        if (eadAcqinfo.getP() != null) {
          AcqinfoP eadP = eadAcqinfo.getP();
          P p = new P();

          // acqinfo/p/text()
          XmlCursor cursor = eadP.newCursor();
          cursor.toLastAttribute();
          TokenType nextToken = cursor.toNextToken();
          if (nextToken.isText() && cursor.getChars()!=null && !"".equals(cursor.getChars().trim())) {
            p.setText(cursor.getTextValue());
          }
          cursor.dispose();

          // acqinfo/p/date
          if (eadP.getDate() != null) {
            p.setDate(eadP.getDate().getNormal());
          }

          // acqinfo/p/num
          if (eadP.getNum() != null) {
            p.setNum(eadP.getNum());
          }

          // acqinfo/p/corpname
          if (eadP.getCorpname() != null) {
            p.setCorpname(eadP.getCorpname());
          }

          acqinfo.setP(p);
        }

        acqinfos[i] = acqinfo;
      }

      dObject.setAcqinfos(new Acqinfos(acqinfos));
    }

    // FIXME fully support several scopecontent occurrences
    // scopecontent/p
    if (c.getScopecontentList().size() > 0 && c.getScopecontentList().get(0) != null) {
      dObject.setScopecontent(c.getScopecontentList().get(0).getP());
    }

    // FIXME fully support several appraisal occurrences
    // appraisal/p
    if (c.getAppraisalList().size() > 0 && c.getAppraisalList().get(0) != null) {
      dObject.setAppraisal(c.getAppraisalList().get(0).getP());
    }

    // FIXME fully support several accruals occurrences
    // accruals/p
    if (c.getAccrualsList().size() > 0 && c.getAccrualsList().get(0) != null) {
      dObject.setAccruals(c.getAccrualsList().get(0).getP());
    }

    // FIXME fully support several arrangement occurrences
    // arrangement
    Arrangement arrangement = null;
    if (c.getArrangementList().size() > 0 && c.getArrangementList().get(0) != null) {
      arrangement = c.getArrangementList().get(0);

      // arrangement/p
      if (arrangement.getP() != null) {
        dObject.setArrangement(arrangement.getP());
      }

      // arrangement/table
      dObject.setArrangementTable(new ArrangementTable(readArrangementTableGroups(arrangement.getTable())));

    } // End of arrangement

    // FIXME fully support several accessrestrict occurrences
    // accessrestrict/p
    if (c.getAccessrestrictList().size() > 0 && c.getAccessrestrictList().get(0) != null) {
      dObject.setAccessrestrict(c.getAccessrestrictList().get(0).getP());
    }

    // FIXME fully support several userestrict occurrences
    // userestrict/p
    if (c.getUserestrictList().size() > 0 && c.getUserestrictList().get(0) != null) {
      dObject.setUserestrict(c.getUserestrictList().get(0).getP());
    }

    // FIXME fully support several phystech occurrences
    // phystech/p
    if (c.getPhystechList().size() > 0 && c.getPhystechList().get(0) != null) {
      dObject.setPhystech(c.getPhystechList().get(0).getP());

    }

    // FIXME fully support several otherfindaid occurrences
    // otherfindaid/p
    if (c.getOtherfindaidList().size() > 0 && c.getOtherfindaidList().get(0) != null) {
      dObject.setOtherfindaid(c.getOtherfindaidList().get(0).getP());
    }

    // relatedmaterial
    if (c.getRelatedmaterialList().size() > 0) {
      Relatedmaterial[] relatedmaterials = new Relatedmaterial[c.getRelatedmaterialList().size()];

      for (int i = 0; i < c.getRelatedmaterialList().size(); i++) {
        pt.gov.dgarq.roda.x2014.eadcSchema.Relatedmaterial eadRelatedmaterial = c.getRelatedmaterialList().get(i);
        Relatedmaterial relatedmaterial = new Relatedmaterial();

        // relatedmaterial/p
        if (eadRelatedmaterial.getP() != null) {
          relatedmaterial.setP(new P(eadRelatedmaterial.getP()));
        }

        // FIXME just one archref at the moment
        // relatedmaterial/archref
        if (eadRelatedmaterial.getArchrefList() != null && eadRelatedmaterial.getArchrefList().size() > 0) {
          pt.gov.dgarq.roda.x2014.eadcSchema.Archref eadArchref = eadRelatedmaterial.getArchrefList().get(0);
          Archref newArchref = new Archref();

          // relatedmaterial/archref/unitid
          if (eadArchref.getUnitidList().size() > 0) {
            List<UnitidWithOptionalAttributes> eadUnitidList = eadArchref.getUnitidList();
            pt.gov.dgarq.roda.core.data.eadc.Unitid[] unitids = new pt.gov.dgarq.roda.core.data.eadc.Unitid[eadUnitidList
              .size()];

            for (int j = 0; j < eadUnitidList.size(); j++) {
              UnitidWithOptionalAttributes eadUnitid = eadUnitidList.get(j);
              pt.gov.dgarq.roda.core.data.eadc.Unitid unitid = new pt.gov.dgarq.roda.core.data.eadc.Unitid();

              // relatedmaterial/archref/unitid/@altrender
              if (eadUnitid.getAltrender() != null) {
                unitid.setAttributeAltrender(eadUnitid.getAltrender().getStringValue());
              }

              // processinfo/p/archref/unitid/text()
              unitid.setText(eadUnitid.getStringValue());

              unitids[j] = unitid;
            }
            newArchref.setUnitids(unitids);
          }

          // relatedmaterial/archref/unittile
          if (eadArchref.getUnittitle() != null) {
            newArchref.setUnittitle(eadArchref.getUnittitle());
          }

          relatedmaterial.setArchref(newArchref);
        }

        relatedmaterials[i] = relatedmaterial;
      }

      dObject.setRelatedmaterials(new Relatedmaterials(relatedmaterials));
    }

    // FIXME fully support several bibliography occurrences
    // bibliography/p
    if (c.getBibliographyList().size() > 0 && c.getBibliographyList().get(0) != null) {
      dObject.setBibliography(c.getBibliographyList().get(0).getP());
    }

    // note
    if (c.getNoteList().size() > 0) {
      Note[] notes = new Note[c.getNoteList().size()];
      for (int j = 0; j < c.getNoteList().size(); j++) {
        pt.gov.dgarq.roda.x2014.eadcSchema.Note eadNote = c.getNoteList().get(j);
        Note newNote = new Note();

        // note/@label
        if (eadNote.getLabel() != null) {
          newNote.setAttributeLabel(eadNote.getLabel().getStringValue());
        }

        // note/@altrender
        if (eadNote.getAltrender() != null) {
          newNote.setAttributeAltrender(eadNote.getAltrender().getStringValue());
        }

        // note/p
        if (eadNote.getP() != null) {
          newNote.setP(new P(eadNote.getP()));
        }

        // note/list
        if (eadNote.getList() != null && eadNote.getList().getItemList() != null) {
          String[] items = new String[eadNote.getList().getItemList().size()];

          // note/list/item
          for (int k = 0; k < eadNote.getList().getItemList().size(); k++) {
            items[k] = eadNote.getList().getItemList().get(k);
          }
          newNote.setList(new ItemList(items));
        }

        notes[j] = newNote;
      }

      dObject.setNotes(new Notes(notes));
    }

    // FIXME fully support several index occurrences
    // index
    if (c.getIndexList().size() > 0 && c.getIndexList().get(0) != null) {
      pt.gov.dgarq.roda.x2014.eadcSchema.Index eadIndex = c.getIndexList().get(0);
      List<pt.gov.dgarq.roda.x2014.eadcSchema.Indexentry> eadIndexentryList = eadIndex.getIndexentryList();

      // index/indexentries
      if (eadIndexentryList.size() > 0) {
        Indexentry[] indexentries = new Indexentry[eadIndexentryList.size()];

        for (int i = 0; i < eadIndexentryList.size(); i++) {
          // index/indexentries/subject
          indexentries[i] = new Indexentry(eadIndexentryList.get(i).getSubject().getStringValue());
        }

        dObject.setIndex(new Index(indexentries));
      }
    }

    // FIXME fully support several prefercite occurrences
    // prefercite/p
    if (c.getPreferciteList().size() > 0 && c.getPreferciteList().get(0) != null) {
      dObject.setPrefercite(c.getPreferciteList().get(0).getP());
    }

    return dObject;
  }

  /**
   * Replaces the current EAD-C XML data for the data inside the given
   * {@link DescriptionObject}.
   * 
   * @param dObject
   *          the {@link DescriptionObject}.
   */
  public void setDescriptionObject(DescriptionObject dObject) {

    // Replaces the current EAD-C with a new empty <eadc>
    setEadC(C.Factory.newInstance());
    C c = getEadC();

    // @otherlevel
    c.setLevel(Enum.forString(dObject.getLevel().getLevel()));

    Did did = c.addNewDid();

    // did/unitid
    Unitid unitid = did.addNewUnitid();
    unitid.setStringValue(dObject.getId());

    // did/unitid/@repositorycode
    String countryRepositoryCode = "";
    if (!StringUtils.isBlank(dObject.getCountryCode())) {
      countryRepositoryCode += dObject.getCountryCode();
    }
    countryRepositoryCode += "-";
    if (!StringUtils.isBlank(dObject.getRepositoryCode())) {
      countryRepositoryCode += dObject.getRepositoryCode();
    }
    unitid.setRepositorycode(countryRepositoryCode);

    // did/unittitle
    did.addNewUnittitle().setStringValue(dObject.getTitle());
    // if (dObject.getTitle() != null) {
    // did.setUnittitle(dObject.getTitle());
    // }

    // did/unitdate
    String joinDates2 = joinDates(dObject.getDateInitial(), dObject.getDateFinal());
    if (joinDates2 != null) {
      did.addNewUnitdate().setNormal(joinDates2);
    }

    // did/abstract
    if (dObject.getAbstract() != null) {
      did.addNewAbstract().setStringValue(dObject.getAbstract());
    }

    // did/physdesc
    Physdesc physdesc = did.addNewPhysdesc();

    // did/physdesc/p
    if (dObject.getPhysdesc() != null) {
      physdesc.setP(dObject.getPhysdesc());
    }

    // did/physdesc/dimensions
    if (dObject.getPhysdescDimensions() != null) {
      Dimensions dimensions = physdesc.addNewDimensions();

      // did/physdesc/dimensions/text()
      dimensions.setStringValue(dObject.getPhysdescDimensions().getValue());

      // did/physdesc/dimensions/@unit
      if (dObject.getPhysdescDimensions().getUnit() != null) {

        dimensions.setUnit(dObject.getPhysdescDimensions().getUnit());
      }

    }

    // did/physdesc/physfacet
    if (dObject.getPhysdescPhysfacet() != null) {
      Physfacet physfacet = physdesc.addNewPhysfacet();

      // did/physdesc/physfacet/text()
      physfacet.setStringValue(dObject.getPhysdescPhysfacet().getValue());

      // did/physdesc/physfacet/@unit
      if (dObject.getPhysdescPhysfacet().getUnit() != null) {
        physfacet.setUnit(dObject.getPhysdescPhysfacet().getUnit());
      }

    }

    // did/physdesc/date
    String joinDates = joinDates(dObject.getPhysdescDateInitial(), dObject.getPhysdescDateFinal());
    if (joinDates != null) {
      physdesc.addNewDate().setNormal(joinDates);
    }

    // did/physdesc/extent
    if (dObject.getPhysdescExtent() != null) {
      Extent extent = physdesc.addNewExtent();

      // did/physdesc/extent/text()
      extent.setStringValue(dObject.getPhysdescExtent().getValue());

      // did/physdesc/extent/@unit
      if (dObject.getPhysdescExtent().getUnit() != null) {
        extent.setUnit(dObject.getPhysdescExtent().getUnit());
      }
    }

    // did/physdesc/genreform
    if (dObject.getPhysdescGenreform() != null) {
      Genreform genreform = physdesc.addNewGenreform();

      // did/physdesc/genreform/@authfilenumber
      if (dObject.getPhysdescGenreform().getAttributeAuthfilenumber() != null) {
        genreform.setAuthfilenumber(dObject.getPhysdescGenreform().getAttributeAuthfilenumber());
      }

      // did/physdesc/genreform/@normal
      if (dObject.getPhysdescGenreform().getAttributeNormal() != null) {
        genreform.setNormal(dObject.getPhysdescGenreform().getAttributeNormal());
      }

      // did/physdesc/genreform/@source
      if (dObject.getPhysdescGenreform().getAttributeSource() != null) {
        genreform.setSource(dObject.getPhysdescGenreform().getAttributeSource());
      }

      // did/physdesc/genreform/text()
      genreform.setStringValue(dObject.getPhysdescGenreform().getText());
    }

    // did/materialspec
    if (dObject.getMaterialspecs() != null && dObject.getMaterialspecs().getMaterialspecs() != null) {

      for (pt.gov.dgarq.roda.core.data.eadc.Materialspec materialspec : dObject.getMaterialspecs().getMaterialspecs()) {
        Materialspec newMaterialspec = did.addNewMaterialspec();

        // did/materialspec/@label
        if (materialspec.getAttributeLabel() != null) {
          newMaterialspec.setLabel(materialspec.getAttributeLabel());
        }

        // did/materialspec/text()
        newMaterialspec.setStringValue(materialspec.getText());
      }

    }

    // did/origination
    if (dObject.getOrigination() != null) {
      did.addNewOrigination().setStringValue(dObject.getOrigination());
    }

    // did/langmaterial
    if (dObject.getLangmaterialLanguages() != null
      && dObject.getLangmaterialLanguages().getLangmaterialLanguages() != null
      && dObject.getLangmaterialLanguages().getLangmaterialLanguages().length > 0) {

      Langmaterial langmaterial = did.addNewLangmaterial();

      for (String language : dObject.getLangmaterialLanguages().getLangmaterialLanguages()) {
        langmaterial.addNewLanguage().setStringValue(language);
      }

    }
    // End of did

    // processinfo
    ProcessInfo processinfo = dObject.getProcessinfo();
    if (processinfo != null) {
      Processinfo newProcessinfo = c.addNewProcessinfo();

      // processinfo/@altrender
      newProcessinfo.addNewAltrender().setStringValue(processinfo.getAttributeAltrender());

      // processinfo/note
      if (processinfo.getNote() != null) {
        pt.gov.dgarq.roda.x2014.eadcSchema.Note newNote = newProcessinfo.addNewNote();

        // processinfo/note/@altrender
        if (processinfo.getNote().getAttributeAltrender() != null) {
          newNote.addNewAltrender().setStringValue(processinfo.getNote().getAttributeAltrender());
        }

        // processinfo/note/p/text()
        if (processinfo.getNote().getP() != null) {
          newNote.setP(processinfo.getNote().getP().getText());
        }
      }

      // processinfo/p
      if (processinfo.getpList() != null && processinfo.getpList().length > 0) {
        for (P p : processinfo.getpList()) {
          ProcessinfoP newP = newProcessinfo.addNewP();

          // processinfo/p/@altrender
          if (p.getAttributeAltrender() != null) {
            newP.addNewAltrender().setStringValue(p.getAttributeAltrender());
          }

          // processinfo/p/note
          if (p.getNotes() != null && p.getNotes().getNotes() != null && p.getNotes().getNotes().length > 0) {

            for (Note note : p.getNotes().getNotes()) {
              pt.gov.dgarq.roda.x2014.eadcSchema.Note newNote = newP.addNewNote();

              // processinfo/p/note/@altrender
              if (note.getAttributeAltrender() != null) {
                newNote.addNewAltrender().setStringValue(note.getAttributeAltrender());
              }

              // processinfo/p/note/p/text()
              if (note.getP() != null) {
                newNote.setP(note.getP().getText());
              }
            }
          }

          // processinfo/p/archref
          if (p.getArchrefs() != null && p.getArchrefs().getArchrefs() != null
            && p.getArchrefs().getArchrefs().length > 0) {
            for (Archref archref : p.getArchrefs().getArchrefs()) {
              ProcessinfoPArchref newArchref = newP.addNewArchref();

              // processinfo/p/archref/unitid
              if (archref.getUnitids() != null && archref.getUnitids().length > 0) {
                for (pt.gov.dgarq.roda.core.data.eadc.Unitid id : archref.getUnitids()) {
                  UnitidWithOptionalAttributes newUnitid = newArchref.addNewUnitid();

                  // processinfo/p/archref/unitid/@altrender
                  if (id.getAttributeAltrender() != null) {
                    newUnitid.addNewAltrender().setStringValue(id.getAttributeAltrender());
                  }

                  // processinfo/p/archref/unitid/text()
                  newUnitid.setStringValue(id.getText());
                }
              }

              // processinfo/p/archref/note
              if (archref.getNote() != null) {
                pt.gov.dgarq.roda.x2014.eadcSchema.Note newNote = newArchref.addNewNote();

                // processinfo/p/archref/note/@altrender
                if (archref.getNote().getAttributeAltrender() != null) {
                  newNote.addNewAltrender().setStringValue(archref.getNote().getAttributeAltrender());
                }

                // processinfo/p/archref/note/p/text()
                if (archref.getNote().getP() != null) {
                  newNote.setP(archref.getNote().getP().getText());
                }
              }
            }
          }

        }
      }
    }

    // controlaccess
    ControlAccesses dObjectControlaccesses = dObject.getControlaccesses();
    if (dObjectControlaccesses != null && dObjectControlaccesses.getControlaccesses() != null) {
      for (ControlAccess dObjectControlaccess : dObjectControlaccesses.getControlaccesses()) {
        Controlaccess eadControlaccess = c.addNewControlaccess();
        if (dObjectControlaccess.getAttributeEncodinganalog() != null) {
          eadControlaccess.setEncodinganalog(dObjectControlaccess.getAttributeEncodinganalog());
        }
        if (dObjectControlaccess.getFunction() != null) {
          eadControlaccess.setFunctionArray(new String[] {dObjectControlaccess.getFunction()});
        }
        if (dObjectControlaccess.getHead() != null) {
          eadControlaccess.setHeadArray(new String[] {dObjectControlaccess.getHead()});
        }
        if (dObjectControlaccess.getSubject() != null) {
          eadControlaccess.setSubjectArray(new String[] {dObjectControlaccess.getSubject()});
        }
        if (dObjectControlaccess.getP() != null) {
          eadControlaccess.addP(dObjectControlaccess.getP());
        }
      }
    }

    // odd
    if (dObject.getOdd() != null) {
      c.addNewOdd().setStringValue(dObject.getOdd());
    }

    // bioghist
    if (dObject.getBioghist() != null || dObject.getBioghistChronlist() != null) {

      Bioghist bioghist = c.addNewBioghist();

      // bioghist/p
      if (dObject.getBioghist() != null) {
        bioghist.setP(dObject.getBioghist());
      }

      // bioghist/chronlist
      if (dObject.getBioghistChronlist() != null && dObject.getBioghistChronlist().getBioghistChronitems() != null
        && dObject.getBioghistChronlist().getBioghistChronitems().length > 0) {

        Chronlist chronlist = bioghist.addNewChronlist();

        for (BioghistChronitem item : dObject.getBioghistChronlist().getBioghistChronitems()) {

          Chronitem chronitem = chronlist.addNewChronitem();
          chronitem.addNewDate().setNormal(joinDates(item.getDateInitial(), item.getDateFinal()));
          chronitem.setEvent(item.getEvent());
        }
      } // End of bioghist/chronlist

    } // End of bioghist

    // custodhist/p
    if (dObject.getCustodhist() != null) {
      c.addNewCustodhist().setP(dObject.getCustodhist());
    }

    // acqinfo
    if (dObject.getAcqinfos() != null && dObject.getAcqinfos().getAcqinfos() != null) {

      for (pt.gov.dgarq.roda.core.data.eadc.Acqinfo acqinfo : dObject.getAcqinfos().getAcqinfos()) {
        Acqinfo newAcqinfo = c.addNewAcqinfo();

        // acqinfo/@altrender
        if (acqinfo.getAttributeAltrender() != null) {
          newAcqinfo.addNewAltrender().setStringValue(acqinfo.getAttributeAltrender());
        }

        // acqinfo/p
        if (acqinfo.getP() != null) {
          P p = acqinfo.getP();
          AcqinfoP newP = newAcqinfo.addNewP();

          // INFO must be the first one to be added because we use cursor to add
          // the pcdata (mixed=true)
          // acqinfo/p/text()
          if (p.getText() != null) {
            XmlCursor newCursor = newP.newCursor();
            newCursor.toLastAttribute();
            newCursor.setTextValue(p.getText());
            newCursor.dispose();
          }

          // acqinfo/p/date
          if (p.getDate() != null) {
            newP.addNewDate().setNormal(p.getDate());
          }

          // acqinfo/p/num
          if (p.getNum() != null) {
            newP.setNum(p.getNum());
          }

          // acqinfo/p/corpname
          if (p.getCorpname() != null) {
            newP.setCorpname(p.getCorpname());
          }
        }
      }
    }

    // scopecontent/p
    if (dObject.getScopecontent() != null) {
      c.addNewScopecontent().setP(dObject.getScopecontent());
    }

    // appraisal/p
    if (dObject.getAppraisal() != null) {
      c.addNewAppraisal().setP(dObject.getAppraisal());
    }

    // accruals/p
    if (dObject.getAccruals() != null) {
      c.addNewAccruals().setP(dObject.getAccruals());
    }

    // arrangement
    if (dObject.getArrangement() != null || dObject.getArrangementTable() != null) {

      Arrangement arrangement = c.addNewArrangement();

      // arrangement/p
      if (dObject.getArrangement() != null) {
        arrangement.setP(dObject.getArrangement());
      }

      // arrangement/table
      if (dObject.getArrangementTable() != null && dObject.getArrangementTable().getArrangementTableGroups() != null
        && dObject.getArrangementTable().getArrangementTableGroups().length > 0) {

        Table table = arrangement.addNewTable();

        for (ArrangementTableGroup group : dObject.getArrangementTable().getArrangementTableGroups()) {

          Tgroup tgroup = table.addNewTgroup();
          tgroup.setCols(new BigInteger(new Integer(group.getColumns()).toString()));

          if (group.getHead() != null) {

            Thead thead = tgroup.addNewThead();

            for (ArrangementTableRow atrow : group.getHead().getRows()) {

              Row row = thead.addNewRow();
              for (String entry : atrow.getEntries()) {
                row.addNewEntry().setStringValue(entry);
              }

            }
          }

          if (group.getBody() != null) {
            Tbody tbody = tgroup.addNewTbody();
            for (ArrangementTableRow atrow : group.getBody().getRows()) {

              Row row = tbody.addNewRow();
              for (String entry : atrow.getEntries()) {
                row.addNewEntry().setStringValue(entry);
              }

            }

          }
        }
      } // End of arrangement/table

    } // End of arrangement

    // accessrestrict/p
    if (dObject.getAccessrestrict() != null) {
      c.addNewAccessrestrict().setP(dObject.getAccessrestrict());
    }

    // userestrict/p
    if (dObject.getUserestrict() != null) {
      c.addNewUserestrict().setP(dObject.getUserestrict());
    }

    // phystech/p
    if (dObject.getPhystech() != null) {
      c.addNewPhystech().setP(dObject.getPhystech());
    }

    // otherfindaid/p
    if (dObject.getOtherfindaid() != null) {
      c.addNewOtherfindaid().setP(dObject.getOtherfindaid());
    }

    // relatedmaterial
    if (dObject.getRelatedmaterials() != null && dObject.getRelatedmaterials().getRelatedmaterials() != null) {
      for (Relatedmaterial relatedmaterial : dObject.getRelatedmaterials().getRelatedmaterials()) {
        pt.gov.dgarq.roda.x2014.eadcSchema.Relatedmaterial newRelatedmaterial = c.addNewRelatedmaterial();

        // relatedmaterial/p
        if (relatedmaterial.getP() != null && relatedmaterial.getP().getText() != null) {
          newRelatedmaterial.setP(relatedmaterial.getP().getText());
        }

        // relatedmaterial/archref
        if (relatedmaterial.getArchref() != null) {
          Archref archref = relatedmaterial.getArchref();
          pt.gov.dgarq.roda.x2014.eadcSchema.Archref newArchref = newRelatedmaterial.addNewArchref();

          // relatedmaterial/archref/unitid
          if (archref.getUnitids() != null && archref.getUnitids().length > 0) {
            for (pt.gov.dgarq.roda.core.data.eadc.Unitid id : archref.getUnitids()) {
              UnitidWithOptionalAttributes newUnitid = newArchref.addNewUnitid();

              // relatedmaterial/archref/unitid/@altrender
              if (id.getAttributeAltrender() != null) {
                newUnitid.addNewAltrender().setStringValue(id.getAttributeAltrender());
              }

              // relatedmaterial/archref/unitid/text()
              newUnitid.setStringValue(id.getText());
            }
          }
        }
      }
    }

    // bibliography/p
    if (dObject.getBibliography() != null) {
      c.addNewBibliography().setP(dObject.getBibliography());
    }

    // note
    if (dObject.getNotes() != null && dObject.getNotes().getNotes() != null) {
      for (Note note : dObject.getNotes().getNotes()) {
        pt.gov.dgarq.roda.x2014.eadcSchema.Note newNote = c.addNewNote();

        // note/@label
        if (note.getAttributeLabel() != null) {
          newNote.addNewLabel().setStringValue(note.getAttributeLabel());
        }

        // note/@altrender
        if (note.getAttributeAltrender() != null) {
          newNote.addNewAltrender().setStringValue(note.getAttributeAltrender());
        }

        // note/p
        if (note.getP() != null && note.getP().getText() != null) {
          newNote.setP(note.getP().getText());
        }

        // note/list
        if (note.getList() != null && note.getList().getItems() != null) {
          pt.gov.dgarq.roda.x2014.eadcSchema.List newList = newNote.addNewList();

          // note/list/item
          for (String item : note.getList().getItems()) {
            newList.addNewItem().setStringValue(item);
          }
        }
      }
    }

    // index
    if (dObject.getIndex() != null && dObject.getIndex().getIndexes() != null) {
      pt.gov.dgarq.roda.x2014.eadcSchema.Index newIndex = c.addNewIndex();

      // index/indexentry
      for (Indexentry indexentry : dObject.getIndex().getIndexes()) {

        // index/indexentry/subject
        if (indexentry.getSubject() != null) {
          newIndex.addNewIndexentry().addNewSubject().setStringValue(indexentry.getSubject());
        }
      }
    }

    // prefercite/p
    if (dObject.getPrefercite() != null) {
      c.addNewPrefercite().setP(dObject.getPrefercite());
    }

  }

  /**
   * Replaces the current EAD-C XML data for the specified fields. This creates
   * a EAD-C XML document with only the mandatory elements.
   * 
   * @param dLevel
   * @param countrycode
   * @param repositorycode
   * @param id
   * @param title
   * @param origination
   * @param scopecontent
   */
  public void setMinimalDescriptionObject(DescriptionLevel dLevel, String countrycode, String repositorycode,
    String id, String title, String origination, String scopecontent) {

    // Replaces the current EAD-C with a new empty <eadc>
    setEadC(C.Factory.newInstance());

    C c = getEadC();

    // @otherlevel
    c.setLevel(Enum.forString(dLevel.getLevel()));

    Did did = c.addNewDid();

    // did/unitid
    Unitid unitid = did.addNewUnitid();
    unitid.setStringValue(id);

    // did/unitid/@repositorycode
    unitid.setRepositorycode(countrycode + "-" + repositorycode);

    // did/unittitle
    did.addNewUnittitle().setStringValue(title);

    // did/origination
    did.addNewOrigination().setStringValue(origination);

    // scopecontent/p
    c.addNewScopecontent().setP(scopecontent);
  }

  /**
   * Saves the current EAD-C document to a byte array.
   * 
   * @return a <code>byte[]</code> with the contents of the EAD-C XML file.
   * 
   * @throws EadCMetadataException
   *           if the EAD-C document is not valid or if something goes wrong
   *           with the serialisation.
   */
  public byte[] saveToByteArray() throws EadCMetadataException {
    return saveToByteArray(true);
  }

  /**
   * Saves the current EAD-C document to a byte array.
   * 
   * @param writeXMLDeclaration
   * 
   * @return a <code>byte[]</code> with the contents of the EAD-C XML file.
   * 
   * @throws EadCMetadataException
   *           if the EAD-C document is not valid or if something goes wrong
   *           with the serialisation.
   */
  public byte[] saveToByteArray(boolean writeXMLDeclaration) throws EadCMetadataException {

    try {

      return MetadataHelperUtility.saveToByteArray(getEadcDocument(), writeXMLDeclaration);

    } catch (MetadataException e) {
      logger.debug(e.getMessage(), e);
      throw new EadCMetadataException(e.getMessage(), e);
    }
  }

  /**
   * Saves the current EAD-C document to a {@link File}.
   * 
   * @param eadcFile
   *          the {@link File}.
   * 
   * @throws EadCMetadataException
   *           if the EAD-C document is not valid or if something goes wrong
   *           with the serialisation.
   * 
   * @throws FileNotFoundException
   *           if the specified {@link File} couldn't be opened.
   * @throws IOException
   *           if {@link FileOutputStream} associated with the {@link File}
   *           couldn't be closed.
   */
  public void saveToFile(File eadcFile) throws EadCMetadataException, FileNotFoundException, IOException {
    try {

      MetadataHelperUtility.saveToFile(getEadcDocument(), eadcFile);

    } catch (MetadataException e) {
      logger.debug(e.getMessage(), e);
      throw new EadCMetadataException(e.getMessage(), e);
    }
  }

  private void setEadC(C eadc) {
    getEadcDocument().setEadC(eadc);
  }

  private ArrangementTableGroup[] readArrangementTableGroups(Table table) {

    ArrangementTableGroup[] tgroups = null;

    if (table != null) {

      List<Tgroup> tgroupList = table.getTgroupList();

      if (tgroupList != null) {

        tgroups = new ArrangementTableGroup[tgroupList.size()];

        for (int i = 0; i < tgroupList.size(); i++) {

          ArrangementTableHead thead = null;
          if (tgroupList.get(i).getThead() != null) {

            List<Row> rowList = tgroupList.get(i).getThead().getRowList();

            ArrangementTableRow[] rows = readArrangementTableRows(rowList);

            thead = new ArrangementTableHead(rows);

          } else {
            // tgroupArray[i].getThead() is null
          }

          ArrangementTableBody tbody = null;
          if (tgroupList.get(i).getTbody() != null) {

            ArrangementTableRow[] rows = readArrangementTableRows(tgroupList.get(i).getTbody().getRowList());

            tbody = new ArrangementTableBody(rows);

          } else {
            // tgroupArray[i].getTbody() is null
          }

          tgroups[i] = new ArrangementTableGroup(tgroupList.get(i).getCols().intValue(), thead, tbody);
        }
      }

    }

    return tgroups;
  }

  private ArrangementTableRow[] readArrangementTableRows(List<Row> rowList) {

    ArrangementTableRow[] rows = null;

    if (rowList != null) {

      rows = new ArrangementTableRow[rowList.size()];

      for (int i = 0; rowList != null && i < rowList.size(); i++) {

        List<String> entryList = rowList.get(i).getEntryList();

        String[] entries = entryList.toArray(new String[entryList.size()]);

        rows[i] = new ArrangementTableRow(entries);
      }

    } else {
      // rowArray is null. no rows
    }

    return rows;
  }

  private String joinDates(String dateInitial, String dateFinal) {

    String normalDates = null;

    if (dateInitial != null || dateFinal != null) {

      if (dateInitial != null && dateFinal != null) {
        normalDates = dateInitial + "/" + dateFinal;
      } else if (dateInitial != null) {
        normalDates = dateInitial;
      } else {
        normalDates = dateFinal;
      }

    } else {
      // no dates!
    }

    return normalDates;
  }
}
