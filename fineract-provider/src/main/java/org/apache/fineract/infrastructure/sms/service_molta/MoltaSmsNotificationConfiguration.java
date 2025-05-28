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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApprovedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRejectedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentDueBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMakeRepaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MoltaSmsNotificationConfiguration {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final MoltaLoanRepaymentDueNotificationListener moltaLoanRepaymentDueNotificationListener;
    private final MoltaLoanApprovedNotificationListener moltaLoanApprovedNotificationListener;
    private final MoltaLoanRejectedNotificationListener moltaLoanRejectedNotificationListener;
    private final MoltaLoanDisbursedNotificationListener moltaLoanDisbursedNotificationListener;
    private final MoltaLoanRepaymentSuccessfulNotificationListener moltaLoanRepaymentSuccessfulNotificationListener;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(
                LoanApprovedBusinessEvent.class,
                moltaLoanApprovedNotificationListener
        );

        businessEventNotifierService.addPostBusinessEventListener(
                LoanRejectedBusinessEvent.class,
                moltaLoanRejectedNotificationListener
        );

        businessEventNotifierService.addPostBusinessEventListener(
                LoanDisbursalBusinessEvent.class,
                moltaLoanDisbursedNotificationListener
        );

        businessEventNotifierService.addPostBusinessEventListener(
                LoanTransactionMakeRepaymentPostBusinessEvent.class,
                moltaLoanRepaymentSuccessfulNotificationListener
        );

        businessEventNotifierService.addPostBusinessEventListener(
                LoanRepaymentDueBusinessEvent.class,
                moltaLoanRepaymentDueNotificationListener
        );
    }
}