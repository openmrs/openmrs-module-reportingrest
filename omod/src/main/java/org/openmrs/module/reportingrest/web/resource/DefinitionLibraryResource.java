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

package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.definition.library.LibraryDefinitionSummary;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.CrudResource;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/definitionlibrary",
        supportedClass = Definition.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*"})
public class DefinitionLibraryResource implements CrudResource, Searchable {

    static Map<String, Class<? extends Definition>> types;
    static {
        types = new HashMap<String, Class<? extends Definition>>();
        types.put("cohort", CohortDefinition.class);
        types.put("dataSet", DataSetDefinition.class);
        types.put("encounterQuery", EncounterQuery.class);
        types.put("patientData", PatientDataDefinition.class);
        types.put("encounterData", EncounterDataDefinition.class);
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT);
    }

    @Override
    public SimpleObject search(RequestContext context) throws ResponseException {
        Class<? extends Definition> definitionClass = getDefinitionClass(context, "q");
        List<LibraryDefinitionSummary> summaries = getLibraries().getDefinitionSummaries(definitionClass);
        SimpleObject results = new SimpleObject();
        results.put("results", summaries);
        return results;
    }

    private Class<? extends Definition> getDefinitionClass(RequestContext context, String paramName) {
        String typeName = context.getParameter(paramName);
        Class<? extends Definition> definitionClass = types.get(typeName);
        if (definitionClass == null) {
            try {
                definitionClass = (Class<? extends Definition>) Context.loadClass(typeName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unknown type: " + typeName);
            }
        }
        return definitionClass;
    }

    @Override
    public Object retrieve(String uuid, RequestContext context) throws ResponseException {
        Definition definition = getLibraries().getDefinition(null, uuid);
        if (definition == null) {
            throw new ObjectNotFoundException();
        }
        return ConversionUtil.convertToRepresentation(definition, context.getRepresentation());
    }

    @Override
    public String getUri(Object instance) {
        return RestConstants.URI_PREFIX + RestConstants.VERSION_1 +
                "/reportingrest/definitionlibrary/" + ((Definition) instance).getUuid();
    }

    @Override
    public Object create(SimpleObject simpleObject, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void delete(String s, String s2, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void purge(String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Object update(String s, SimpleObject simpleObject, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    private AllDefinitionLibraries getLibraries() {
        return Context.getRegisteredComponents(AllDefinitionLibraries.class).get(0);
    }

}
