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
package org.apache.fineract.infrastructure.campaigns.jobs.sendmessagetosmsgateway_molta;

import org.apache.fineract.infrastructure.campaigns.helper_molta.SmsMoltaConfigUtils;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.gcm.service.NotificationSenderService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SendMessageToSmsGatewayMoltaConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private SmsMessageRepository smsMessageRepository;
    @Autowired
    private NotificationSenderService notificationSenderService;
    @Autowired
    private SmsMoltaConfigUtils smsMoltaConfigUtils;
    @Autowired
    @Qualifier(TaskExecutorConstant.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    private ThreadPoolTaskExecutor taskExecutor;

    @Bean
    @Primary
    protected Step sendMessageToSmsMoltaGatewayStep() {
        return new StepBuilder(JobName.SEND_MESSAGES_TO_SMS_GATEWAY_MOLTA.name(), jobRepository)
                .tasklet(sendMessageToSmsGatewayMoltaTasklet(), transactionManager).build();
    }

    @Bean
    @Primary
    public Job sendMessageToSmsMoltaGatewayJob() {
        return new JobBuilder(JobName.SEND_MESSAGES_TO_SMS_GATEWAY_MOLTA.name(), jobRepository).start(sendMessageToSmsMoltaGatewayStep())
                .incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    @Primary
    public SendMessageToSmsGatewayMoltaTasklet sendMessageToSmsGatewayMoltaTasklet() {
        return new SendMessageToSmsGatewayMoltaTasklet(smsMessageRepository, notificationSenderService, smsMoltaConfigUtils, taskExecutor);
    }
}
