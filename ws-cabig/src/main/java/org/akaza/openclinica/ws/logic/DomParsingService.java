/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomParsingService {

    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    private final String ISO_21090 = "uri:iso.org:21090";

    public DomParsingService() {

    }

    /**
     * get element value, generic parser for getting the value of an attribute in an XML line.
     * 
     * @param subject
     *            , our XML node.
     * @param namespace
     *            , our namespace, typically will be http://clinicalconnector.nci.nih.gov, but could also be ISO 21090
     * @param xmlLine
     *            , the xml line we want to parse
     * @param attrName
     *            , the attribute we want to grab, could be 'value' or 'code' or something generic like that
     * @return
     */
    public String getElementValue(Node subject, String namespace, String xmlLine, String attrName) {
        String ret = "";
        Element subjectElement = (Element) subject;
        NodeList xmlNode = subjectElement.getElementsByTagNameNS(namespace, xmlLine);
        Node xmlNodeValue = xmlNode.item(0);
        try {
            if (xmlNodeValue.hasAttributes()) {
                NamedNodeMap nodeMap = xmlNodeValue.getAttributes();
                Node nodeValue = nodeMap.getNamedItem(attrName);
                ret = nodeValue.getNodeValue();
            }
        } catch (NullPointerException npe) {
            System.out.println("null pointer found");
            // catch a null pointer, look for the null flavor instead
            NamedNodeMap nodeMap = xmlNodeValue.getAttributes();
            Node nodeValue = nodeMap.getNamedItem("nullFlavor");
            ret = nodeValue.getNodeValue();
        }
        return ret;

    }

    // ///////////////////////////////////////////////
    // study-specific parsings
    // ///////////////////////////////////////////////

    /*
     * <ns2:targetAccrualNumberRange xmlns:ns1="uri:iso.org:21090" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:IVL_INT"
     * lowClosed="false" highClosed="false"> <ns1:high xsi:type="ns1:INT" value="143"/>
     */
    public int getTargetAccrualNumberRange(Node study) {
        int enrollment = 0;
        Element studyElement = (Element) study;
        NodeList nlist = studyElement.getElementsByTagNameNS(this.CONNECTOR_NAMESPACE_V1, "targetAccrualNumberRange");
        Node nlistNode = nlist.item(0);
        Element nlistNodeElement = (Element) nlistNode;
        NodeList nlist2 = nlistNodeElement.getElementsByTagNameNS(this.ISO_21090, "high");
        Node nlist2Node = nlist2.item(0);
        if (nlist2Node.hasAttributes()) {
            NamedNodeMap nodeMap = nlist2Node.getAttributes();
            Node nodeValue = nodeMap.getNamedItem("value");
            enrollment = new Integer(nodeValue.getNodeValue()).intValue();
        }

        return enrollment;

    }

    /**
     * <ns2:identifier xmlns:ns1="uri:iso.org:21090" xsi:type="ns1:II" root="2.16.840.1.113883.3.26.7.8" extension="NC010"
     * identifierName="Study Coordinating Center" displayable="false"/> <ns2:name xmlns:ns1="uri:iso.org:21090" xsi:type="ns1:ST"
     * value="Duke University Medical Center"/> <ns2:postalAddress xmlns:ns1="uri:iso.org:21090" xsi:type="ns1:AD" isNotOrdered="false"> <ns1:part
     * value="Durham" type="CTY"/> <ns1:part code="USA" type="CNT"/> <ns1:part code="NC" type="STA"/> </ns2:postalAddress>
     * 
     * @param study
     * @param studyNode
     * @return
     */
    public StudyBean getStudyCenter(StudyBean study, Node studyNode) {
        String facilityCity = "", facilityCountry = "", facilityName = "", facilityState = "";
        Element studyElement = (Element) studyNode;
        NodeList nlist = studyElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studyCoordinatingCenter");
        Node nlistNode = nlist.item(0);
        Element nlistNodeElement = (Element) nlistNode;
        NodeList nlist2 = nlistNodeElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "name");
        Node nlist2Node = nlist2.item(0);
        if (nlist2Node.hasAttributes()) {
            NamedNodeMap nodeMap = nlist2Node.getAttributes();
            Node nodeValue = nodeMap.getNamedItem("value");
            facilityName = nodeValue.getNodeValue();
        }
        NodeList nlist3 = nlistNodeElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "postalAddress");
        for (int i = 0; i < nlist3.getLength(); i++) {
            Node nlist3Node = nlist3.item(i);
            NamedNodeMap nodeMap = nlist3Node.getAttributes();
            Node nodeType = nodeMap.getNamedItem("type");
            Node nodeValue = nodeMap.getNamedItem("value");
            if ("CTY".equals(nodeType.getNodeValue())) {
                facilityCity = nodeValue.getNodeValue();
            } else if ("CNT".equals(nodeType.getNodeValue())) {
                facilityCountry = nodeValue.getNodeValue();
            } else if ("STA".equals(nodeType.getNodeValue())) {
                facilityState = nodeValue.getNodeValue();
            }
            // case nodeType.getNodeValue():
        }

        study.setFacilityCity(facilityCity);
        study.setFacilityCountry(facilityCountry);
        study.setFacilityName(facilityName);
        study.setFacilityState(facilityState);
        return study;
    }

    public StudyBean getStudyInvestigator(StudyBean study, Node studyNode) {
        Element studyElement = (Element) studyNode;
        NodeList nlist = studyElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studyInvestigator");
        Node nlistNode = nlist.item(0);
        Element nlistNodeElement = (Element) nlistNode;
        NodeList nlist2 = nlistNodeElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "name");
        String firstName = "", lastName = "";
        Node nameNode = nlist2.item(0);
        Element nameNodeElement = (Element) nameNode;
        NodeList nlistNames = nameNodeElement.getElementsByTagNameNS(this.ISO_21090, "part");
        for (int i = 0; i < nlistNames.getLength(); i++) {
            Node nlist2Node = nlistNames.item(i);
            NamedNodeMap nodeMap = nlist2Node.getAttributes();
            System.out.println("found node map: " + nodeMap.toString() + " " + nodeMap.getLength());
            for (int x = 0; x < nodeMap.getLength(); x++) {
                Node something = nodeMap.item(x);
                System.out.println(x + ": " + something.getLocalName() + " -> " + something.getNodeName() + " -> " // + something.getAttributes().getLength()
                    + " " + something.getNodeValue());
            }
            Node nodeType = nodeMap.getNamedItem("type");
            Node nodeValue = nodeMap.getNamedItem("value");
            if ("GIV".equals(nodeType.getNodeValue())) {
                firstName = nodeValue.getNodeValue();
            } else {
                lastName = nodeValue.getNodeValue();
            }
        }
        study.setPrincipalInvestigator(firstName + " " + lastName);
        study.setFacilityContactName(firstName + " " + lastName);
        NodeList nlist3 = nlistNodeElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "telecomAddress");
        Node addrNode = nlist3.item(0);
        String facilityContactEmail = "", facilityContactPhone = "";
        Element addrNodeElement = (Element) addrNode;
        NodeList addrNodeList = addrNodeElement.getElementsByTagNameNS(ISO_21090, "item");
        for (int j = 0; j < addrNodeList.getLength(); j++) {
            Node nlist3Node = addrNodeList.item(j);
            NamedNodeMap nodeMap = nlist3Node.getAttributes();
            // Node nodeType = nodeMap.getNamedItemNS("http://www.w3.org/2001/XMLSchema-instance", "type");
            // above necessary if we get things other than telephone, tbh
            Node nodeValue = nodeMap.getNamedItem("value");
            if (nodeValue.getNodeValue().startsWith("tel:")) {
                facilityContactPhone = nodeValue.getNodeValue().replaceFirst("tel:", "");
            } else if (nodeValue.getNodeValue().startsWith("mailto:")) {
                facilityContactEmail = nodeValue.getNodeValue().replaceFirst("mailto:", "");
            }

        }
        study.setFacilityContactEmail(facilityContactEmail);
        study.setFacilityContactPhone(facilityContactPhone);
        System.out.println("found email " + facilityContactEmail + " phone " + facilityContactPhone);
        return study;
    }
}
