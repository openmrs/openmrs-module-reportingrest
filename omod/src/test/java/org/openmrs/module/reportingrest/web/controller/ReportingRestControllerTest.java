package org.openmrs.module.reportingrest.web.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.webservices.rest.OpenmrsPathMatcher;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ReportingRestControllerTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    ReportDefinitionService reportDefinitionService;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    private List<RequestMappingHandlerMapping> handlerMappings;

    private final OpenmrsPathMatcher pathMatcher = new OpenmrsPathMatcher();

    ReportDefinition setupReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(UUID.randomUUID().toString());
        rd.setName("Test Report");
        rd.addParameter(new Parameter("gender", "Gender", String.class));
        SqlDataSetDefinition names = new SqlDataSetDefinition();
        names.addParameter(new Parameter("gender", "Gender", String.class));
        names.setSqlQuery("select p.person_id, n.given_name, n.family_name from person p inner join person_name n on p.person_id = n.person_id where p.gender = :gender");
        rd.addDataSetDefinition("names", names, Mapped.straightThroughMappings(rd));
        SqlDataSetDefinition birthdates = new SqlDataSetDefinition();
        birthdates.addParameter(new Parameter("gender", "Gender", String.class));
        birthdates.setSqlQuery("select person_id, birthdate from person where gender = :gender");
        rd.addDataSetDefinition("birthdates", birthdates, Mapped.straightThroughMappings(rd));
        return reportDefinitionService.saveDefinition(rd);
    }

    @Test
    public void testEvaluateReportDataSet() throws Exception {
        ReportDefinition rd = setupReportDefinition();
        Map<String, SimpleObject> dataSets = new LinkedHashMap<>();
        for (String key : rd.getDataSetDefinitions().keySet()) {
            String restUrl = "/reportDataSet/" + rd.getUuid() + "/" + key;
            MockHttpServletResponse response = handle(getRequest(restUrl, "gender", "F"));
            dataSets.put(key, deserialize(response));
        }
        assertThat(dataSets.size(), equalTo(2));
        {
            SimpleObject names = dataSets.get("names");
            List<Map> columns = (List<Map>) ((Map) names.get("metadata")).get("columns");
            assertThat(columns.size(), equalTo(3));
            assertThat(columns.get(0).get("name"), equalTo("PERSON_ID"));
            assertThat(columns.get(1).get("name"), equalTo("GIVEN_NAME"));
            assertThat(columns.get(2).get("name"), equalTo("FAMILY_NAME"));
            List<Map> rows = names.get("rows");
            assertThat(rows.size(), equalTo(3));
            assertRow(rows.get(0), "PERSON_ID", 7, "GIVEN_NAME", "Collet", "FAMILY_NAME", "Chebaskwony");
            assertRow(rows.get(1), "PERSON_ID", 8, "GIVEN_NAME", "Anet", "FAMILY_NAME", "Oloo");
            assertRow(rows.get(2), "PERSON_ID", 501, "GIVEN_NAME", "Bruno", "FAMILY_NAME", "Otterbourg");
        }
        {
            SimpleObject birthdates = dataSets.get("birthdates");
            List<Map> columns = (List<Map>) ((Map) birthdates.get("metadata")).get("columns");
            assertThat(columns.size(), equalTo(2));
            assertThat(columns.get(0).get("name"), equalTo("PERSON_ID"));
            assertThat(columns.get(1).get("name"), equalTo("BIRTHDATE"));
            List<Map> rows = birthdates.get("rows");
            assertThat(rows.size(), equalTo(3));
            assertRow(rows.get(0), "PERSON_ID", 7, "BIRTHDATE", formatIsoDate("1976-08-25"));
            assertRow(rows.get(1), "PERSON_ID", 8, "BIRTHDATE", null);
            assertRow(rows.get(2), "PERSON_ID", 501, "BIRTHDATE", null);
        }
    }

    void assertRow(Map row, Object... keysAndValues) {
        for (int i = 0; i < keysAndValues.length; i += 2) {
            assertThat(row.get(keysAndValues[i].toString()), equalTo(keysAndValues[i + 1]));
        }
    }

    String formatIsoDate(String expectedYmd) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(expectedYmd);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(d);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    MockHttpServletRequest getRequest(String requestURI, String... parameterKeysAndValues) {
        MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.GET.toString(), "/rest/v1/reportingrest" + requestURI);
        for (int i=0; i<parameterKeysAndValues.length; i+=2) {
            request.setParameter(parameterKeysAndValues[i], parameterKeysAndValues[i+1]);
        }
        request.addHeader("content-type", "application/json");
        return request;
    }

    MockHttpServletResponse handle(HttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerExecutionChain handlerExecutionChain = null;
        for (RequestMappingHandlerMapping handlerMapping : handlerMappings) {
            handlerMapping.setPathMatcher(pathMatcher);
            handlerExecutionChain = handlerMapping.getHandler(request);
            if (handlerExecutionChain != null) {
                break;
            }
        }
        Assert.assertNotNull("The request URI does not exist", handlerExecutionChain);
        handlerAdapter.handle(request, response, handlerExecutionChain.getHandler());
        return response;
    }

    SimpleObject deserialize(MockHttpServletResponse response) throws Exception {
        return new ObjectMapper().readValue(response.getContentAsString(), SimpleObject.class);
    }
}
