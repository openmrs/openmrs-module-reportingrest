/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.reportingrest.web;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.reporting.query.IdSet;
import org.openmrs.module.reportingrest.SimpleIdSet;

import java.util.List;
import java.util.Map;

/**
 * Used for evaluating a list of queries from definition libraries.
 */
class AdHocQueryDeleteMe {

    @JsonProperty
    private String definitionType;

    @JsonProperty
    private List<Query> queries;

    @JsonProperty
    private String composition;

    @JsonProperty
    private IdSet<?> result;

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public IdSet<?> getResult() {
        return result;
    }

    public void setResult(IdSet<?> result) {
        this.result = result;
    }

    public static class Query {

        @JsonProperty
        private String definitionKey;

        @JsonProperty
        private Map<String, Object> parameterValues;

        @JsonProperty
        private IdSet<?> result;

        public String getDefinitionKey() {
            return definitionKey;
        }

        public void setDefinitionKey(String definitionKey) {
            this.definitionKey = definitionKey;
        }

        public Map<String, Object> getParameterValues() {
            return parameterValues;
        }

        public void setParameterValues(Map<String, Object> parameterValues) {
            this.parameterValues = parameterValues;
        }

        public IdSet<?> getResult() {
            return result;
        }

        public void setResult(IdSet<?> result) {
            this.result = result;
        }
    }

}
