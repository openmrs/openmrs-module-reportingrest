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
import org.openmrs.module.reporting.query.IdSet;

import java.util.ArrayList;
import java.util.List;

public class AdHocRowFilterResults {

    @JsonProperty
    private IdSet<?> result;

    @JsonProperty
    private List<IdSet<?>> individualResults;

    public IdSet<?> getResult() {
        return result;
    }

    public void setResult(IdSet<?> result) {
        this.result = result;
    }

    public List<IdSet<?>> getIndividualResults() {
        return individualResults;
    }

    public void setIndividualResults(List<IdSet<?>> individualResults) {
        this.individualResults = individualResults;
    }

    public void addResult(IdSet result) {
        if (individualResults == null) {
            individualResults = new ArrayList<IdSet<?>>();
        }
        individualResults.add(result);
    }

}
