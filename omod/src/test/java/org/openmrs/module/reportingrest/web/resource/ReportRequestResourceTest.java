package org.openmrs.module.reportingrest.web.resource;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

public class ReportRequestResourceTest extends BaseModuleWebContextSensitiveTest {
	
	public static final String REPORT_REQUEST_UUID = "9b220525-9830-4dc3-9dae-43aac15b317e";
	public static final String REPORT_DEFINITION_UUID = "d9c79890-7ea9-41b1-a068-b5b99ca3d593";
	public static final String REPORT_DEFINITION_UUID_TWO = "d9c79890-7ea9-41b1-a068-b5b99ca3d595";
	public static final String COHORT_DEFINITION_UUID = "d9c79890-7ea9-41b1-a068-b5b99ca3d594";
	public static final String RENDERER_MODE_UUID = "49cf7248-d27c-11ef-a287-0242ac120002";
	
	public static final String REPORT_DEFINITION_JSON = "{"
			+ "\"parameterizable\":{"
			+ "   \"uuid\":\"" + REPORT_DEFINITION_UUID + "\""
			+ "},"
			+ "\"parameterMappings\":{"
			+ "   \"endDate\":\"2017-01-31\","
			+ "   \"startDate\":\"2017-01-01\"}"
			+ "}";
	
	public static final String REPORT_DEFINITION_JSON_TWO = "{"
			+ "\"parameterizable\":{"
			+ "   \"uuid\":\"" + REPORT_DEFINITION_UUID_TWO + "\""
			+ "},"
			+ "\"parameterMappings\":{"
			+ "   \"endDate\":\"2017-01-31\","
			+ "   \"startDate\":\"2017-01-01\"}"
			+ "}";

	public static final String RENDERER_MODE_JSON = "{"
				+ "\"renderer\":\"org.openmrs.module.reporting.report.renderer.CsvReportRenderer\","
				+ "\"label\":\"test label\","
				+ "\"argument\":\"" + RENDERER_MODE_UUID + "\","
				+ "\"sortWeight\":1000"
			+ "}";
	
	@Autowired
	private ReportDefinitionService reportDefinitionService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private CohortDefinitionService cohortDefinitionService;
	
	@Before
	public void setUp() throws Exception {
		SqlDataSetDefinition dsd1 = new SqlDataSetDefinition();
		dsd1.setName("Not everyone");
		dsd1.setDescription("via SQL");
		dsd1.setSqlQuery("select person_id, birthdate from person where voided = 0 and birthdate >= :bornAfter and birthdate < :bornBefore");
		dsd1.addParameter(new Parameter("bornAfter", "Born after", Date.class));
		dsd1.addParameter(new Parameter("bornBefore", "Born before", Date.class));
		
		SqlDataSetDefinition dsd2 = new SqlDataSetDefinition();
		dsd2.setName("For fun");
		dsd2.setDescription("another SQL query");
		dsd2.setSqlQuery("select person_id from person where voided = 0");
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Report definition");
		reportDefinition.setUuid(REPORT_DEFINITION_UUID);
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		reportDefinition.addDataSetDefinition("dsd1", dsd1, ParameterizableUtil.createParameterMappings("bornAfter=${startDate},bornBefore=${endDate}"));
		reportDefinition.addDataSetDefinition("dsd2", dsd2, ParameterizableUtil.createParameterMappings(""));
		
		reportDefinitionService.saveDefinition(reportDefinition);

		ReportDesign reportDesign = new ReportDesign();
		reportDesign.setName("CSV");
		reportDesign.setRendererType(CsvReportRenderer.class);
		reportDesign.setReportDefinition(reportDefinition);
		reportDesign.setUuid(RENDERER_MODE_UUID);
		reportService.saveReportDesign(reportDesign);


		executeDataSet("ReportRequestTest.xml");
	}
	
	private ReportRequestResource getResource() {
		return (ReportRequestResource) Context.getService(RestService.class).getResourceBySupportedClass(ReportRequest.class);
	}

