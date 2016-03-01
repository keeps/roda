/*
 * An XML document type.
 * Localname: significantPropertiesValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignificantPropertiesValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one significantPropertiesValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignificantPropertiesValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignificantPropertiesValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignificantPropertiesValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIESVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantPropertiesValue");
    
    
    /**
     * Gets the "significantPropertiesValue" element
     */
    public java.lang.String getSignificantPropertiesValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "significantPropertiesValue" element
     */
    public org.apache.xmlbeans.XmlString xgetSignificantPropertiesValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "significantPropertiesValue" element
     */
    public void setSignificantPropertiesValue(java.lang.String significantPropertiesValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNIFICANTPROPERTIESVALUE$0);
            }
            target.setStringValue(significantPropertiesValue);
        }
    }
    
    /**
     * Sets (as xml) the "significantPropertiesValue" element
     */
    public void xsetSignificantPropertiesValue(org.apache.xmlbeans.XmlString significantPropertiesValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SIGNIFICANTPROPERTIESVALUE$0);
            }
            target.set(significantPropertiesValue);
        }
    }
}
