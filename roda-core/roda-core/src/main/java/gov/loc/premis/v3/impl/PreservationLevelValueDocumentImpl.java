/*
 * An XML document type.
 * Localname: preservationLevelValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one preservationLevelValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PreservationLevelValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVELVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelValue");
    
    
    /**
     * Gets the "preservationLevelValue" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getPreservationLevelValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(PRESERVATIONLEVELVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevelValue" element
     */
    public void setPreservationLevelValue(gov.loc.premis.v3.StringPlusAuthority preservationLevelValue)
    {
        generatedSetterHelperImpl(preservationLevelValue, PRESERVATIONLEVELVALUE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevelValue" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(PRESERVATIONLEVELVALUE$0);
            return target;
        }
    }
}