	@Test
	public void testCreateScheduledReport() throws Exception {
		String reportRequestJson = "{\n" +
				"  \"status\": \"SCHEDULED\",\n" +
				"  \"priority\": \"HIGHEST\",\n" +
				"  \"priority\": \"HIGHEST\",\n" +
				"  \"reportDefinition\":" + REPORT_DEFINITION_JSON + "," +
				"  \"renderingMode\":" + RENDERER_MODE_JSON +	"," +
				"  \"schedule\": \"0 42 15 8 2 ? 2018\"\n" +
				"}";

		SimpleObject properties = SimpleObject.parseJson(reportRequestJson);
		RequestContext context = new RequestContext();
		context.setRepresentation(Representation.DEFAULT);
		SimpleObject response = (SimpleObject) getResource().create(properties, context);
		assertNotNull(response.get("uuid"));
		assertEquals(response.get("status"), ReportRequest.Status.SCHEDULED);
		assertEquals(response.get("schedule"), "0 42 15 8 2 ? 2018");
		SimpleObject resultObject = (SimpleObject) response.get("renderingMode");
	}

	@Test
	public void testCreateWithReportDefinitionAsParameters() throws Exception {
		
//		String reportRequestJson = "{\n" +
//				"  \"status\": \"REQUESTED\",\n" +
//				"  \"priority\": \"NORMAL\",\n" +
//				"  \"reportDefinition\":" + REPORT_DEFINITION_JSON + "," +
//				"  \"renderingMode\": \"org.openmrs.module.reporting.report.renderer.CsvReportRenderer\"\n" +
//				"}";

		String reportRequestJson = "{\n" +
				"  \"status\": \"REQUESTED\",\n" +
				"  \"priority\": \"NORMAL\",\n" +
				"  \"reportDefinition\":" + REPORT_DEFINITION_JSON + ",\n" +
				"  \"schedule\": \"0 56 11 ? * 3\",\n" +
				"  \"renderingMode\":" + RENDERER_MODE_JSON +
				"}";

		SimpleObject properties = SimpleObject.parseJson(reportRequestJson);
		RequestContext context = new RequestContext();
		context.setRepresentation(Representation.DEFAULT);
		SimpleObject response = (SimpleObject) getResource().create(properties, context);
		assertNotNull(response.get("uuid"));
		assertEquals(response.get("status"), ReportRequest.Status.SCHEDULED);
		assertEquals(response.get("priority"), ReportRequest.Priority.NORMAL);
		SimpleObject resultObject = (SimpleObject) response.get("renderingMode");
		String rendererType = (String) resultObject.get("rendererType");
		assertEquals(rendererType, "org.openmrs.module.reporting.report.renderer.CsvReportRenderer");

		ReportRequest request = getResource().getByUniqueId((String) response.get("uuid"));
		assertThat(request.getReportDefinition().getParameterizable().getUuid(), is(REPORT_DEFINITION_UUID));
		assertEquals(request.getReportDefinition().getParameterMappings().get("startDate").toString(), "Sun Jan 01 00:00:00 CET 2017");
		assertEquals(request.getReportDefinition().getParameterMappings().get("endDate").toString(), "Tue Jan 31 00:00:00 CET 2017");
		assertEquals(request.getPriority(), ReportRequest.Priority.NORMAL);
		assertEquals(request.getStatus(), ReportRequest.Status.SCHEDULED);
		assertEquals(request.getRenderingMode().toString(), "org.openmrs.module.reporting.report.renderer.CsvReportRenderer!" + RENDERER_MODE_UUID);
		assertNull(request.getBaseCohort());
	}

