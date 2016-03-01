/*
 * An XML document type.
 * Localname: preservationLevelType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one preservationLevelType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PreservationLevelTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVELTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelType");
    
    
    /**
     * Gets the "preservationLevelType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getPreservationLevelType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(PRESERVATIONLEVELTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevelType" element
     */
    public void setPreservationLevelType(gov.loc.premis.v3.StringPlusAuthority preservationLevelType)
    {
        generatedSetterHelperImpl(preservationLevelType, PRESERVATIONLEVELTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevelType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(PRESERVATIONLEVELTYPE$0);
            return target;
        }
    }
}
