/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.common;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-07-2016.
 */
public class XMLSimilarityIgnoreElements implements DifferenceListener {
    private Set<String> blackList = new HashSet<>();

    public XMLSimilarityIgnoreElements(String ... elementNames) {
        for (String name : elementNames) {
            blackList.add(name);
        }
    }

    public int differenceFound(Difference difference) {
        if (difference.getId() == DifferenceConstants.SCHEMA_LOCATION_ID) {
            return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }
        if (difference.getTestNodeDetail().getNode() != null && blackList.contains(difference.getTestNodeDetail().getNode().getLocalName())) {
            return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node node, Node node1) {

    }
}