/*
 * An XML document type.
 * Localname: objectCharacteristics
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectCharacteristicsDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one objectCharacteristics(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ObjectCharacteristicsDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectCharacteristicsDocument
{
    private static final long serialVersionUID = 1L;
    
    public ObjectCharacteristicsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTCHARACTERISTICS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectCharacteristics");
    
    
    /**
     * Gets the "objectCharacteristics" element
     */
    public gov.loc.premis.v3.ObjectCharacteristicsComplexType getObjectCharacteristics()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectCharacteristicsComplexType target = null;
            target = (gov.loc.premis.v3.ObjectCharacteristicsComplexType)get_store().find_element_user(OBJECTCHARACTERISTICS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "objectCharacteristics" element
     */
    public void setObjectCharacteristics(gov.loc.premis.v3.ObjectCharacteristicsComplexType objectCharacteristics)
    {
        generatedSetterHelperImpl(objectCharacteristics, OBJECTCHARACTERISTICS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "objectCharacteristics" element
     */
    public gov.loc.premis.v3.ObjectCharacteristicsComplexType addNewObjectCharacteristics()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectCharacteristicsComplexType target = null;
            target = (gov.loc.premis.v3.ObjectCharacteristicsComplexType)get_store().add_element_user(OBJECTCHARACTERISTICS$0);
            return target;
        }
    }
}
