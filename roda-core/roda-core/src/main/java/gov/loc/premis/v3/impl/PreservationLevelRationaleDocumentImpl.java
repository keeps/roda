/*
 * An XML document type.
 * Localname: preservationLevelRationale
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelRationaleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one preservationLevelRationale(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PreservationLevelRationaleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelRationaleDocument
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelRationaleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVELRATIONALE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelRationale");
    
    
    /**
     * Gets the "preservationLevelRationale" element
     */
    public java.lang.String getPreservationLevelRationale()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "preservationLevelRationale" element
     */
    public org.apache.xmlbeans.XmlString xgetPreservationLevelRationale()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevelRationale" element
     */
    public void setPreservationLevelRationale(java.lang.String preservationLevelRationale)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PRESERVATIONLEVELRATIONALE$0);
            }
            target.setStringValue(preservationLevelRationale);
        }
    }
    
    /**
     * Sets (as xml) the "preservationLevelRationale" element
     */
    public void xsetPreservationLevelRationale(org.apache.xmlbeans.XmlString preservationLevelRationale)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(PRESERVATIONLEVELRATIONALE$0);
            }
            target.set(preservationLevelRationale);
        }
    }
}
