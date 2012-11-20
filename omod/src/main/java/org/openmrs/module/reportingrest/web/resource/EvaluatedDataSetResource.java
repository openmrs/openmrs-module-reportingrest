/**
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link Resource} for evaluating {@link DataSetDefinition}s
 */
public class EvaluatedDataSetResource extends EvaluatedResource<DataSet> {
	
	private static Log log = LogFactory.getLog(EvaluatedDataSetResource.class);
	
	public EvaluatedDataSetResource() {
        resourceName = "dataset";
        remappedProperties.put("metadata", "metaData");
    }
	
	@Override
	public Object retrieve(String uuid, RequestContext requestContext)
			throws ResponseException {

		DataSetDefinitionService dataSetDefinitionService = DefinitionContext.getDataSetDefinitionService();
		
		// the passed in uuid is the DataSetDefinition uuid
		DataSetDefinition definition = dataSetDefinitionService.getDefinitionByUuid(uuid);

        EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext);

        HttpServletRequest httpRequest = requestContext.getRequest();

        // if there is a "cohort" parameter, use that to look for a CohortDefinition to run against, otherwise all patients
        String cohortUuid = httpRequest.getParameter("cohort");
        if (StringUtils.hasLength(cohortUuid)) {
            EvaluatedCohort cohort = new EvaluatedCohortResource().getEvaluatedCohort(cohortUuid, requestContext, "cohort.");
            evalContext.setBaseCohort(cohort);
        }

        // actually do the evaluation
		DataSet dataSet = null;
		try {
			dataSet = dataSetDefinitionService.evaluate(definition, evalContext);
			// there seems to be a bug in the underlying reporting module that doesn't set this
			if (dataSet.getDefinition().getUuid() == null)
				dataSet.getDefinition().setUuid(definition.getUuid());
			if (dataSet.getDefinition().getName() == null)
				dataSet.getDefinition().setName(definition.getName());
			if (dataSet.getDefinition().getDescription() == null)
				dataSet.getDefinition().setDescription(definition.getDescription());
		} catch (EvaluationException e) {
			log.error("Unable to evaluate definition with uuid: " + uuid);
		}
		
		return convertDelegateToRepresentation(dataSet, getRepresentationDescription(Representation.DEFAULT));
	}

    @Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = null;
		
		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid"); // see @PropertyGetter method below
			description.addProperty("metadata"); // remapped property
			description.addProperty("rows"); // see @PropertyGetter method below
			description.addSelfLink();
		}

		return description;
	}

	/**
	 * returns [ { col1 : val1, col2, val2 }, {col1a, val1a, col2a, val2a } ]
	 * 
	 * @param dataSet the delegate
	 * @return the a list of maps for the rows
	 */
	@PropertyGetter("rows")
	public List<Map<String, Object>> getRowsOfDataSetDefinition(DataSet dataSet) {
		
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		
		Iterator<DataSetRow> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			DataSetRow row = iterator.next();
			Map<String, Object> rowMap = new HashMap<String, Object>();
			for (Map.Entry<DataSetColumn, Object> rowEntry : row.getColumnValues().entrySet()) {
                Object value = rowEntry.getValue();

                // If the value we return has any pointers to hibernate proxies, conversion to JSON will fail when we
                // try to return it to the client. If we pass through an indicator result with an EvaluationContext,
                // its cache will likely contain hibernate proxies an break things. So we just return the numeric value,
                // and not the pointers to how we evaluated things. (Plus I don't think we really should be sending mor
                // than the value back anyway.)
                if (value instanceof IndicatorResult) {
                    value = ((IndicatorResult) value).getValue();
                }

                rowMap.put(rowEntry.getKey().getName(), value);
			}
			rows.add(rowMap);
		}
		
		return rows;
	}
	
}
