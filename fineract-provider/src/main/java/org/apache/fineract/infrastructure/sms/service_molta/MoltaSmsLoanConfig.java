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

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class MoltaSmsLoanConfig {

    @Getter
    @Setter
    private Map<String, Boolean> actions;

    @Getter
    @Setter
    private Map<String, String> campaigns;

    public boolean isApproveEnabled() {
        return actions.getOrDefault("approve", false);
    }

    public boolean isRejectEnabled() {
        return actions.getOrDefault("reject", false);
    }

    public boolean isDisburseEnabled() {
        return actions.getOrDefault("disburse", false);
    }

    public boolean isRepaymentEnabled() {
        return actions.getOrDefault("repayment", false);
    }

    public String getDueRepaymentCampaign() {
        return campaigns.getOrDefault("dueRepayment", "Loan Repayment Due Notification");
    }

    public String getApprovedCampaign() {
        return campaigns.getOrDefault("approve", "Loan Approved Status Notification");
    }

    public String getRejectedCampaign() {
        return campaigns.getOrDefault("reject", "Loan Rejected Status Notification");
    }

    public String getDisbursedCampaign() {
        return campaigns.getOrDefault("disburse", "Loan Disbursed Status Notification");
    }

    public String getRepaymentCampaign() {
        return campaigns.getOrDefault("repayment", "Loan Repayment Status Notification");
    }

}