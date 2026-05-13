package org.openmrs.module.reportingrest.web.controller;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.openmrs.module.webservices.rest.OpenmrsPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RunReportTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    ReportDefinitionService reportDefinitionService;

    @Autowired
    ReportService reportService;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    private List<RequestMappingHandlerMapping> handlerMappings;

    private final OpenmrsPathMatcher pathMatcher = new OpenmrsPathMatcher();

    @Test
    public void runReport_shouldEvaluateAndRenderCsvInSingleCall() throws Exception {
        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName("counts");
        dsd.setSqlQuery("select count(*) as total_persons from person where voided = 0");

        ReportDefinition rd = new ReportDefinition();
        rd.setName("Test Count Report");
        rd.addDataSetDefinition("counts", dsd, ParameterizableUtil.createParameterMappings(""));
        reportDefinitionService.saveDefinition(rd);

        ReportDesign design = new ReportDesign();
        design.setName("CSV");
        design.setRendererType(CsvReportRenderer.class);
        design.setReportDefinition(rd);
        reportService.saveReportDesign(design);

        MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.POST.toString(),
                "/rest/v1/reportingrest/runReport");
        request.addHeader("content-type", "application/json");
        request.setParameter("reportDefinitionUuid", rd.getUuid());
        request.setParameter("reportDesignUuid", design.getUuid());

        MockHttpServletResponse response = handle(request);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getContentType().contains("text/csv"));
        String body = response.getContentAsString();
        Assert.assertTrue(body.trim().length() > 0);
    }

    @Test
    public void runReport_shouldRenderHtmlTemplateToPdf() throws Exception {
        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName("info");
        dsd.setSqlQuery("select 'hello' as greeting");

        ReportDefinition rd = new ReportDefinition();
        rd.setName("Test PDF Report");
        rd.addDataSetDefinition("info", dsd, ParameterizableUtil.createParameterMappings(""));
        reportDefinitionService.saveDefinition(rd);

        String html = "<!DOCTYPE html><html><body><p>#greeting#</p></body></html>";

        ReportDesignResource templateResource = new ReportDesignResource();
        templateResource.setName("template");
        templateResource.setExtension("html");
        templateResource.setContentType("text/html");
        templateResource.setContents(html.getBytes(StandardCharsets.UTF_8));

        ReportDesign design = new ReportDesign();
        design.setName("PDF Label");
        design.setRendererType(org.openmrs.module.reporting.report.renderer.PdfTemplateRenderer.class);
        design.setReportDefinition(rd);
        design.addResource(templateResource);
        templateResource.setReportDesign(design);
        reportService.saveReportDesign(design);

        MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.POST.toString(),
                "/rest/v1/reportingrest/runReport");
        request.addHeader("content-type", "application/json");
        request.setParameter("reportDefinitionUuid", rd.getUuid());
        request.setParameter("reportDesignUuid", design.getUuid());

        MockHttpServletResponse response = handle(request);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/pdf", response.getContentType());
        Assert.assertTrue(response.getContentAsByteArray().length > 0);
    }

    @Test
    public void runReport_shouldPassParametersToEvaluation() throws Exception {
        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName("filtered");
        dsd.addParameter(new Parameter("gender", "Gender", String.class));
        dsd.setSqlQuery("select count(*) as total from person where gender = :gender and voided = 0");

        ReportDefinition rd = new ReportDefinition();
        rd.setName("Filtered Count Report");
        rd.addParameter(new Parameter("gender", "Gender", String.class));
        rd.addDataSetDefinition("filtered", dsd, Mapped.straightThroughMappings(rd));
        reportDefinitionService.saveDefinition(rd);

        ReportDesign design = new ReportDesign();
        design.setName("CSV");
        design.setRendererType(CsvReportRenderer.class);
        design.setReportDefinition(rd);
        reportService.saveReportDesign(design);

        MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.POST.toString(),
                "/rest/v1/reportingrest/runReport");
        request.addHeader("content-type", "application/json");
        request.setParameter("reportDefinitionUuid", rd.getUuid());
        request.setParameter("reportDesignUuid", design.getUuid());
        request.setParameter("gender", "M");

        MockHttpServletResponse response = handle(request);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getContentType().contains("text/csv"));
    }

    MockHttpServletResponse handle(HttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerExecutionChain chain = null;
        for (RequestMappingHandlerMapping mapping : handlerMappings) {
            mapping.setPathMatcher(pathMatcher);
            chain = mapping.getHandler(request);
            if (chain != null) break;
        }
        Assert.assertNotNull("No handler found for request URI", chain);
        handlerAdapter.handle(request, response, chain.getHandler());
        return response;
    }
}
