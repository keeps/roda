/*
 * An XML document type.
 * Localname: environmentDesignationNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentDesignationNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentDesignationNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentDesignationNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentDesignationNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentDesignationNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTDESIGNATIONNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentDesignationNote");
    
    
    /**
     * Gets the "environmentDesignationNote" element
     */
    public java.lang.String getEnvironmentDesignationNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentDesignationNote" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentDesignationNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentDesignationNote" element
     */
    public void setEnvironmentDesignationNote(java.lang.String environmentDesignationNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTDESIGNATIONNOTE$0);
            }
            target.setStringValue(environmentDesignationNote);
        }
    }
    
    /**
     * Sets (as xml) the "environmentDesignationNote" element
     */
    public void xsetEnvironmentDesignationNote(org.apache.xmlbeans.XmlString environmentDesignationNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTDESIGNATIONNOTE$0);
            }
            target.set(environmentDesignationNote);
        }
    }
}
