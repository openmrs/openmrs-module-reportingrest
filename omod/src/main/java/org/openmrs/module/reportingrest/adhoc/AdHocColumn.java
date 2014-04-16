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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.module.reporting.data.BaseDefinitionLibraryDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.DefinitionLibraryPatientDataDefinition;
import org.openmrs.module.reporting.dataset.column.definition.RowPerObjectColumnDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reportingrest.util.ParameterUtil;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties({"label", "value", "parameters"})
public class AdHocColumn {

    @JsonProperty
    private String type;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private Map<String, Object> parameterValues;

    public AdHocColumn() {
    }

    public AdHocColumn(RowPerObjectColumnDefinition definition) {
        if (definition.getDataDefinition().getParameterizable() instanceof BaseDefinitionLibraryDataDefinition) {
            BaseDefinitionLibraryDataDefinition def = (BaseDefinitionLibraryDataDefinition) definition.getDataDefinition().getParameterizable();
            this.key = def.getDefinitionKey();
            this.type = def.getClass().getName();
            this.name = definition.getName();
            this.description = definition.getDescription();
            this.parameterValues = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : definition.getDataDefinition().getParameterMappings().entrySet()) {
                if (!entry.getValue().equals("${" + entry.getKey() + "}")) {
                    this.parameterValues.put(entry.getKey(), ConversionUtil.convertToRepresentation(entry.getValue(), Representation.DEFAULT));
                }
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported column type: " + definition.getDataDefinition().getParameterizable().getClass());
        }
    }

    public RowPerObjectColumnDefinition toColumnDefinition(AllDefinitionLibraries definitionLibraries) throws Exception {
        DefinitionLibraryPatientDataDefinition dataDefinition = new DefinitionLibraryPatientDataDefinition(key);
        dataDefinition.loadParameters(definitionLibraries);

        RowPerObjectColumnDefinition col = new RowPerObjectColumnDefinition(name, dataDefinition, ParameterUtil.convertParameterValues(dataDefinition.getParameters(), parameterValues));
        col.setDescription(description);
        ParameterUtil.mapMissingParametersStraightThrough(col.getDataDefinition());
        return col;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, Object> parameterValues) {
        this.parameterValues = parameterValues;
    }
}
