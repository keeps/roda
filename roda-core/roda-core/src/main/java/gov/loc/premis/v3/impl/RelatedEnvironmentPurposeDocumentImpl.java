/*
 * An XML document type.
 * Localname: relatedEnvironmentPurpose
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEnvironmentPurposeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedEnvironmentPurpose(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedEnvironmentPurposeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEnvironmentPurposeDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEnvironmentPurposeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDENVIRONMENTPURPOSE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEnvironmentPurpose");
    
    
    /**
     * Gets the "relatedEnvironmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedEnvironmentPurpose()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDENVIRONMENTPURPOSE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedEnvironmentPurpose" element
     */
    public void setRelatedEnvironmentPurpose(gov.loc.premis.v3.StringPlusAuthority relatedEnvironmentPurpose)
    {
        generatedSetterHelperImpl(relatedEnvironmentPurpose, RELATEDENVIRONMENTPURPOSE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedEnvironmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedEnvironmentPurpose()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDENVIRONMENTPURPOSE$0);
            return target;
        }
    }
}
