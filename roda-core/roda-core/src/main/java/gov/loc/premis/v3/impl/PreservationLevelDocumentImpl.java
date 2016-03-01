/*
 * An XML document type.
 * Localname: preservationLevel
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one preservationLevel(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PreservationLevelDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelDocument
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVEL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevel");
    
    
    /**
     * Gets the "preservationLevel" element
     */
    public gov.loc.premis.v3.PreservationLevelComplexType getPreservationLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PreservationLevelComplexType target = null;
            target = (gov.loc.premis.v3.PreservationLevelComplexType)get_store().find_element_user(PRESERVATIONLEVEL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevel" element
     */
    public void setPreservationLevel(gov.loc.premis.v3.PreservationLevelComplexType preservationLevel)
    {
        generatedSetterHelperImpl(preservationLevel, PRESERVATIONLEVEL$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevel" element
     */
    public gov.loc.premis.v3.PreservationLevelComplexType addNewPreservationLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PreservationLevelComplexType target = null;
            target = (gov.loc.premis.v3.PreservationLevelComplexType)get_store().add_element_user(PRESERVATIONLEVEL$0);
            return target;
        }
    }
}
