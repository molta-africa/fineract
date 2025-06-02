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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.molta.MoltaSmsCampaignRepository;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMakeRepaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Component
@Slf4j
@RequiredArgsConstructor
public class MoltaLoanRepaymentSuccessfulNotificationListener implements BusinessEventListener<LoanTransactionMakeRepaymentPostBusinessEvent> {

    private final SmsMessageRepository smsMessageRepository;
    private final MoltaSmsCampaignRepository moltaSmsCampaignRepository;
    private final MoltaSmsLoanConfigLoader moltaSmsLoanConfigLoader;
    private final MoltaSendMessageUtil moltaSendMessageUtil;

    @Override
    public void onBusinessEvent(LoanTransactionMakeRepaymentPostBusinessEvent event) {
        try {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            Client client = loan.getClient();

            MoltaSmsLoanConfig moltaSmsLoanConfig = moltaSmsLoanConfigLoader.getMoltaSmsLoanConfig();
            String campaignName;
            SmsCampaign smsCampaign;

            if (client != null && client.mobileNo() != null && !client.mobileNo().isEmpty()) {
                if (loan.getLoanStatus() != null && loan.getLoanStatus().equals(600)) { //CLOSED_OBLIGATIONS_MET
                    // Check if SMS is enabled for successful completion of loan repayment
                    if (!moltaSmsLoanConfig.isCompletionEnabled()) {
                        log.info("SMS is not configured to be sent for loan completion");
                        return;
                    }

                    // Find the campaign
                    campaignName = moltaSmsLoanConfig.getCompletionCampaign();
                } else { //REGULAR_REPAYMENT
                    // Check if SMS is enabled for successful loan repayment
                    if (!moltaSmsLoanConfig.isRepaymentEnabled()) {
                        log.info("SMS is not configured to be sent for successful loan repayment");
                        return;
                    }

                    // Find the campaign
                    campaignName = moltaSmsLoanConfig.getRepaymentCampaign();
                }

                smsCampaign = moltaSmsCampaignRepository.findFirstByCampaignNameOrderByIdDesc(campaignName);
                if (smsCampaign == null || StringUtils.isBlank(smsCampaign.getMessage())) {
                    log.error("SMS campaign message '{}' not found. Please create it through the API first.", campaignName);
                    return;
                }

                String last4Digit = "";
                ExternalId externalId = client.getExternalId();
                if (externalId != null && StringUtils.isNotBlank(externalId.getValue())) {
                    last4Digit = externalId.getValue().length() > 4 ? externalId.getValue().substring(externalId.getValue().length() - 4) : externalId.getValue();
                }

                // Create SMS message text
                String smsText = smsCampaign.getMessage();
                smsText = smsText.replace("${display_name}", client.getDisplayName())
                        .replace("${currency}", loan.getCurrency().getCode())
                        .replace("${account_number}", loan.getAccountNumber())
                        .replace("${date}", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                        .replace("${last4Digit}", last4Digit)
                        .replace("${principal_amount}", loan.getApprovedPrincipal().toString());

                // Create and save SMS message
                SmsMessage smsMessage = SmsMessage.pendingSms(
                        null, null, client, null, smsText, client.mobileNo(), smsCampaign, false
                );

                smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                smsMessageRepository.save(smsMessage);
                moltaSendMessageUtil.sendMessage(smsText, client.mobileNo(), campaignName);
            }
        } catch (Exception e) {
            log.error("Error queuing SMS notification for loan repayment", e);
        }
    }
}