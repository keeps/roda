/*
 * An XML document type.
 * Localname: preservationLevelDateAssigned
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelDateAssignedDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one preservationLevelDateAssigned(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PreservationLevelDateAssignedDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelDateAssignedDocument
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelDateAssignedDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVELDATEASSIGNED$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelDateAssigned");
    
    
    /**
     * Gets the "preservationLevelDateAssigned" element
     */
    public java.lang.String getPreservationLevelDateAssigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "preservationLevelDateAssigned" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetPreservationLevelDateAssigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevelDateAssigned" element
     */
    public void setPreservationLevelDateAssigned(java.lang.String preservationLevelDateAssigned)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PRESERVATIONLEVELDATEASSIGNED$0);
            }
            target.setStringValue(preservationLevelDateAssigned);
        }
    }
    
    /**
     * Sets (as xml) the "preservationLevelDateAssigned" element
     */
    public void xsetPreservationLevelDateAssigned(gov.loc.premis.v3.EdtfSimpleType preservationLevelDateAssigned)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$0, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(PRESERVATIONLEVELDATEASSIGNED$0);
            }
            target.set(preservationLevelDateAssigned);
        }
    }
}
