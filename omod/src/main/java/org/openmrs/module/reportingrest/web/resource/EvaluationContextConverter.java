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

import org.apache.commons.beanutils.BeanUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;


/**
 * Serializes EvaluationContext
 */
@Handler(supports = EvaluationContext.class, order = 0)
public class EvaluationContextConverter implements Converter<EvaluationContext> {

	@Override
	public EvaluationContext newInstance(String type) {
		return new EvaluationContext();
	}

	@Override
	public EvaluationContext getByUniqueId(String string) {
		throw new UnsupportedOperationException("Cannot retrieve EvaluationContext by id");
	}

	@Override
	public SimpleObject asRepresentation(EvaluationContext instance, Representation rep) throws ConversionException {
		SimpleObject result = new SimpleObject();

		result.add("evaluationId", instance.getEvaluationId());
		result.add("evaluationDate", convertRecursively(instance.getEvaluationDate()));
		result.add("evaluationLevel", instance.getEvaluationLevel());
		result.add("limit", instance.getLimit());
		result.add("baseCohort", convertRecursively(instance.getBaseCohort()));
		result.add("contextValues", convertRecursively(instance.getContextValues()));
		result.add("parameterValues", convertRecursively(instance.getParameterValues()));

		return result;
	}

	public Object convertRecursively(Object obj) {
		return ConversionUtil.convertToRepresentation(obj, Representation.REF);
	}

	@Override
	public Object getProperty(EvaluationContext instance, String propertyName) throws ConversionException {
		try {
			return BeanUtils.getProperty(instance, propertyName);
		} catch (Exception e) {
			throw new ConversionException("Cannot read " + propertyName + " from  EvaluationContext", e);
		}
	}

	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		try {
			BeanUtils.setProperty(instance, propertyName, value);
		} catch (Exception e) {
			throw new ConversionException("Cannot set " + propertyName + " on EvaluationContext", e);
		}
	}
}
