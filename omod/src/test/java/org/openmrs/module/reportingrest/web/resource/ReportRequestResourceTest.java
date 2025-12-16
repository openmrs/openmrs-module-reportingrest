package org.openmrs.module.reportingrest.web.resource;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

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
		assertEquals(ReportRequest.Status.SCHEDULED, response.get("status"));
		assertEquals("0 42 15 8 2 ? 2018", response.get("schedule"));
	}

	@Test
	public void testCreateWithReportDefinitionAsParameters() throws Exception {
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
		assertEquals(ReportRequest.Status.SCHEDULED, response.get("status"));
		assertEquals(ReportRequest.Priority.NORMAL, response.get("priority"));
		SimpleObject resultObject = (SimpleObject) response.get("renderingMode");
		String rendererType = (String) resultObject.get("rendererType");
		assertEquals("org.openmrs.module.reporting.report.renderer.CsvReportRenderer", rendererType);

		ReportRequest request = getResource().getByUniqueId((String) response.get("uuid"));
		assertThat(request.getReportDefinition().getParameterizable().getUuid(), is(REPORT_DEFINITION_UUID));
		assertEquals(LocalDate.parse("2017-01-01"), convertDateParamToLocalDate("startDate", request));
		assertEquals(LocalDate.parse("2017-01-31"), convertDateParamToLocalDate("endDate", request));
		assertEquals(ReportRequest.Priority.NORMAL, request.getPriority());
		assertEquals(ReportRequest.Status.SCHEDULED, request.getStatus());
		assertEquals("org.openmrs.module.reporting.report.renderer.CsvReportRenderer!" + RENDERER_MODE_UUID,
				request.getRenderingMode().toString());
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
		assertEquals(ReportRequest.Status.REQUESTED, response.get("status"));
		assertEquals(ReportRequest.Priority.HIGHEST, response.get("priority"));
		SimpleObject resultObject = (SimpleObject) response.get("renderingMode");
		String rendererType = (String) resultObject.get("rendererType");
		assertEquals("org.openmrs.module.reporting.report.renderer.CsvReportRenderer", rendererType);

		ReportRequest request = getResource().getByUniqueId((String) response.get("uuid"));
		assertThat(request.getReportDefinition().getParameterizable().getUuid(), is(REPORT_DEFINITION_UUID));
		assertThat(request.getBaseCohort().getParameterizable().getUuid(), is(COHORT_DEFINITION_UUID));
		assertEquals(ReportRequest.Priority.HIGHEST, request.getPriority());
		assertEquals(ReportRequest.Status.REQUESTED, request.getStatus());
		assertEquals("org.openmrs.module.reporting.report.renderer.CsvReportRenderer!" + RENDERER_MODE_UUID,
				request.getRenderingMode().toString());
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

	@Test
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
		assertThrows(IllegalArgumentException.class, () -> getResource().create(properties, context));
	}

	private LocalDate convertDateParamToLocalDate(String dateParamName, ReportRequest request) {
		Date date = (Date) request.getReportDefinition().getParameterMappings().get(dateParamName);
		DateTime dateTime = new DateTime(date);
		return dateTime.toLocalDate();
	}
}