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

package org.openmrs.module.reportingrest.adhoc;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.module.reporting.cohort.definition.DefinitionLibraryCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.RowPerObjectDataSetDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.query.Query;
import org.openmrs.module.reportingrest.util.ParameterUtil;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties({"label", "value", "name", "description", "parameters"})
public class AdHocRowFilter {

    @JsonProperty
    private String type;

    @JsonProperty
    private String key;

    @JsonProperty
    private Map<String, Object> parameterValues;

    public AdHocRowFilter() {
    }

    public AdHocRowFilter(String key) {
        this.key = key;
    }

    public AdHocRowFilter(Mapped<? extends Definition> query) {
        try {
            this.key = (String) PropertyUtils.getProperty(query.getParameterizable(), "definitionKey");
            this.type = query.getParameterizable().getClass().getName();
            this.parameterValues = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : query.getParameterMappings().entrySet()) {
                if (!entry.getValue().equals("${" + entry.getKey() + "}")) {
                    this.parameterValues.put(entry.getKey(), ConversionUtil.convertToRepresentation(entry.getValue(), Representation.DEFAULT));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("query does not have a definitionKey property");
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, Object> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public Mapped<? extends Query> toQuery(Class<? extends RowPerObjectDataSetDefinition> dsdClass, AllDefinitionLibraries definitionLibraries) throws Exception {
        if (PatientDataSetDefinition.class.isAssignableFrom(dsdClass)) {
            DefinitionLibraryCohortDefinition query = new DefinitionLibraryCohortDefinition();
            query.setDefinitionKey(key);
            query.loadParameters(definitionLibraries);

            Mapped<DefinitionLibraryCohortDefinition> mapped = new Mapped<DefinitionLibraryCohortDefinition>(query,
                    ParameterUtil.convertParameterValues(query.getParameters(), parameterValues));
            ParameterUtil.mapMissingParametersStraightThrough(mapped);
            return mapped;
        }
        else {
            throw new IllegalArgumentException("Don't know how to convert to query for " + dsdClass);
        }
    }
}
