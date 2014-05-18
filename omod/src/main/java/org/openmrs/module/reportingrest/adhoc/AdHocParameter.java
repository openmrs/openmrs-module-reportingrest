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

import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;

import java.util.Collection;

public class AdHocParameter {

    @JsonProperty
    private String name;

    // @JsonProperty annotation is on accessors, for localization
    private String label;

    @JsonProperty
    private String type;

    @JsonProperty
    private String collectionType;

    @JsonProperty
    private Object value;

    public AdHocParameter() {
    }

    public AdHocParameter(Parameter parameter) {
        this.name = parameter.getName();
        this.label = parameter.getLabel();
        this.type = parameter.getType().getName();
        if (parameter.getCollectionType() != null) {
            this.collectionType = parameter.getCollectionType().getName();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getLabel() {
        return label == null ? null : Context.getMessageSourceService().getMessage(label);
    }

    @JsonProperty
    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Parameter toParameter() throws ClassNotFoundException {
        Parameter p = new Parameter();
        p.setName(name);
        p.setLabel(label);
        p.setType(Context.loadClass(type));
        if (collectionType != null) {
            p.setCollectionType((Class<? extends Collection>) Context.loadClass(collectionType));
        }
        return p;
    }

}
