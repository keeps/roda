/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import static org.apache.solr.common.params.CommonParams.NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Moved class from:
 * https://github.com/apache/solr/blob/30bf94db62fd92354a9c9437eacf884f8fc862d9/solr/core/src/java/org/apache/solr/handler/loader/XMLLoader.java
 */
public class SolrXMLLoader {

    private static final AtomicBoolean WARNED_ABOUT_INDEX_TIME_BOOSTS = new AtomicBoolean();
    private static final Logger log = LoggerFactory.getLogger(ZkController.class);

    /**
     * Given the input stream, read a document
     *
     * @since solr 1.3
     */
    @SuppressWarnings({"unchecked"})
    public SolrInputDocument readDoc(XMLStreamReader parser) throws XMLStreamException {
        SolrInputDocument doc = new SolrInputDocument();

        String attrName = "";
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            attrName = parser.getAttributeLocalName(i);
            if ("boost".equals(attrName)) {
                String message = "Ignoring document boost: " + parser.getAttributeValue(i)
                    + " as index-time boosts are not supported anymore";
                if (WARNED_ABOUT_INDEX_TIME_BOOSTS.compareAndSet(false, true)) {
                    log.warn(message);
                } else {
                    log.debug(message);
                }
            } else {
                log.warn("XML element <doc> has invalid XML attr: {}", attrName);
            }
        }

        StringBuilder text = new StringBuilder();
        String name = null;
        boolean isNull = false;
        boolean isLabeledChildDoc = false;
        String update = null;
        Collection<SolrInputDocument> subDocs = null;
        Map<String, Map<String, Object>> updateMap = null;
        boolean complete = false;
        while (!complete) {
            int event = parser.next();
            switch (event) {
                // Add everything to the text
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.CHARACTERS:
                    text.append(parser.getText());
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if ("doc".equals(parser.getLocalName())) {
                        if (subDocs != null && !subDocs.isEmpty()) {
                            doc.addChildDocuments(subDocs);
                            subDocs = null;
                        }
                        complete = true;
                        break;
                    } else if ("field".equals(parser.getLocalName())) {
                        // should I warn in some text has been found too
                        Object v = isNull ? null : text.toString();
                        if (update != null) {
                            if (updateMap == null)
                                updateMap = new HashMap<>();
                            Map<String, Object> extendedValues = updateMap.get(name);
                            if (extendedValues == null) {
                                extendedValues = new HashMap<>(1);
                                updateMap.put(name, extendedValues);
                            }
                            Object val = extendedValues.get(update);
                            if (val == null) {
                                extendedValues.put(update, v);
                            } else {
                                // multiple val are present
                                if (val instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> list = (List<Object>) val;
                                    list.add(v);
                                } else {
                                    List<Object> values = new ArrayList<>();
                                    values.add(val);
                                    values.add(v);
                                    extendedValues.put(update, values);
                                }
                            }
                            break;
                        }
                        if (!isLabeledChildDoc) {
                            // only add data if this is not a childDoc, since it
                            // was added already
                            doc.addField(name, v);
                        } else {
                            // reset so next field is not treated as child doc
                            isLabeledChildDoc = false;
                        }
                        // field is over
                        name = null;
                    }
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    text.setLength(0);
                    String localName = parser.getLocalName();
                    if ("doc".equals(localName)) {
                        if (name != null) {
                            // flag to prevent spaces after doc from being added
                            isLabeledChildDoc = true;
                            if (!doc.containsKey(name)) {
                                doc.setField(name, Lists.newArrayList());
                            }
                            doc.addField(name, readDoc(parser));
                            break;
                        }
                        if (subDocs == null)
                            subDocs = Lists.newArrayList();
                        subDocs.add(readDoc(parser));
                    } else {
                        if (!"field".equals(localName)) {
                            String msg = "XML element <doc> has invalid XML child element: " + localName;
                            log.warn(msg);
                            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, msg);
                        }
                        update = null;
                        isNull = false;
                        String attrVal = "";
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            attrName = parser.getAttributeLocalName(i);
                            attrVal = parser.getAttributeValue(i);
                            if (NAME.equals(attrName)) {
                                name = attrVal;
                            } else if ("boost".equals(attrName)) {
                                String message = "Ignoring field boost: " + attrVal
                                    + " as index-time boosts are not supported anymore";
                                if (WARNED_ABOUT_INDEX_TIME_BOOSTS.compareAndSet(false, true)) {
                                    log.warn(message);
                                } else {
                                    log.debug(message);
                                }
                            } else if ("null".equals(attrName)) {
                                isNull = StrUtils.parseBoolean(attrVal);
                            } else if ("update".equals(attrName)) {
                                update = attrVal;
                            } else {
                                log.warn("XML element <field> has invalid XML attr: {}", attrName);
                            }
                        }
                    }
                    break;
            }
        }

        if (updateMap != null) {
            for (Map.Entry<String, Map<String, Object>> entry : updateMap.entrySet()) {
                name = entry.getKey();
                Map<String, Object> value = entry.getValue();
                doc.addField(name, value);
            }
        }

        return doc;
    }

}
