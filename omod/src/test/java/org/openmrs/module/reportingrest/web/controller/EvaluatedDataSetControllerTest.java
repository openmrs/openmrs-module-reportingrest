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
package org.openmrs.module.reportingrest.web.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EvaluatedDataSetControllerTest extends BaseModuleWebContextSensitiveTest {

//	EvaluatedDataSetController controller;
//	MockHttpServletRequest request;
//	MockHttpServletResponse response;
//
//	@Before
//	public void before() {
//		controller = new EvaluatedDataSetController();
//		request = new MockHttpServletRequest();
//		response = new MockHttpServletResponse();
//	}
//
//	@SuppressWarnings("rawtypes")
//    @Test
//	public void shouldEvaluateADSDWithNoParams() throws Exception {
//        DataSetDefinitionService dataSetDefinitionService = Context.getService(DataSetDefinitionService.class);
//
//        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
//        dsd.setName("Everyone");
//        dsd.setDescription("via SQL");
//        dsd.setSqlQuery("select person_id, birthdate from person where voided = 0");
//        dsd.setUuid("no-params");
//        dataSetDefinitionService.saveDefinition(dsd);
//
//		Object object = controller.retrieve("no-params", request);
//		printJson(object);
//
//		Assert.assertEquals("no-params", path(object, "uuid"));
//		Iterator cols = ((Collection) path(object, "metadata.columns")).iterator();
//		{
//			Object col = cols.next();
//			Assert.assertEquals("PERSON_ID", path(col, "name"));
//			Assert.assertEquals("PERSON_ID", path(col, "label"));
//			Assert.assertEquals("java.lang.Integer", path(col, "datatype"));
//		}
//		{
//			Object col = cols.next();
//			Assert.assertEquals("BIRTHDATE", path(col, "name"));
//			Assert.assertEquals("BIRTHDATE", path(col, "label"));
//			Assert.assertEquals("java.sql.Timestamp", path(col, "datatype"));
//		}
//	}
//
//    @Test
//    public void shouldHandleParams() throws Exception {
//        DataSetDefinitionService dataSetDefinitionService = Context.getService(DataSetDefinitionService.class);
//
//        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
//        dsd.setName("Everyone");
//        dsd.setDescription("via SQL");
//        dsd.setSqlQuery("select person_id, birthdate from person where voided = 0 and birthdate > :param2");
//        dsd.addParameter(new Parameter("param1", "param 1", String.class));
//        dsd.addParameter(new Parameter("param2", "param 2", Date.class));
//        dsd.setUuid("params");
//        dataSetDefinitionService.saveDefinition(dsd);
//
//        request.addParameter("param1", "these are words, that we won't use");
//        request.addParameter("param2", "2000-11-01");
//        Object object = controller.retrieve("params", request);
//        printJson(object);
//
//        assertEquals("params", path(object, "uuid"));
//        Collection rows = (Collection) path(object, "rows");
//        assertThat(rows.size(), is(1));
//        Object row = rows.iterator().next();
//        assertEquals(6, path(row, "PERSON_ID"));
//    }
//
//    @Test
//    public void shouldConvertCohortIndicatorDataSet() throws Exception {
//        DataSetDefinitionService dataSetDefinitionService = Context.getService(DataSetDefinitionService.class);
//        {
//            GenderCohortDefinition cd = new GenderCohortDefinition();
//            cd.setName("Gender = Male");
//            cd.setMaleIncluded(true);
//
//            CohortIndicator ind = new CohortIndicator("Gender = Male");
//            ind.setCohortDefinition(map(cd, null));
//
//            CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
//            dsd.setName("Cohort Indicator DSD");
//            dsd.setUuid("cohort-indicator-dsd");
//            dsd.addColumn("1", "One", map(ind, null), "");
//
//            dataSetDefinitionService.saveDefinition(dsd);
//        }
//
//        Object result = controller.retrieve("cohort-indicator-dsd", new MockHttpServletRequest());
//        printJson(result);
//
//        // I'm not asserting anything, because the success criteria is that we successfully converted the result to JSON.
//    }
//
//    private void printJson(Object object) throws IOException {
//        System.out.println(new ObjectMapper().writeValueAsString(object));
//    }
//
//    public static Object path(Object object, String path) {
//		String[] components = path.split("\\.");
//		for (String s : components) {
//			try {
//	            object = PropertyUtils.getProperty(object, s);
//            }
//            catch (Exception ex) {
//	            return null;
//            }
//		}
//		return object;
//	}
//
//    private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
//        if (parameterizable == null) {
//            throw new NullPointerException("Programming error: missing parameterizable");
//        }
//        if (mappings == null) {
//            mappings = ""; // probably not necessary, just to be safe
//        }
//        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
//    }

}
