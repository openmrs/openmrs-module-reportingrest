package org.openmrs.module.reportingrest.web.resource;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

public class ReportDefinitionResourceTest extends BaseDelegatingResourceTest<ReportDefinitionResource, ReportDefinition> {

    public static final String UUID = "d9c79890-7ea9-41b1-a068-b5b99ca3d593";

    @Autowired
    ReportService reportService;

    @Autowired
    ReportDefinitionService reportDefinitionService;

    ReportDefinition reportDefinition;

    ReportDesign reportDesign;

    @Before
    public void setUp() {

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

        reportDefinition = new ReportDefinition();
        reportDefinition.setName("Report definition");
        reportDefinition.setUuid(UUID);
        reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
        reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
        reportDefinition.addDataSetDefinition("dsd1", dsd1, ParameterizableUtil.createParameterMappings("bornAfter=${startDate},bornBefore=${endDate}"));
        reportDefinition.addDataSetDefinition("dsd2", dsd2, ParameterizableUtil.createParameterMappings(""));

        reportDefinition = reportDefinitionService.saveDefinition(reportDefinition);

        reportDesign = new ReportDesign();
        reportDesign.setName("CSV Export");
        reportDesign.setRendererType(CsvReportRenderer.class);
        reportDesign.setReportDefinition(reportDefinition);
        reportDesign = reportService.saveReportDesign(reportDesign);
    }

    @Override
    public ReportDefinition newObject() {
        return reportDefinitionService.getDefinitionByUuid(UUID);
    }

    @Override
    public String getDisplayProperty() {
        return reportDefinition.getName();
    }

    @Override
    public String getUuidProperty() {
        return reportDefinition.getUuid();
    }

    @Test
    public void testRetrieveDefault() throws Exception {
        SimpleObject response = (SimpleObject) getResource().retrieve(UUID, buildRequestContext());
        assertThat(response.get("uuid"), is(reportDefinition.getUuid()));
        assertThat(response.get("name"), is(reportDefinition.getName()));
        assertThat(response.get("description"), is(reportDefinition.getDescription()));

        List<SimpleObject> parameters = response.get("parameters");
        assertThat(parameters.size(), is(2));
        assertThat(parameters.get(0).get("name"), is("startDate"));
        assertThat(parameters.get(0).get("label"), is("Start Date"));
        assertThat(parameters.get(0).get("type"), is("java.util.Date"));
        assertThat(parameters.get(0).get("required"), is(true));
        assertThat(parameters.get(0).get("display"), is("Start Date"));
        assertThat(parameters.get(1).get("name"), is("endDate"));
        assertThat(parameters.get(1).get("label"), is("End Date"));
        assertThat(parameters.get(1).get("type"), is("java.util.Date"));
        assertThat(parameters.get(1).get("required"), is(true));
        assertThat(parameters.get(1).get("display"), is("End Date"));

        List<SimpleObject> dsds = response.get("dataSetDefinitions");
        assertThat(dsds.size(), is(2));
        {
            assertThat(dsds.get(0).get("key"), is("dsd1"));
            SimpleObject mapped = dsds.get(0).get("value");
            List<SimpleObject> mappings = mapped.get("parameterMappings");
            assertThat(mappings.size(), is(2));
            mappings.sort(Comparator.comparing(a -> a.get("key").toString()));
            assertThat(mappings.get(0).get("key"), is("bornAfter"));
            assertThat(mappings.get(0).get("value"), is("${startDate}"));
            assertThat(mappings.get(1).get("key"), is("bornBefore"));
            assertThat(mappings.get(1).get("value"), is("${endDate}"));
            SimpleObject parameterizable = mapped.get("parameterizable");
            assertThat(parameterizable.get("display"), is("Not everyone"));
        }
        {
            assertThat(dsds.get(1).get("key"), is("dsd2"));
            SimpleObject mapped = dsds.get(1).get("value");
            List<SimpleObject> mappings = mapped.get("parameterMappings");
            assertThat(mappings.size(), is(0));
            SimpleObject parameterizable = mapped.get("parameterizable");
            assertThat(parameterizable.get("display"), is("For fun"));
        }

        List<SimpleObject> designs = response.get("reportDesigns");
        assertThat(designs.size(), is(1));
        SimpleObject design = designs.get(0);
        assertThat(design.get("name"), is(reportDesign.getName()));
        assertThat(design.get("display"), is(reportDesign.getName()));
        assertThat(design.get("uuid"), is(reportDesign.getUuid()));
        assertThat(design.get("rendererType"), is(reportDesign.getRendererType()));
    }

	/**
	 * @verifies throw ObjectNotFoundException if resource does not exist
	 */
	@Test
	public void testRetreiveMissing() {
		assertThrows(ObjectNotFoundException.class, () ->
                getResource().retrieve("not-existing", buildRequestContext())
        );
	}

    private RequestContext buildRequestContext(String... paramNamesAndValues) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        for (int i = 0; i < paramNamesAndValues.length; i += 2) {
            request.addParameter(paramNamesAndValues[i], paramNamesAndValues[i + 1]);
        }
        RequestContext context = new RequestContext();
        context.setRequest(request);
        return context;
    }
}
