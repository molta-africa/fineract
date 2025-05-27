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
package org.apache.fineract.infrastructure.campaigns.helper_molta;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.data.MessageGatewayConfigurationData;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsRuntimeException;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformServiceImpl;
import org.apache.fineract.infrastructure.sms.data.SmsMessageApiQueueResourceData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SmsMoltaConfigUtils {

    @Value("${molta.sms.property}")
    private String configProperty;

    @Value("${molta.sms.value}")
    private String configPropertyValue;

    @Value("${molta.sms.mediatype}")
    private String configMediaType;

    @Value("${molta.sms.queryparam}")
    private String configQueryParam;

    @Value("${molta.sms.headers}")
    private String configHeadersStr;

    @Value("${molta.sms.scheme}")
    private String configHttpScheme;

    @Value("${molta.sms.http.method}")
    private String configHttpMethod;

    @Value("${molta.sms.format.number}")
    private boolean configShouldFormatNumber;

    private static final String SPLITTER = ";";


    @Autowired
    private ExternalServicesPropertiesReadPlatformServiceImpl propertiesReadPlatformService;

    // This method will return uri and HttpEntry objects with keys as uri, entity and httpMethod
    public Map<String, Object> getMessageGateWayRequestURI(SmsMessageApiQueueResourceData apiQueueResourceData) {
        Map<String, Object> httpRequestDetails = new HashMap<>();
        MessageGatewayConfigurationData gatewayConfigurationData = this.propertiesReadPlatformService.getSMSGateway();

        String property = configProperty;
        String propertyValue = configPropertyValue;

        HttpHeaders headers = buildHeaders(configHeadersStr);

        String message = apiQueueResourceData.getMessage();
        String mobileNumber = apiQueueResourceData.getMobileNumber();
        if (configShouldFormatNumber) {
            mobileNumber = formatTo234(mobileNumber);
        }

        if (StringUtils.isBlank(mobileNumber)) {
            throw new SmsRuntimeException("SMS mobile number error", "SMS mobile number is invalid: " + mobileNumber);
        }

        if (StringUtils.isBlank(property) || property.split(SPLITTER).length == 0) {
            throw new SmsRuntimeException("SMS config property blank error", "SMS property is blank: ");
        }
        String[] properties = property.split(SPLITTER);

        if (StringUtils.isBlank(propertyValue) || propertyValue.split(SPLITTER).length == 0) {
            throw new SmsRuntimeException("SMS config property values blank error", "SMS property values is blank: ");
        }
        propertyValue = propertyValue.replace("{message}", message)
                        .replace("{to}", mobileNumber);
        String[] propertyValues = propertyValue.split(SPLITTER);

        if (properties.length != propertyValues.length || properties.length < 2) {
            log.error("SMS properties size {} not matching SMS property values size {}", properties.length, propertyValues.length);
            throw new SmsRuntimeException("SMS properties does not match property values", "SMS properties does not match property values");
        }

        String httpScheme = StringUtils.isBlank(configHttpScheme) ? "https" : configHttpScheme;
        String url = constructBaseUrl(gatewayConfigurationData, httpScheme);
        url = buildFullUrl(properties, propertyValues, url, configQueryParam);

        HttpEntity<?> entity = buildRequestEntity(properties, propertyValues, configMediaType, headers);

        HttpMethod httpMethod = getHttpMethod(configHttpMethod);

        httpRequestDetails.put("url", url);
        httpRequestDetails.put("entity", entity);
        httpRequestDetails.put("httpMethod", httpMethod);

        return httpRequestDetails;
    }

    private HttpMethod getHttpMethod(String configHttpMethod) {
        HttpMethod httpMethod = HttpMethod.POST;
        if (StringUtils.isNotBlank(configHttpMethod)) {
            try {
                httpMethod = HttpMethod.valueOf(configHttpMethod.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Invalid HTTP method: {}", configHttpMethod);
            }
        }
        return httpMethod;
    }

    private HttpHeaders buildHeaders(String headersStr) {
        HttpHeaders headers = new HttpHeaders();
        if (headersStr != null && !headersStr.isBlank()) {
            String[] pairs = headersStr.split(SPLITTER);
            for (String pair : pairs) {
                String[] parts = pair.trim().split(":", 2);
                if (parts.length == 2) {
                    headers.add(parts[0].trim(), parts[1].trim());
                }
            }
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return headers;
    }

    private String constructBaseUrl(MessageGatewayConfigurationData config, String scheme) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(scheme).append("://").append(config.hostName());

        if (StringUtils.isNotBlank(config.endPoint())) {
            urlBuilder.append(config.endPoint());
        }

        if (config.portNumber() != 0) {
            urlBuilder.append(":").append(config.portNumber());
        }

        return urlBuilder.toString();
    }

    private String buildFullUrl(String[] properties, String[] propertyValues, String url, String queryParams) {
        if (StringUtils.isNotBlank(queryParams)) {
            String processedQueryParams = queryParams;
            for (int i = 0; i < properties.length; i++) {
                processedQueryParams = processedQueryParams.replace("{" + properties[i] + "}", propertyValues[i]);
            }
            return url + processedQueryParams;
        }
        return url;
    }

    private HttpEntity<?> buildRequestEntity(String[] properties, String[] propertyValues, String mediaType, HttpHeaders headers) {
        if ("form-data".equalsIgnoreCase(mediaType)) {
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            return getMultiValueMapHttpEntity(properties, propertyValues, headers);
        } else if ("json".equalsIgnoreCase(mediaType)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> payload = new HashMap<>();

            for (int i = 0; i < properties.length; i++) {
                payload.put(properties[i], propertyValues[i]);
            }

            return new HttpEntity<>(payload, headers);
        } else if ("url-encoded".equalsIgnoreCase(mediaType)) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return getMultiValueMapHttpEntity(properties, propertyValues, headers);
        }

        return new HttpEntity<>(headers);
    }

    public String formatTo234(String input) {
        if (StringUtils.isBlank(input)) return null;

        // Remove leading '+'
        if (input.startsWith("+")) {
            input = input.substring(1);
        }

        // Replace starting 0 with 234
        if (input.startsWith("0")) {
            input = "234" + input.substring(1);
        }

        // If already starts with 234 and length is 13, return as is
        if (input.startsWith("234") && input.length() == 13) {
            return input;
        }

        // If starts with 234 but is longer or shorter than 13, truncate or reject
        if (input.startsWith("234")) {
            if (input.length() > 13) {
                return input.substring(0, 13);
            } else if (input.length() < 13) {
                return null; // Not a valid number
            }
        }

        // If number doesn't start with 234 and is not formatted yet, try formatting
        if (!input.startsWith("234") && input.length() == 10) {
            return "234" + input;
        }

        // If it’s already 13 digits but doesn't start with 234, we don’t accept it
        return null;
    }

    @NotNull
    private HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(String[] properties, String[] propertyValues, HttpHeaders headers) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        for (int i = 0; i < properties.length; i++) {
            formData.add(properties[i], propertyValues[i]);
        }

        return new HttpEntity<>(formData, headers);
    }

}
