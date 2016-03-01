/*
 * XML Type:  copyrightDocumentationIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML copyrightDocumentationIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class CopyrightDocumentationIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightDocumentationIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationIdentifierType");
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationIdentifierValue");
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationRole");
    
    
    /**
     * Gets the "copyrightDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCopyrightDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationIdentifierType" element
     */
    public void setCopyrightDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority copyrightDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(copyrightDocumentationIdentifierType, COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCopyrightDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "copyrightDocumentationIdentifierValue" element
     */
    public java.lang.String getCopyrightDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "copyrightDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetCopyrightDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationIdentifierValue" element
     */
    public void setCopyrightDocumentationIdentifierValue(java.lang.String copyrightDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.setStringValue(copyrightDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "copyrightDocumentationIdentifierValue" element
     */
    public void xsetCopyrightDocumentationIdentifierValue(org.apache.xmlbeans.XmlString copyrightDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.set(copyrightDocumentationIdentifierValue);
        }
    }
    
    /**
     * Gets the "copyrightDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCopyrightDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(COPYRIGHTDOCUMENTATIONROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "copyrightDocumentationRole" element
     */
    public boolean isSetCopyrightDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COPYRIGHTDOCUMENTATIONROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationRole" element
     */
    public void setCopyrightDocumentationRole(gov.loc.premis.v3.StringPlusAuthority copyrightDocumentationRole)
    {
        generatedSetterHelperImpl(copyrightDocumentationRole, COPYRIGHTDOCUMENTATIONROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCopyrightDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(COPYRIGHTDOCUMENTATIONROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "copyrightDocumentationRole" element
     */
    public void unsetCopyrightDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COPYRIGHTDOCUMENTATIONROLE$4, 0);
        }
    }
}
