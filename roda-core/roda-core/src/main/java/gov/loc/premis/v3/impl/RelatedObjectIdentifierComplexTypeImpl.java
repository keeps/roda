/*
 * XML Type:  relatedObjectIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedObjectIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML relatedObjectIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RelatedObjectIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedObjectIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RelatedObjectIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDOBJECTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectIdentifierType");
    private static final javax.xml.namespace.QName RELATEDOBJECTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectIdentifierValue");
    private static final javax.xml.namespace.QName RELATEDOBJECTSEQUENCE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectSequence");
    private static final javax.xml.namespace.QName RELOBJECTXMLID$6 = 
        new javax.xml.namespace.QName("", "RelObjectXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$8 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "relatedObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDOBJECTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedObjectIdentifierType" element
     */
    public void setRelatedObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority relatedObjectIdentifierType)
    {
        generatedSetterHelperImpl(relatedObjectIdentifierType, RELATEDOBJECTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDOBJECTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "relatedObjectIdentifierValue" element
     */
    public java.lang.String getRelatedObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedObjectIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetRelatedObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "relatedObjectIdentifierValue" element
     */
    public void setRelatedObjectIdentifierValue(java.lang.String relatedObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDOBJECTIDENTIFIERVALUE$2);
            }
            target.setStringValue(relatedObjectIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "relatedObjectIdentifierValue" element
     */
    public void xsetRelatedObjectIdentifierValue(org.apache.xmlbeans.XmlString relatedObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RELATEDOBJECTIDENTIFIERVALUE$2);
            }
            target.set(relatedObjectIdentifierValue);
        }
    }
    
    /**
     * Gets the "relatedObjectSequence" element
     */
    public java.math.BigInteger getRelatedObjectSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTSEQUENCE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedObjectSequence" element
     */
    public org.apache.xmlbeans.XmlNonNegativeInteger xgetRelatedObjectSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDOBJECTSEQUENCE$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "relatedObjectSequence" element
     */
    public boolean isSetRelatedObjectSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATEDOBJECTSEQUENCE$4) != 0;
        }
    }
    
    /**
     * Sets the "relatedObjectSequence" element
     */
    public void setRelatedObjectSequence(java.math.BigInteger relatedObjectSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTSEQUENCE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDOBJECTSEQUENCE$4);
            }
            target.setBigIntegerValue(relatedObjectSequence);
        }
    }
    
    /**
     * Sets (as xml) the "relatedObjectSequence" element
     */
    public void xsetRelatedObjectSequence(org.apache.xmlbeans.XmlNonNegativeInteger relatedObjectSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDOBJECTSEQUENCE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().add_element_user(RELATEDOBJECTSEQUENCE$4);
            }
            target.set(relatedObjectSequence);
        }
    }
    
    /**
     * Unsets the "relatedObjectSequence" element
     */
    public void unsetRelatedObjectSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATEDOBJECTSEQUENCE$4, 0);
        }
    }
    
    /**
     * Gets the "RelObjectXmlID" attribute
     */
    public java.lang.String getRelObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(RELOBJECTXMLID$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "RelObjectXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetRelObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(RELOBJECTXMLID$6);
            return target;
        }
    }
    
    /**
     * True if has "RelObjectXmlID" attribute
     */
    public boolean isSetRelObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(RELOBJECTXMLID$6) != null;
        }
    }
    
    /**
     * Sets the "RelObjectXmlID" attribute
     */
    public void setRelObjectXmlID(java.lang.String relObjectXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(RELOBJECTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(RELOBJECTXMLID$6);
            }
            target.setStringValue(relObjectXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "RelObjectXmlID" attribute
     */
    public void xsetRelObjectXmlID(org.apache.xmlbeans.XmlIDREF relObjectXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(RELOBJECTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(RELOBJECTXMLID$6);
            }
            target.set(relObjectXmlID);
        }
    }
    
    /**
     * Unsets the "RelObjectXmlID" attribute
     */
    public void unsetRelObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(RELOBJECTXMLID$6);
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
