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
package org.apache.fineract.portfolio.client_molta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.service.search.domain.ClientSearchData;
import org.apache.fineract.portfolio.client.service.search.domain.ClientTextSearch;
import org.apache.fineract.portfolio.client_molta.domain.ClientMoltaRepository;
import org.apache.fineract.portfolio.client_molta.service.mapper.ClientMoltaSearchDataMapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClientMoltaSearchService {

    private final PlatformSecurityContext context;
    private final ClientMoltaRepository clientRepository;
    private final ClientMoltaSearchDataMapper clientMoltaSearchDataMapper;

    public Page<ClientSearchData> searchByText(PagedRequest<ClientTextSearch> searchRequest) {
        validateTextSearchRequest(searchRequest);
        return executeTextSearch(searchRequest);
    }

    private void validateTextSearchRequest(PagedRequest<ClientTextSearch> searchRequest) {
        Objects.requireNonNull(searchRequest, "searchRequest must not be null");

        context.isAuthenticated();
    }

    private Page<ClientSearchData> executeTextSearch(PagedRequest<ClientTextSearch> searchRequest) {
        AppUser appUser = context.authenticatedUser();
        final String hierarchy = appUser.getOffice().getHierarchy();
        final boolean hasPermission = hasPermission(appUser);

        final Long staffId;
        if (hasPermission(appUser, "ALL_FUNCTIONS") || hasPermission(appUser, "ADMIN") ||
                hasPermission(appUser, "COMPLIANCE") || hasPermission(appUser, "FINANCE")) {
            staffId = null;
        } else {
            staffId = appUser.getStaff() == null ? null : appUser.getStaff().getId();
        }

        Optional<ClientTextSearch> request = searchRequest.getRequest();
        String requestSearchText = request.map(ClientTextSearch::getText).orElse(null);
        String searchText = StringUtils.defaultString(requestSearchText, "");

        Pageable pageable = searchRequest.toPageable();

        return clientRepository.searchByText(searchText, pageable, hierarchy, staffId, hasPermission).map(clientMoltaSearchDataMapper::map);
    }

    private boolean hasPermission(AppUser appUser) {
        final String ALL_FUNCTIONS = "ALL_FUNCTIONS";
        final String ALL_FUNCTIONS_READ = "ALL_FUNCTIONS_READ";

        try {
            appUser.validateHasReadPermission(ALL_FUNCTIONS);
            return true;
        } catch (Exception e) {
            log.error("Client - user has no permission all functions");
        }

        try {
            appUser.validateHasReadPermission(ALL_FUNCTIONS_READ);
            return true;
        } catch (Exception e) {
            log.error("Client - user has no permission to read all functions");
        }

        return false;
    }

    private boolean hasPermission(AppUser appUser, String permission) {
        try {
            appUser.validateHasReadPermission(permission);
            return true;
        } catch (Exception e) {
            log.error("user has no {} permission", permission);
        }

        return false;
    }
}
