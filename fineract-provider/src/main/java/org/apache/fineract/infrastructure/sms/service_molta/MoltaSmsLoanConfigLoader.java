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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MoltaSmsLoanConfigLoader {

    @Value("${molta.sms.loan.actions}")
    private String moltaSmsLoanAction;

    @Value("${molta.sms.loan.campaigns}")
    private String moltaSmsLoanCampaigns;

    @Getter
    private MoltaSmsLoanConfig moltaSmsLoanConfig;

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        moltaSmsLoanConfig = new MoltaSmsLoanConfig();
        try {
            Map<String, Boolean> actions = mapper.readValue(moltaSmsLoanAction, new TypeReference<>() {});
            moltaSmsLoanConfig.setActions(actions);
        } catch (Exception e) {
            log.error("Unable to load SMS loan actions configuration {}, using default configuration", moltaSmsLoanAction, e);
            Map<String, Boolean> actions = new HashMap<>();
            actions.put("approve", false);
            actions.put("reject", false);
            actions.put("disburse", false);
            actions.put("repayment", false);
            actions.put("completion", false);
            moltaSmsLoanConfig.setActions(actions);
        }

        try {
            Map<String, String> campaigns = mapper.readValue(moltaSmsLoanCampaigns, new TypeReference<>() {});
            moltaSmsLoanConfig.setCampaigns(campaigns);
        } catch (Exception e) {
            log.error("Unable to load SMS loan campaigns configuration {}, using default configuration", moltaSmsLoanCampaigns, e);
            Map<String, String> campaigns = new HashMap<>();
            campaigns.put("dueRepayment", "Loan Repayment Due Notification");
            campaigns.put("approve", "Loan Approved Status Notification");
            campaigns.put("reject", "Loan Rejected Status Notification");
            campaigns.put("disburse", "Loan Disbursed Status Notification");
            campaigns.put("repayment", "Loan Repayment Status Notification");
            campaigns.put("completion", "Loan Completion Status Notification");
            moltaSmsLoanConfig.setCampaigns(campaigns);
        }
    }

}