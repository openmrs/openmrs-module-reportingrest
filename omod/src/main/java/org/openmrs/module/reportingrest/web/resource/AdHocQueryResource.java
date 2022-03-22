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

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.Cohorts;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DefinitionLibraryCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.data.patient.definition.DefinitionLibraryPatientDataDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.IdSet;
import org.openmrs.module.reporting.query.Query;
import org.openmrs.module.reportingrest.SimpleIdSet;
import org.openmrs.module.reportingrest.adhoc.AdHocColumn;
import org.openmrs.module.reportingrest.adhoc.AdHocDataSet;
import org.openmrs.module.reportingrest.adhoc.AdHocParameter;
import org.openmrs.module.reportingrest.adhoc.AdHocRowFilter;
import org.openmrs.module.reportingrest.util.ParameterUtil;
import org.openmrs.module.reportingrest.web.AdHocRowFilterResults;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for <em>evaluating</em> a list of queries +/- columns that refer to definition libraries on the fly.
 *
 * If you want to persist an AdHocDataSet for later usage
 * @see {@link org.openmrs.module.reportingrest.web.resource.AdHocDataSetResource}
 *
 * If you POST with v=rowFilters, you will get back detailed results about the row filters, otherwise you will
 * get back the result of evaluating the data set itself
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/adhocquery",
        supportedClass = AdHocDataSet.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*", "1.10.*, 1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*", "2.6.*"})
public class AdHocQueryResource implements Creatable {

    private AllDefinitionLibraries libraries;

    /**
     * Resources are not Spring beans, so we can't autowire the libraries property.
     * @return
     */
    private AllDefinitionLibraries getLibraries() {
        if (libraries == null) {
            libraries = Context.getRegisteredComponents(AllDefinitionLibraries.class).get(0);
        }
        return libraries;
    }

