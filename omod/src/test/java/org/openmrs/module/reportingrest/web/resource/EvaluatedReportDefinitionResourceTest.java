package org.openmrs.module.reportingrest.web.resource;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 *
 */
public class EvaluatedReportDefinitionResourceTest extends BaseEvaluatedResourceTest<EvaluatedReportDefinitionResource, ReportData> {

    public static final String UUID = "d9c79890-7ea9-41b1-a068-b5b99ca3d593";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ReportDefinitionService reportDefinitionService;

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
        reportDefinition.setUuid(UUID);
        reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
        reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
        reportDefinition.addDataSetDefinition("dsd1", dsd1, ParameterizableUtil.createParameterMappings("bornAfter=${startDate},bornBefore=${endDate}"));
        reportDefinition.addDataSetDefinition("dsd2", dsd2, ParameterizableUtil.createParameterMappings(""));

        reportDefinitionService.saveDefinition(reportDefinition);
    }

    @Test
    public void testEvaluating() throws Exception {
        SimpleObject response = (SimpleObject) getResource().retrieve(UUID, buildRequestContext("startDate", "1975-01-01", "endDate", "1976-12-31"));
        System.out.println(toJson(response));
    }

}
