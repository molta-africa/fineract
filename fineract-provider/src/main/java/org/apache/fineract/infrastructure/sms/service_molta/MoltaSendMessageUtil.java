/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.sms.service_molta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.campaigns.helper_molta.SmsMoltaConfigUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignConstants;
import org.apache.fineract.infrastructure.campaigns.sms.exception.ConnectionFailureException;
import org.apache.fineract.infrastructure.sms.data.SmsMessageApiQueueResourceData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoltaSendMessageUtil {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SmsMoltaConfigUtils smsMoltaConfigUtils;

    public void sendMessage(String message, String mobileNo, String campaignName) {
        try {
            SmsMessageApiQueueResourceData apiQueueResourceData = SmsMessageApiQueueResourceData.instance(null, null, null, null, mobileNo, message, null);
            Map<String, Object> hostConfig = smsMoltaConfigUtils.getMessageGateWayRequestURI(apiQueueResourceData);
            String url = hostConfig.get("url").toString();
            HttpEntity<?> entity = (HttpEntity<?>) hostConfig.get("entity");
            HttpMethod httpMethod = (HttpMethod) hostConfig.get("httpMethod");

            log.info("Sending SMS notification for loan - {}", campaignName);
            ResponseEntity<String> responseOne = restTemplate.exchange(url, httpMethod, entity, new ParameterizedTypeReference<>() {});
            if (!responseOne.getStatusCode().is2xxSuccessful()) {
                log.debug("{}", responseOne.getStatusCode().value());
                throw new ConnectionFailureException(SmsCampaignConstants.SMS);
            }
        } catch (Exception e) {
            log.error("Error sending SMS notification for loan - {}", campaignName, e);
        }
    }

}
