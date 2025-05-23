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
package org.apache.fineract.portfolio.client_molta.api.v2.search;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.portfolio.client.api.v2.search.ClientSearchV2Api;
import org.apache.fineract.portfolio.client.service.search.domain.ClientSearchData;
import org.apache.fineract.portfolio.client.service.search.domain.ClientTextSearch;
import org.apache.fineract.portfolio.client_molta.service.ClientMoltaSearchService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientMoltaSearchV2ApiDelegate implements ClientSearchV2Api {

    private final ClientMoltaSearchService searchService;

    @Override
    public Page<ClientSearchData> searchByText(PagedRequest<ClientTextSearch> request) {
        return searchService.searchByText(request);
    }
}
