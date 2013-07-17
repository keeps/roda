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
import org.apache.xmlbeans.XmlException;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTable;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableBody;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableGroup;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableHead;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTableRow;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronitem;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.MetadataHelperUtility;
import pt.gov.dgarq.roda.x2008.eadcSchema.Acqinfo;
import pt.gov.dgarq.roda.x2008.eadcSchema.Arrangement;
import pt.gov.dgarq.roda.x2008.eadcSchema.AvLevel.Enum;
import pt.gov.dgarq.roda.x2008.eadcSchema.Bioghist;
import pt.gov.dgarq.roda.x2008.eadcSchema.C;
import pt.gov.dgarq.roda.x2008.eadcSchema.Chronitem;
import pt.gov.dgarq.roda.x2008.eadcSchema.Chronlist;
import pt.gov.dgarq.roda.x2008.eadcSchema.Date;
import pt.gov.dgarq.roda.x2008.eadcSchema.Did;
import pt.gov.dgarq.roda.x2008.eadcSchema.Dimensions;
import pt.gov.dgarq.roda.x2008.eadcSchema.EadCDocument;
import pt.gov.dgarq.roda.x2008.eadcSchema.Extent;
import pt.gov.dgarq.roda.x2008.eadcSchema.Langmaterial;
import pt.gov.dgarq.roda.x2008.eadcSchema.Language;
import pt.gov.dgarq.roda.x2008.eadcSchema.Physdesc;
import pt.gov.dgarq.roda.x2008.eadcSchema.Physfacet;
import pt.gov.dgarq.roda.x2008.eadcSchema.Row;
import pt.gov.dgarq.roda.x2008.eadcSchema.Table;
import pt.gov.dgarq.roda.x2008.eadcSchema.Tbody;
import pt.gov.dgarq.roda.x2008.eadcSchema.Tgroup;
import pt.gov.dgarq.roda.x2008.eadcSchema.Thead;
import pt.gov.dgarq.roda.x2008.eadcSchema.Unitdate;
import pt.gov.dgarq.roda.x2008.eadcSchema.Unitid;

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
	 * Creates a new instance of a {@link EadCHelper} for the EAD-C XML inside
	 * the given {@link File}.
	 * 
	 * @param eadcFile
	 *            the EAD-C XML file.
	 * 
	 * @return a {@link EadCHelper} for the given EAD-C XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws EadCMetadataException
	 *             if the EAD-C XML document is invalid.
	 */
	public static EadCHelper newInstance(File eadcFile)
			throws EadCMetadataException, FileNotFoundException, IOException {
		FileInputStream eadcInputStream = new FileInputStream(eadcFile);
		EadCHelper instance = newInstance(eadcInputStream);
		eadcInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link EadCHelper} for the EAD-C XML inside
	 * the given {@link InputStream}.
	 * 
	 * @param eadcInputStream
	 *            the EAD-C XML {@link InputStream}.
	 * 
	 * @return a {@link EadCHelper} for the given EAD-C XML {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws EadCMetadataException
	 *             if the EAD-C XML document is invalid.
	 */
	public static EadCHelper newInstance(InputStream eadcInputStream)
			throws EadCMetadataException, IOException {

		try {

			EadCDocument document = EadCDocument.Factory.parse(eadcInputStream);
			if (document.validate()) {
				return new EadCHelper(document);
			} else {
				throw new EadCMetadataException("Error validating XML document");
			}

		} catch (XmlException e) {
			logger.debug("Error parsing EAD-C - " + e.getMessage(), e);
			throw new EadCMetadataException("Error parsing EAD-C - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Constructs a new {@link EadCHelper} with a new EAD-C document.
	 */
	public EadCHelper() {
		this(EadCDocument.Factory.newInstance());
	}

	/**
	 * Constructs a new {@link EadCHelper} with a new EAD-C document and sets
	 * the information inside the given {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject}.
	 */
	public EadCHelper(DescriptionObject dObject) {
		this();
		setDescriptionObject(dObject);
	}

	/**
	 * Constructs a new {@link EadCHelper} for the given EAD-C document.
	 * 
	 * @param eadcDocument
	 *            the EAD-C document.
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
		return getSimpleDescriptionObject(new SimpleDescriptionObject(), null,
				0);

	}

	/**
	 * Gets a {@link SimpleDescriptionObject} from the current
	 * {@link EadCDocument}.
	 * 
	 * @param rodaObject
	 *            the {@link RODAObject} that should be returned as a
	 *            {@link SimpleDescriptionObject}.
	 * @param subElementsCount
	 *            the number of sub-elements for the
	 *            {@link SimpleDescriptionObject}.
	 * 
	 * @return a {@link SimpleDescriptionObject}.
	 * 
	 * @throws InvalidDescriptionLevel
	 *             if the level being set is not one of
	 *             <ul>
	 *             <li>{@link DescriptionLevel#FONDS},</li>
	 *             <li>{@link DescriptionLevel#SUBFONDS},</li>
	 *             <li>{@link DescriptionLevel#CLASS},</li>
	 *             <li>{@link DescriptionLevel#SUBCLASS},</li>
	 *             <li>{@link DescriptionLevel#SERIES},</li>
	 *             <li>{@link DescriptionLevel#SUBSERIES},</li>
	 *             <li>{@link DescriptionLevel#FILE},</li>
	 *             <li>{@link DescriptionLevel#ITEM}.</li>
	 *             </ul>
	 */
	public SimpleDescriptionObject getSimpleDescriptionObject(
			RODAObject rodaObject, String parentPID, int subElementsCount)
			throws InvalidDescriptionLevel {

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

			// did/unitid/@repositorycode
			if (StringUtils.isNotBlank(did.getUnitid().getRepositorycode())) {

				String repositoryCode = did.getUnitid().getRepositorycode();

				int indexOfDivider = repositoryCode.indexOf("-");
				if (indexOfDivider >= 0) {

					sdo.setCountryCode(repositoryCode.substring(0,
							indexOfDivider));
					if (indexOfDivider + 1 < repositoryCode.length()) {
						sdo.setRepositoryCode(repositoryCode
								.substring(indexOfDivider + 1));
					}

				} else {
					logger.warn("Invalid countryRepositoryCode '"
							+ did.getUnitid().getRepositorycode() + "'");
				}
			}

			// did/unitid
			sdo.setId(did.getUnitid().getStringValue());

			// did/unittitle
			sdo.setTitle(did.getUnittitle());

			// did/unitdate
			Unitdate unitdate = did.getUnitdate();
			if (unitdate != null) {

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

		// scopecontent/p
		if (c.getScopecontent() != null) {
			description = c.getScopecontent().getP();
		}

		// bioghist
		Bioghist bioghist = c.getBioghist();
		if (bioghist != null) {
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
	 *            the {@link RODAObject} that should be returned as a
	 *            {@link DescriptionObject}.
	 * @param subElementsCount
	 *            the number of sub-elements for the {@link DescriptionObject}.
	 * 
	 * @return a {@link DescriptionObject}.
	 */
	public DescriptionObject getDescriptionObject(RODAObject rodaObject,
			String parentPID, int subElementsCount) {

		return getDescriptionObject(getSimpleDescriptionObject(rodaObject,
				parentPID, subElementsCount));
	}

	/**
	 * Gets a {@link DescriptionObject} from the current {@link EadCDocument}.
	 * 
	 * @param simpleDO
	 *            the {@link SimpleDescriptionObject} that should be returned as
	 *            a {@link DescriptionObject}.
	 * 
	 * @return a {@link DescriptionObject}.
	 */
	public DescriptionObject getDescriptionObject(
			SimpleDescriptionObject simpleDO) {

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

			// did/physdesc
			Physdesc physdesc = did.getPhysdesc();
			if (physdesc != null) {

				// did/physdesc/p
				if (physdesc.getP() != null) {
					dObject.setPhysdesc(physdesc.getP());
				}

				// did/physdesc/dimensions
				Dimensions dimensions = physdesc.getDimensions();
				if (dimensions != null) {

					// did/physdesc/dimensions/@unit
					dObject.setPhysdescDimensions(new PhysdescElement(
							dimensions.getStringValue(), dimensions.getUnit()));
				}

				// did/physdesc/physfacet
				Physfacet physfacet = physdesc.getPhysfacet();
				if (physfacet != null) {
					// did/physdesc/physfacet/@unit
					dObject.setPhysdescPhysfacet(new PhysdescElement(physfacet
							.getStringValue(), physfacet.getUnit()));
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
					dObject.setPhysdescExtent(new PhysdescElement(extent
							.getStringValue(), extent.getUnit()));
				}

			} // End of did/physdesc

			// did/materialspec
			if (did.getMaterialspec() != null) {
				dObject.setMaterialspec(did.getMaterialspec());

			}

			// did/origination
			if (did.getOrigination() != null) {
				dObject.setOrigination(did.getOrigination());
			}

			// did/langmaterial
			Langmaterial langmaterial = did.getLangmaterial();
			if (langmaterial != null) {

				List<Language> languageList = langmaterial.getLanguageList();

				String[] languages = null;

				if (languageList != null) {

					languages = new String[languageList.size()];

					for (int i = 0; i < languageList.size(); i++) {
						languages[i] = languageList.get(i).getStringValue();
					}
				}

				dObject.setLangmaterialLanguages(new LangmaterialLanguages(
						languages));
			}

		} // End of did

		// bioghist
		Bioghist bioghist = c.getBioghist();
		if (bioghist != null) {

			// bioghist/p
			if (bioghist.getP() != null) {
				dObject.setBioghist(bioghist.getP());
			}

			// bioghist/chronlist
			Chronlist chronlist = bioghist.getChronlist();
			if (chronlist != null) {

				List<Chronitem> chronitemList = chronlist.getChronitemList();

				BioghistChronitem[] chronitems = new BioghistChronitem[chronitemList
						.size()];

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

					chronitems[index] = new BioghistChronitem(
							chronitem.getEvent(), dateInitial, dateFinal);

					index++;
				}

				dObject.setBioghistChronlist(new BioghistChronlist(chronitems));
			}
		} // End of bioghist

		// custodhist/p
		if (c.getCustodhist() != null) {
			dObject.setCustodhist(c.getCustodhist().getP());
		}

		// acqinfo
		if (c.getAcqinfo() != null) {

			// acqinfo/p
			dObject.setAcqinfo(c.getAcqinfo().getP());

			// acqinfo/date
			Date date = c.getAcqinfo().getDate();
			if (date != null) {

				String normalDate = date.getNormal();
				if (normalDate != null) {
					String[] dates = normalDate.split("/");
					if (dates.length > 0) {
						// dateInitial
						dObject.setAcqinfoDate(dates[0]);
						if (dates.length > 1) {
							// dateFinal
							logger.warn("Acqinfo date has more than 1 value");
						}
					}
				}
			}

			// acqinfo/num
			dObject.setAcqinfoNum(c.getAcqinfo().getNum());
		}

		// scopecontent/p
		if (c.getScopecontent() != null) {
			dObject.setScopecontent(c.getScopecontent().getP());
		}

		// appraisal/p
		if (c.getAppraisal() != null) {
			dObject.setAppraisal(c.getAppraisal().getP());
		}

		// accruals/p
		if (c.getAccruals() != null) {
			dObject.setAccruals(c.getAccruals().getP());
		}

		// arrangement
		Arrangement arrangement = c.getArrangement();
		if (arrangement != null) {

			// arrangement/p
			if (arrangement.getP() != null) {
				dObject.setArrangement(arrangement.getP());
			}

			// arrangement/table
			dObject.setArrangementTable(new ArrangementTable(
					readArrangementTableGroups(arrangement.getTable())));

		} // End of arrangement

		// accessrestrict/p
		if (c.getAccessrestrict() != null) {
			dObject.setAccessrestrict(c.getAccessrestrict().getP());
		}

		// userestrict/p
		if (c.getUserestrict() != null) {
			dObject.setUserestrict(c.getUserestrict().getP());
		}

		// phystech/p
		if (c.getPhystech() != null) {
			dObject.setPhystech(c.getPhystech().getP());

		}

		// otherfindaid/p
		if (c.getOtherfindaid() != null) {
			dObject.setOtherfindaid(c.getOtherfindaid().getP());
		}

		// relatedmaterial/p
		if (c.getRelatedmaterial() != null) {
			dObject.setRelatedmaterial(c.getRelatedmaterial().getP());
		}

		// bibliography/p
		if (c.getBibliography() != null) {
			dObject.setBibliography(c.getBibliography().getP());
		}

		// note/p
		if (c.getNote() != null) {
			dObject.setNote(c.getNote().getP());
		}

		// prefercite/p
		if (c.getPrefercite() != null) {
			dObject.setPrefercite(c.getPrefercite().getP());
		}

		return dObject;
	}

	/**
	 * Replaces the current EAD-C XML data for the data inside the given
	 * {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject}.
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
		did.setUnittitle(dObject.getTitle());
		// if (dObject.getTitle() != null) {
		// did.setUnittitle(dObject.getTitle());
		// }

		// did/unitdate
		String joinDates2 = joinDates(dObject.getDateInitial(),
				dObject.getDateFinal());
		if (joinDates2 != null) {
			did.addNewUnitdate().setNormal(joinDates2);
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
			dimensions.setStringValue(dObject.getPhysdescDimensions()
					.getValue());

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
		String joinDates = joinDates(dObject.getPhysdescDateInitial(),
				dObject.getPhysdescDateFinal());
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

		// did/materialspec
		if (dObject.getMaterialspec() != null) {
			did.setMaterialspec(dObject.getMaterialspec());
		}

		// did/origination
		if (dObject.getOrigination() != null) {
			did.setOrigination(dObject.getOrigination());
		}

		// did/langmaterial
		if (dObject.getLangmaterialLanguages() != null
				&& dObject.getLangmaterialLanguages()
						.getLangmaterialLanguages() != null
				&& dObject.getLangmaterialLanguages()
						.getLangmaterialLanguages().length > 0) {

			Langmaterial langmaterial = did.addNewLangmaterial();

			for (String language : dObject.getLangmaterialLanguages()
					.getLangmaterialLanguages()) {
				langmaterial.addNewLanguage().setStringValue(language);
			}

		}
		// End of did

		// bioghist
		if (dObject.getBioghist() != null
				|| dObject.getBioghistChronlist() != null) {

			Bioghist bioghist = c.addNewBioghist();

			// bioghist/p
			if (dObject.getBioghist() != null) {
				bioghist.setP(dObject.getBioghist());
			}

			// bioghist/chronlist
			if (dObject.getBioghistChronlist() != null
					&& dObject.getBioghistChronlist().getBioghistChronitems() != null
					&& dObject.getBioghistChronlist().getBioghistChronitems().length > 0) {

				Chronlist chronlist = bioghist.addNewChronlist();

				for (BioghistChronitem item : dObject.getBioghistChronlist()
						.getBioghistChronitems()) {

					Chronitem chronitem = chronlist.addNewChronitem();
					chronitem.addNewDate().setNormal(
							joinDates(item.getDateInitial(),
									item.getDateFinal()));
					chronitem.setEvent(item.getEvent());
				}
			} // End of bioghist/chronlist

		} // End of bioghist

		// custodhist/p
		if (dObject.getCustodhist() != null) {
			c.addNewCustodhist().setP(dObject.getCustodhist());
		}

		// acqinfo
		if (dObject.getAcqinfo() != null || dObject.getAcqinfoNum() != null
				|| dObject.getAcqinfoDate() != null) {
			Acqinfo acqinfo = c.addNewAcqinfo();

			// acqinfo/p
			if (dObject.getAcqinfo() != null) {
				acqinfo.setP(dObject.getAcqinfo());
			}

			// acqinfo/date
			if (dObject.getAcqinfoDate() != null) {
				Date acqinfoDate = acqinfo.addNewDate();
				acqinfoDate.setNormal(dObject.getAcqinfoDate());
			}

			// acqinfo/num
			if (dObject.getAcqinfoNum() != null) {
				acqinfo.setNum(dObject.getAcqinfoNum());
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
		if (dObject.getArrangement() != null
				|| dObject.getArrangementTable() != null) {

			Arrangement arrangement = c.addNewArrangement();

			// arrangement/p
			if (dObject.getArrangement() != null) {
				arrangement.setP(dObject.getArrangement());
			}

			// arrangement/table
			if (dObject.getArrangementTable() != null
					&& dObject.getArrangementTable()
							.getArrangementTableGroups() != null
					&& dObject.getArrangementTable()
							.getArrangementTableGroups().length > 0) {

				Table table = arrangement.addNewTable();

				for (ArrangementTableGroup group : dObject
						.getArrangementTable().getArrangementTableGroups()) {

					Tgroup tgroup = table.addNewTgroup();
					tgroup.setCols(new BigInteger(new Integer(group
							.getColumns()).toString()));

					if (group.getHead() != null) {

						Thead thead = tgroup.addNewThead();

						for (ArrangementTableRow atrow : group.getHead()
								.getRows()) {

							Row row = thead.addNewRow();
							for (String entry : atrow.getEntries()) {
								row.addNewEntry().setStringValue(entry);
							}

						}
					}

					if (group.getBody() != null) {
						Tbody tbody = tgroup.addNewTbody();
						for (ArrangementTableRow atrow : group.getBody()
								.getRows()) {

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

		// relatedmaterial/p
		if (dObject.getRelatedmaterial() != null) {
			c.addNewRelatedmaterial().setP(dObject.getRelatedmaterial());
		}

		// bibliography/p
		if (dObject.getBibliography() != null) {
			c.addNewBibliography().setP(dObject.getBibliography());
		}

		// note/P
		if (dObject.getNote() != null) {
			c.addNewNote().setP(dObject.getNote());
		}

		// prefercite/p
		if (dObject.getPrefercite() != null) {
			c.addNewPrefercite().setP(dObject.getPrefercite());
		}

	}

	/**
	 * Replaces the current EAD-C XML data for the specified fields. This
	 * creates a EAD-C XML document with only the mandatory elements.
	 * 
	 * @param dLevel
	 * @param countrycode
	 * @param repositorycode
	 * @param id
	 * @param title
	 * @param origination
	 * @param scopecontent
	 */
	public void setMinimalDescriptionObject(DescriptionLevel dLevel,
			String countrycode, String repositorycode, String id, String title,
			String origination, String scopecontent) {

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
		did.setUnittitle(title);

		// did/origination
		did.setOrigination(origination);

		// scopecontent/p
		c.addNewScopecontent().setP(scopecontent);
	}

	/**
	 * Saves the current EAD-C document to a byte array.
	 * 
	 * @return a <code>byte[]</code> with the contents of the EAD-C XML file.
	 * 
	 * @throws EadCMetadataException
	 *             if the EAD-C document is not valid or if something goes wrong
	 *             with the serialisation.
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
	 *             if the EAD-C document is not valid or if something goes wrong
	 *             with the serialisation.
	 */
	public byte[] saveToByteArray(boolean writeXMLDeclaration)
			throws EadCMetadataException {

		try {

			return MetadataHelperUtility.saveToByteArray(getEadcDocument(),
					writeXMLDeclaration);

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new EadCMetadataException(e.getMessage(), e);
		}
	}

	/**
	 * Saves the current EAD-C document to a {@link File}.
	 * 
	 * @param eadcFile
	 *            the {@link File}.
	 * 
	 * @throws EadCMetadataException
	 *             if the EAD-C document is not valid or if something goes wrong
	 *             with the serialisation.
	 * 
	 * @throws FileNotFoundException
	 *             if the specified {@link File} couldn't be opened.
	 * @throws IOException
	 *             if {@link FileOutputStream} associated with the {@link File}
	 *             couldn't be closed.
	 */
	public void saveToFile(File eadcFile) throws EadCMetadataException,
			FileNotFoundException, IOException {
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

						List<Row> rowList = tgroupList.get(i).getThead()
								.getRowList();

						ArrangementTableRow[] rows = readArrangementTableRows(rowList);

						thead = new ArrangementTableHead(rows);

					} else {
						// tgroupArray[i].getThead() is null
					}

					ArrangementTableBody tbody = null;
					if (tgroupList.get(i).getTbody() != null) {

						ArrangementTableRow[] rows = readArrangementTableRows(tgroupList
								.get(i).getTbody().getRowList());

						tbody = new ArrangementTableBody(rows);

					} else {
						// tgroupArray[i].getTbody() is null
					}

					tgroups[i] = new ArrangementTableGroup(tgroupList.get(i)
							.getCols().intValue(), thead, tbody);
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

				String[] entries = entryList.toArray(new String[entryList
						.size()]);

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
