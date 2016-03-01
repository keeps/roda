/*
 * An XML document type.
 * Localname: statuteCitation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteCitationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteCitation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteCitationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteCitationDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteCitationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTECITATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteCitation");
    
    
    /**
     * Gets the "statuteCitation" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStatuteCitation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STATUTECITATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteCitation" element
     */
    public void setStatuteCitation(gov.loc.premis.v3.StringPlusAuthority statuteCitation)
    {
        generatedSetterHelperImpl(statuteCitation, STATUTECITATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteCitation" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewStatuteCitation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STATUTECITATION$0);
            return target;
        }
    }
}
