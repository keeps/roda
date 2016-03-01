/*
 * XML Type:  linkingEnvironmentIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML linkingEnvironmentIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LinkingEnvironmentIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEnvironmentIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentIdentifierType");
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentIdentifierValue");
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentRole");
    private static final javax.xml.namespace.QName LINKEVENTXMLID$6 = 
        new javax.xml.namespace.QName("", "LinkEventXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$8 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "linkingEnvironmentIdentifierType" element
     */
    public java.lang.String getLinkingEnvironmentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingEnvironmentIdentifierType" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingEnvironmentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingEnvironmentIdentifierType" element
     */
    public void setLinkingEnvironmentIdentifierType(java.lang.String linkingEnvironmentIdentifierType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0);
            }
            target.setStringValue(linkingEnvironmentIdentifierType);
        }
    }
    
    /**
     * Sets (as xml) the "linkingEnvironmentIdentifierType" element
     */
    public void xsetLinkingEnvironmentIdentifierType(org.apache.xmlbeans.XmlString linkingEnvironmentIdentifierType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0);
            }
            target.set(linkingEnvironmentIdentifierType);
        }
    }
    
    /**
     * Gets the "linkingEnvironmentIdentifierValue" element
     */
    public java.lang.String getLinkingEnvironmentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingEnvironmentIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingEnvironmentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingEnvironmentIdentifierValue" element
     */
    public void setLinkingEnvironmentIdentifierValue(java.lang.String linkingEnvironmentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$2);
            }
            target.setStringValue(linkingEnvironmentIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingEnvironmentIdentifierValue" element
     */
    public void xsetLinkingEnvironmentIdentifierValue(org.apache.xmlbeans.XmlString linkingEnvironmentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$2);
            }
            target.set(linkingEnvironmentIdentifierValue);
        }
    }
    
    /**
     * Gets array of all "linkingEnvironmentRole" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getLinkingEnvironmentRoleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGENVIRONMENTROLE$4, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingEnvironmentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingEnvironmentRoleArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGENVIRONMENTROLE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingEnvironmentRole" element
     */
    public int sizeOfLinkingEnvironmentRoleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGENVIRONMENTROLE$4);
        }
    }
    
    /**
     * Sets array of all "linkingEnvironmentRole" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingEnvironmentRoleArray(gov.loc.premis.v3.StringPlusAuthority[] linkingEnvironmentRoleArray)
    {
        check_orphaned();
        arraySetterHelper(linkingEnvironmentRoleArray, LINKINGENVIRONMENTROLE$4);
    }
    
    /**
     * Sets ith "linkingEnvironmentRole" element
     */
    public void setLinkingEnvironmentRoleArray(int i, gov.loc.premis.v3.StringPlusAuthority linkingEnvironmentRole)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGENVIRONMENTROLE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingEnvironmentRole);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEnvironmentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewLinkingEnvironmentRole(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(LINKINGENVIRONMENTROLE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEnvironmentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingEnvironmentRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGENVIRONMENTROLE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingEnvironmentRole" element
     */
    public void removeLinkingEnvironmentRole(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGENVIRONMENTROLE$4, i);
        }
    }
    
    /**
     * Gets the "LinkEventXmlID" attribute
     */
    public java.lang.String getLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKEVENTXMLID$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "LinkEventXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKEVENTXMLID$6);
            return target;
        }
    }
    
    /**
     * True if has "LinkEventXmlID" attribute
     */
    public boolean isSetLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(LINKEVENTXMLID$6) != null;
        }
    }
    
    /**
     * Sets the "LinkEventXmlID" attribute
     */
    public void setLinkEventXmlID(java.lang.String linkEventXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKEVENTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LINKEVENTXMLID$6);
            }
            target.setStringValue(linkEventXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "LinkEventXmlID" attribute
     */
    public void xsetLinkEventXmlID(org.apache.xmlbeans.XmlIDREF linkEventXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKEVENTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(LINKEVENTXMLID$6);
            }
            target.set(linkEventXmlID);
        }
    }
    
    /**
     * Unsets the "LinkEventXmlID" attribute
     */
    public void unsetLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(LINKEVENTXMLID$6);
        }
    }
    
    /**
     * Gets the "simpleLink" attribute
     */
    public java.lang.String getSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "simpleLink" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$8);
            return target;
        }
    }
    
    /**
     * True if has "simpleLink" attribute
     */
    public boolean isSetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(SIMPLELINK$8) != null;
        }
    }
    
    /**
     * Sets the "simpleLink" attribute
     */
    public void setSimpleLink(java.lang.String simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$8);
            }
            target.setStringValue(simpleLink);
        }
    }
    
    /**
     * Sets (as xml) the "simpleLink" attribute
     */
    public void xsetSimpleLink(org.apache.xmlbeans.XmlAnyURI simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$8);
            }
            target.set(simpleLink);
        }
    }
    
    /**
     * Unsets the "simpleLink" attribute
     */
    public void unsetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(SIMPLELINK$8);
        }
    }
}
