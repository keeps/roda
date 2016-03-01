/*
 * XML Type:  otherRightsDocumentationIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML otherRightsDocumentationIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class OtherRightsDocumentationIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsDocumentationIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationIdentifierType");
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationIdentifierValue");
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationRole");
    
    
    /**
     * Gets the "otherRightsDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getOtherRightsDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationIdentifierType" element
     */
    public void setOtherRightsDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority otherRightsDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(otherRightsDocumentationIdentifierType, OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "otherRightsDocumentationIdentifierValue" element
     */
    public java.lang.String getOtherRightsDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "otherRightsDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetOtherRightsDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationIdentifierValue" element
     */
    public void setOtherRightsDocumentationIdentifierValue(java.lang.String otherRightsDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.setStringValue(otherRightsDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "otherRightsDocumentationIdentifierValue" element
     */
    public void xsetOtherRightsDocumentationIdentifierValue(org.apache.xmlbeans.XmlString otherRightsDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.set(otherRightsDocumentationIdentifierValue);
        }
    }
    
    /**
     * Gets the "otherRightsDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getOtherRightsDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "otherRightsDocumentationRole" element
     */
    public boolean isSetOtherRightsDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OTHERRIGHTSDOCUMENTATIONROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationRole" element
     */
    public void setOtherRightsDocumentationRole(gov.loc.premis.v3.StringPlusAuthority otherRightsDocumentationRole)
    {
        generatedSetterHelperImpl(otherRightsDocumentationRole, OTHERRIGHTSDOCUMENTATIONROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "otherRightsDocumentationRole" element
     */
    public void unsetOtherRightsDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OTHERRIGHTSDOCUMENTATIONROLE$4, 0);
        }
    }
}
