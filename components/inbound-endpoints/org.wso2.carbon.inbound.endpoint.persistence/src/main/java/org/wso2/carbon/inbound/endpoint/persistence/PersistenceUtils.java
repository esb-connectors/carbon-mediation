/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.persistence;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceUtils {

    // Attributes
    private static final String NAME_ATT = "name";
    private static final String PORT_ATT = "port";
    private static final String DOMAIN_ATT = "domain";
    private static final String PROTOCOL_ATT = "protocol";

    // QNames
    private static final QName INBOUND_ENDPOINTS_QN = new QName("inboundEndpoints");
    private static final QName INBOUND_ENDPOINT_LISTENER_QN = new QName("inboundEndpointListener");
    private static final QName ENDPOINT_QN = new QName("endpoint");

    private static final QName NAME_QN = new QName(NAME_ATT);
    private static final QName PORT_QN = new QName(PORT_ATT);
    private static final QName DOMAIN_QN = new QName(DOMAIN_ATT);
    private static final QName PROTOCOL_QN = new QName(PROTOCOL_ATT);

    private static OMFactory fac = OMAbstractFactory.getOMFactory();
    private static final OMNamespace nullNS =
            fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    /**
     * Convert EndpointInfo to a OMElement
     *
     * @param endpointInfo tenant data map
     * @return equivalent OMElement for EndpointInfo
     */
    public static OMElement convertEndpointInfoToOM(
            Map<Integer, List<InboundEndpointInfoDTO>> endpointInfo) {

        OMElement parentElement = fac.createOMElement(INBOUND_ENDPOINTS_QN);

        for (Map.Entry<Integer, List<InboundEndpointInfoDTO>> mapEntry : endpointInfo.entrySet()) {
            int port = mapEntry.getKey();

            OMElement listenerElem = fac.createOMElement(INBOUND_ENDPOINT_LISTENER_QN, parentElement);
            listenerElem.addAttribute(PORT_ATT, String.valueOf(port), nullNS);

            List<InboundEndpointInfoDTO> tenantDomains = mapEntry.getValue();
            for (InboundEndpointInfoDTO inboundEndpointInfoDTO : tenantDomains) {
                OMElement endpointElem = fac.createOMElement(ENDPOINT_QN, listenerElem);

                endpointElem.addAttribute(NAME_ATT, inboundEndpointInfoDTO.getEndpointName(), nullNS);
                endpointElem.addAttribute(DOMAIN_ATT, inboundEndpointInfoDTO.getTenantDomain(), nullNS);
                endpointElem.addAttribute(PROTOCOL_ATT, inboundEndpointInfoDTO.getProtocol(), nullNS);
            }
        }
        return parentElement;
    }

    /**
     * Create EndpointInfo from OMElement
     *
     * @param endpointInfoOM OMElement containing endpoint information
     * @return equivalent EndpointInfo for OMElement
     */
    public static Map<Integer,List<InboundEndpointInfoDTO>> convertOMToEndpointInfo(
            OMElement endpointInfoOM) {

        Map<Integer, List<InboundEndpointInfoDTO>> endpointInfo =
                new ConcurrentHashMap<Integer, List<InboundEndpointInfoDTO>>();

        Iterator listenerElementsItr =
                endpointInfoOM.getChildrenWithName(INBOUND_ENDPOINT_LISTENER_QN);
        while (listenerElementsItr.hasNext()) {

            List<InboundEndpointInfoDTO> tenantList = new ArrayList<InboundEndpointInfoDTO>();
            OMElement listenerElement = (OMElement) listenerElementsItr.next();
            int port = Integer.parseInt(listenerElement.getAttributeValue(PORT_QN));

            Iterator endpointsItr = listenerElement.getChildrenWithName(ENDPOINT_QN);
            while (endpointsItr.hasNext()) {
                OMElement endpointElement = (OMElement) endpointsItr.next();
                InboundEndpointInfoDTO inboundEndpointInfoDTO =
                        new InboundEndpointInfoDTO(endpointElement.getAttributeValue(DOMAIN_QN),
                                          endpointElement.getAttributeValue(PROTOCOL_QN),
                                          endpointElement.getAttributeValue(NAME_QN));

                tenantList.add(inboundEndpointInfoDTO);
            }
            endpointInfo.put(port, tenantList);
        }
        return endpointInfo;
    }

}