	@Test
	public void testCreateWithReportDefinitionAndBaseCohortAsParameters() throws Exception {
		
		String cohortDefinitionJson = "{"
				+ "\"parameterizable\":{"
				+ "   \"uuid\":\"" + COHORT_DEFINITION_UUID + "\""
				+ "}"
				+ "}";

		String reportRequestJson = "{\n" +
				"  \"status\": \"REQUESTED\",\n" +
				"  \"priority\": \"NORMAL\",\n" +
				"  \"reportDefinition\":" + REPORT_DEFINITION_JSON + "," +
				"  \"baseCohort\":" + cohortDefinitionJson + "," +
				"  \"renderingMode\":" + RENDERER_MODE_JSON +
				"}";

		SimpleObject properties = SimpleObject.parseJson(reportRequestJson);
		RequestContext context = new RequestContext();
		context.setRepresentation(Representation.DEFAULT);

		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("Males");
		cd.setDescription("male patients");
		cd.setMaleIncluded(true);
		cd.setFemaleIncluded(false);
		cd.setUnknownGenderIncluded(false);
		cd.setUuid(COHORT_DEFINITION_UUID);
		cohortDefinitionService.saveDefinition(cd);

		SimpleObject response = (SimpleObject) getResource().create(properties, context);
		assertNotNull(response.get("uuid"));
		assertEquals(response.get("status"), ReportRequest.Status.REQUESTED);
		assertEquals(response.get("priority"), ReportRequest.Priority.HIGHEST);
		SimpleObject resultObject = (SimpleObject) response.get("renderingMode");
		String rendererType = (String) resultObject.get("rendererType");
		assertEquals(rendererType, "org.openmrs.module.reporting.report.renderer.CsvReportRenderer");

		ReportRequest request = getResource().getByUniqueId((String) response.get("uuid"));
		assertThat(request.getReportDefinition().getParameterizable().getUuid(), is(REPORT_DEFINITION_UUID));
		assertThat(request.getBaseCohort().getParameterizable().getUuid(), is(COHORT_DEFINITION_UUID));
		assertEquals(request.getPriority(), ReportRequest.Priority.HIGHEST);
		assertEquals(request.getStatus(), ReportRequest.Status.REQUESTED);
		assertEquals(request.getRenderingMode().toString(), "org.openmrs.module.reporting.report.renderer.CsvReportRenderer!" + RENDERER_MODE_UUID);
	}
	
	@Test
	public void testGetOne() throws Exception {
		ReportRequest request = getResource().getByUniqueId(REPORT_REQUEST_UUID);
		assertThat(request.getUuid(), is(REPORT_REQUEST_UUID));
		assertThat(request.getStatus(), is(ReportRequest.Status.SAVED));
		assertThat(request.getReportDefinition().getParameterizable().getUuid(), is(REPORT_DEFINITION_UUID));
	}
	
	@Test
	public void testSearchByReportDefinition() throws Exception {
		SimpleObject search = getResource().search(buildRequestContext("reportDefinition", REPORT_DEFINITION_UUID));
		List<SimpleObject> results = (List<SimpleObject>) search.get("results");
		assertThat(results.size(), is(1));
		assertThat((String) results.get(0).get("uuid"), is(REPORT_REQUEST_UUID));
	}
	
	protected RequestContext buildRequestContext(String... paramNamesAndValues) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		for (int i = 0; i < paramNamesAndValues.length; i += 2) {
			request.addParameter(paramNamesAndValues[i], paramNamesAndValues[i + 1]);
		}
		RequestContext context = new RequestContext();
		context.setStartIndex(1);
		context.setLimit(50);
		context.setRequest(request);
		return context;
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThrowsIllegalArgumentExceptionWhenRequiredParametersAreMissing() throws Exception {
		String reportDefinitionJson = "{"
				+ "\"parameterizable\":{"
				+ "   \"uuid\":\"" + REPORT_DEFINITION_UUID + "\""
				+ "}"
				+ "}";

		String reportRequestJson = "{\n" +
				"  \"status\": \"REQUESTED\",\n" +
				"  \"priority\": \"NORMAL\",\n" +
				"  \"reportDefinition\":" + reportDefinitionJson + "," +
				"  \"renderingMode\":" + RENDERER_MODE_JSON +
				"}";

		SimpleObject properties = SimpleObject.parseJson(reportRequestJson);
		RequestContext context = new RequestContext();
		context.setRepresentation(Representation.DEFAULT);
		getResource().create(properties, context);
	}
	
}