/*
 * Copyright 2019-Present Entando Inc. (http://www.entando.com) All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.entando.entando.aps.system.services.digitalexchange.client;

import java.util.Optional;
import java.util.function.Function;
import org.entando.entando.aps.system.services.digitalexchange.model.DigitalExchange;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Contains all the information necessary for querying a specific Digital
 * Exchange endpoint (HTTP method, URL segments, entity sent as payload). This
 * class should be used for describing a call in which it is necessary to
 * retrieve the raw response (InputStream), while it subclasses should be used
 * when it is necessary to parse a response into a specific class and/or combine
 * the result of multiple requests.
 */
public class DigitalExchangeBaseCall<R> {

    private final HttpMethod method;
    private final String[] urlSegments;
    private HttpEntity<?> entity;
    private Function<RestClientResponseException, Optional<R>> exceptionHandler;

    /**
     * @param method e.g. GET, POST, ...
     * @param urlSegments path segments necessary for building the last part of
     * the endpoint URL
     */
    public DigitalExchangeBaseCall(HttpMethod method, String... urlSegments) {

        this.method = method;

        this.urlSegments = new String[urlSegments.length + 1];
        this.urlSegments[0] = "api";
        System.arraycopy(urlSegments, 0, this.urlSegments, 1, urlSegments.length);
    }

    protected String getURL(DigitalExchange digitalExchange) {
        return UriComponentsBuilder
                .fromHttpUrl(digitalExchange.getUrl())
                .pathSegment(urlSegments)
                .build(false) // disable encoding: it will be done by RestTemplate
                .toUriString();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpEntity<?> getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity<?> entity) {
        this.entity = entity;
    }

    /**
     * This method can be used to handle specific HTTP status codes returned by
     * the Digital Exchange instance. The exceptionHandler must return true if
     * the exception has been handled.
     */
    public void setErrorResponseHandler(Function<RestClientResponseException, Optional<R>> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    protected Optional<R> handleErrorResponse(RestClientResponseException ex) {
        if (exceptionHandler != null) {
            return exceptionHandler.apply(ex);
        }
        return Optional.empty();
    }
}