    @Override
    public Object create(SimpleObject post, RequestContext context) throws ResponseException {
        ObjectMapper jackson = new ObjectMapper();
        AdHocDataSet adHocDataSet = jackson.convertValue(post, AdHocDataSet.class);

        boolean previewMode = context.getRepresentation().getRepresentation().equals("rowFilters") || context.getRepresentation().getRepresentation().equals("preview");
        AdHocRowFilterResults rowFilterResults = new AdHocRowFilterResults();

        if (!PatientDataSetDefinition.class.getName().equals(adHocDataSet.getType())) {
            throw new IllegalArgumentException("So far we only support ad hoc queries of PatientDataSetDefinition");
        }

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        for (AdHocParameter adHocParameter : adHocDataSet.getParameters()) {
            try {
                dsd.addParameter(adHocParameter.toParameter());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid type or collectionType in parameter", e);
            }
        }

        int colNumber = 0;
        for (AdHocColumn column : adHocDataSet.getColumns()) {
            colNumber += 1;
            // {
            //   "type":"org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition",
            //   "key":"reporting.patientDataCalculation.patientId",
            //   "name":"reporting.patientDataCalculation.patientId.name",
            //   "description":"reporting.patientDataCalculation.patientId.description",
            //   "parameters":[]
            // }
            // if we have parameters they are like
            // {
            //   "name":"effectiveDate",
            //   "type":"java.util.Date",
            //   "collectionType":null,
            //   "value":"2013-04-03T04:00:00.000Z"
            // }

            DefinitionLibraryPatientDataDefinition definition = new DefinitionLibraryPatientDataDefinition(column.getKey());
            definition.loadParameters(getLibraries());
            Map<String, Object> mappings = Mapped.straightThroughMappings(definition);

            try {
                definition.setParameterValues(ParameterUtil.convertParameterValues(definition.getParameters(), column.getParameterValues()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Error in column " + colNumber, e);
            }

            String columnName = column.getName();
            if (previewMode) {
                columnName = "(" + colNumber + ") " + columnName;
            }
            dsd.addColumn(columnName, definition, mappings);
        }

        CohortDefinitionService cohortDefinitionService = Context.getService(CohortDefinitionService.class);
        EvaluationContext evaluationContext = new EvaluationContext();
        try {
            evaluationContext.setParameterValues(ParameterUtil.convertParameterValues(dsd.getParameters(), adHocDataSet.getParameterValues()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error in parameter", e);
        }

        List<Mapped> queries = new ArrayList<Mapped>();
        for (AdHocRowFilter rowFilter : adHocDataSet.getRowFilters()) {
            DefinitionLibraryCohortDefinition cd = new DefinitionLibraryCohortDefinition(rowFilter.getKey());
            cd.loadParameters(getLibraries());
            try {
                cd.setParameterValues(ParameterUtil.convertParameterValues(cd.getParameters(), rowFilter.getParameterValues()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Error in rowFilter " + rowFilter.getKey(), e);
            }
            Mapped<? extends Query> mappedQuery = mapMissingParametersStraightThrough(cd, rowFilter.getParameterValues());
            queries.add(mappedQuery);

            try {
                EvaluatedCohort evaluated = cohortDefinitionService.evaluate((Mapped<CohortDefinition>) mappedQuery, evaluationContext);
                rowFilterResults.addResult(simplify(evaluated));
            } catch (EvaluationException e) {
                throw new IllegalStateException("Failed to evaluate: " + rowFilter.getKey(), e);
            }
        }

        EvaluatedCohort allRows;
        if (queries.size() > 0) {
            CompositionCohortDefinition cd = new CompositionCohortDefinition();
            cd.setParameters(dsd.getParameters());
            for (int i = 0; i < queries.size(); ++i) {
                Mapped<CohortDefinition> mapped = queries.get(i);
                cd.addSearch("" + (i + 1), mapped);
            }
            if (StringUtils.isEmpty(adHocDataSet.getCustomRowFilterCombination())) {
                adHocDataSet.setCustomRowFilterCombination(defaultCompositionString(queries.size()));
            }
            cd.setCompositionString(adHocDataSet.getCustomRowFilterCombination());

            try {
                allRows = cohortDefinitionService.evaluate(cd, evaluationContext);
                rowFilterResults.setResult(new SimpleIdSet(allRows.getMemberIds()));
            } catch (EvaluationException e) {
                throw new IllegalArgumentException("Failed to evaluate composition: " + adHocDataSet.getCustomRowFilterCombination(), e);
            }

            if (context.getRepresentation().getRepresentation().equals("rowFilters")) {
                return rowFilterResults;
            }

            Cohort cohort;
            if (context.getRepresentation().getRepresentation().equals("preview")) {
                // for preview purposes, we just evaluate on a small number of rows
                cohort = new Cohort();
                int j = 0;
                for (Integer member : rowFilterResults.getResult().getMemberIds()) {
                    j += 1;
                    cohort.addMember(member);
                    if (j >= 10) {
                        break;
                    }
                }
            } else {
                cohort = allRows;
            }

            evaluationContext.setBaseCohort(cohort);
        }
        else { // no row filters
            allRows = Cohorts.allPatients(evaluationContext);
            if (context.getRepresentation().getRepresentation().equals("preview")) {
                Cohort cohort = new Cohort();
                int j = 0;
                for (Integer member : allRows.getMemberIds()) {
                    j += 1;
                    cohort.addMember(member);
                    if (j >= 10) {
                        break;
                    }
                }
                evaluationContext.setBaseCohort(cohort);
            }
        }

        DataSet data = null;
        try {
            data = Context.getService(DataSetDefinitionService.class).evaluate(dsd, evaluationContext);
        } catch (EvaluationException e) {
            throw new IllegalArgumentException("Error evaluating preview columns", e);
        }

        SimpleObject o = (SimpleObject) ConversionUtil.convertToRepresentation(data, Representation.DEFAULT);
        if (previewMode) {
            o.put("allRows", simplify(allRows));
        }
        return o;

    }

    private Mapped<Query> mapMissingParametersStraightThrough(Query cd, Map<String, Object> parameterValues) {
        Map<String, Object> mappings = new HashMap<String, Object>();
        for (Parameter parameter : cd.getParameters()) {
            if (parameterValues == null || parameterValues.get(parameter.getName()) == null) {
                mappings.put(parameter.getName(), parameter.getExpression());
            }
        }

        return new Mapped<Query>(cd, mappings);
    }

    private IdSet simplify(IdSet<?> complex) {
        return new SimpleIdSet(complex.getMemberIds());
    }

    private String defaultCompositionString(int size) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i <= size; ++i) {
            list.add(i);
        }
        return OpenmrsUtil.join(list, " AND ");
    }

    @Override
    public String getUri(Object instance) {
        return null;
    }

}
