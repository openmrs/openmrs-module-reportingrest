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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.util.StringUtils;

/**
 * {@link Resource} for evaluating {@link DataSetDefinition}s
 */
public class EvaluatedDataSetResource extends BaseDelegatingResource<DataSet> implements Retrievable {
	
	private static Log log = LogFactory.getLog(EvaluatedDataSetResource.class);
	
	public EvaluatedDataSetResource() {
		remappedProperties.put("metadata", "metaData");
	}
	
	@Override
	public Object retrieve(String uuid, RequestContext requestContext)
			throws ResponseException {
		DataSetDefinitionService dataSetDefinitionService = DefinitionContext.getDataSetDefinitionService();
		
		// evaluate the DataSet
		
		// the passed in uuid is the DataSetDefinition uuid
		DataSetDefinition definition = dataSetDefinitionService.getDefinitionByUuid(uuid);
		
		EvaluationContext evalContext = new EvaluationContext();
		HttpServletRequest httpRequest = requestContext.getRequest();
		
		// if there is a "cohort" parameter, use that to look for a CohortDefinition to run against, otherwise all patients
		String cohortUuid = httpRequest.getParameter("cohort");
		if (StringUtils.hasLength(cohortUuid)) {
			EvaluatedCohort cohort = EvaluatedCohortResource.getEvaluatedCohort(cohortUuid, requestContext, "cohort.");
			evalContext.setBaseCohort(cohort);
		}
		
		// get the params off the requestContext and put them on the evalContext
		for (Parameter param : definition.getParameters()) {
			String httpParamValue = httpRequest.getParameter(param.getName());
			
			// TODO error check if value not found?
			
			evalContext.addParameterValue(param.getName(), httpParamValue);
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
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT);
	}

	@Override
	public String getUri(Object instance) {
		// TODO, use annotation?
		return RestConstants.URI_PREFIX.replace("/rest", "/reporting") + "dataset/" + getUuidOfDataSetDefinition((DataSet) instance);
	}

	@Override
	public DataSet getByUniqueId(String uniqueId) {
		// not used
		return null;
	}

	@Override
	protected DataSet newDelegate() {
		// not used (?)
		return null;
	}

	@Override
	protected DataSet save(DataSet delegate) {
		// not used
		return null;
	}

	@Override
	protected void delete(DataSet delegate, String reason,
			RequestContext context) throws ResponseException {
		// not used
	}

	@Override
	public void purge(DataSet delegate, RequestContext context)
			throws ResponseException {
		// not used
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
	 * @param dataSet the delegate
	 * @return the uuid of the DataSet definintion that is defined on this object
	 */
	@PropertyGetter("uuid")
	public String getUuidOfDataSetDefinition(DataSet dataSet) {
		return dataSet.getDefinition().getUuid();
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
				rowMap.put(rowEntry.getKey().getName(), rowEntry.getValue());
			}
			rows.add(rowMap);
		}
		
		return rows;
	}
	
}
