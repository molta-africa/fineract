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


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.molta.MoltaSmsCampaignRepository;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentDueBusinessEvent;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MoltaLoanRepaymentDueNotificationListener implements BusinessEventListener<LoanRepaymentDueBusinessEvent> {

    private final SmsMessageRepository smsMessageRepository;
    private final MoltaSmsCampaignRepository moltaSmsCampaignRepository;
    private final MoltaSmsLoanConfigLoader moltaSmsLoanConfigLoader;

    @Autowired
    public MoltaLoanRepaymentDueNotificationListener(SmsMessageRepository smsMessageRepository, MoltaSmsCampaignRepository moltaSmsCampaignRepository, MoltaSmsLoanConfigLoader moltaSmsLoanConfigLoader) {
        this.smsMessageRepository = smsMessageRepository;
        this.moltaSmsCampaignRepository = moltaSmsCampaignRepository;
        this.moltaSmsLoanConfigLoader = moltaSmsLoanConfigLoader;
    }


    @Override
    public void onBusinessEvent(LoanRepaymentDueBusinessEvent event) {
        try {
            LoanRepaymentScheduleInstallment installment = event.get();
            Loan loan = installment.getLoan();
            Client client = loan.getClient();

            if (client != null && client.mobileNo() != null && !client.mobileNo().isEmpty()) {
                // find the sms campaign
                String campaignName = moltaSmsLoanConfigLoader.getMoltaSmsLoanConfig().getDueRepaymentCampaign();
                SmsCampaign smsCampaign = moltaSmsCampaignRepository.findFirstByCampaignNameOrderByIdDesc(campaignName);
                if (smsCampaign == null || StringUtils.isBlank(smsCampaign.getMessage())) {
                    log.error("SMS campaign message '{}' not found. Please create it through the API first.", campaignName);
                    return;
                }

                // Create SMS message text
                String smsText = smsCampaign.getMessage();
                smsText = smsText.replace("${display_name}", client.getDisplayName())
                        .replace("${currency}", loan.getCurrency().getCode())
                        .replace("${amount}", installment.getTotalOutstanding(loan.getCurrency()).getAmount().toString())
                        .replace("${due_date}", installment.getDueDate().format(DateUtils.DEFAULT_DATE_FORMATTER));

                SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, smsText, client.mobileNo(), smsCampaign, false);
                smsMessageRepository.save(smsMessage);

                log.info("SMS notification sent for loan repayment due. Loan ID: {}, Client: {}", loan.getId(), client.getDisplayName());
            } else {
                log.warn("Cannot send SMS notification: Client has no mobile number. Loan ID: {}", loan.getId());
            }
        } catch (Exception e) {
            log.error("Error sending SMS notification for loan repayment due", e);
        }
    }

}
