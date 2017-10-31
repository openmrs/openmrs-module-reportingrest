package org.openmrs.module.reportingrest.web.resource;

import org.apache.struts.mock.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EvaluatedResourceTest extends BaseModuleWebContextSensitiveTest {

    EvaluatedResource resource;

    @Before
    public void setUp() throws Exception {
        resource = new EvaluatedResource<Evaluated<?>>() {
            @Override
            public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
                return null;
            }
        };
    }

    @Test
    public void testGetEvaluationContextWithParameters_noParams() throws Exception {
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition, buildRequestContext(), null, null);
        assertThat(evalContext.getParameterValues().size(), is(0));
    }

    @Test
    public void testGetEvaluationContextWithParameters_paramFromRequest() throws Exception {
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("timeQualifier", "Time Qualifier", TimeQualifier.class));
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition, buildRequestContext("timeQualifier", "FIRST"), null, null);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertThat((TimeQualifier) evalContext.getParameterValue("timeQualifier"), is(TimeQualifier.FIRST));
    }

    @Test
    public void testGetEvaluationContextWithParameters_paramFromBody() throws Exception {
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("timeQualifier", "Time Qualifier", TimeQualifier.class));
        SimpleObject postBody = new SimpleObject().add("timeQualifier", "FIRST");
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition, buildRequestContext(), null, postBody);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertThat((TimeQualifier) evalContext.getParameterValue("timeQualifier"), is(TimeQualifier.FIRST));
    }

    @Test
    public void testGetEvaluationContextWithParameters_collectionParamFromRequest() throws Exception {
        String etUuid = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition,
                buildRequestContext("encounterTypeList", etUuid), null, null);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertThat((List<EncounterType>) evalContext.getParameterValue("encounterTypeList"),
                containsInAnyOrder(Context.getEncounterService().getEncounterTypeByUuid(etUuid)));
    }

    @Test
    public void testGetEvaluationContextWithParameters_emptyCollectionParamFromRequest() throws Exception {
        String etUuid = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition,
                buildRequestContext("encounterTypeList", null), null, null);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertThat(((List<EncounterType>) evalContext.getParameterValue("encounterTypeList")).size(),
                is(0));
    }

    @Test
    public void testGetEvaluationContextWithParameters_collectionParamFromBody() throws Exception {
        String etUuid = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
        SimpleObject postBody = new SimpleObject().add("encounterTypeList", Arrays.asList(etUuid));
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition, buildRequestContext(), null, postBody);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertThat((List<EncounterType>) evalContext.getParameterValue("encounterTypeList"),
                containsInAnyOrder(Context.getEncounterService().getEncounterTypeByUuid(etUuid)));
    }

    @Test
    public void testGetEvaluationContextWithParameters_emptyCollectionParamFromBody() throws Exception {
        String etUuid = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
        SimpleObject postBody = new SimpleObject().add("encounterTypeList", null);
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition, buildRequestContext(), null, postBody);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertThat(((List<EncounterType>) evalContext.getParameterValue("encounterTypeList")).size(),
                is(0));
    }


    @Test
    public void testGetEvaluationContextWithParameters_missingCollectionParam() throws Exception {
        String etUuid = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
        EncounterCohortDefinition definition = new EncounterCohortDefinition();
        definition.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null, null, false));;
        EvaluationContext evalContext = resource.getEvaluationContextWithParameters(definition,
                buildRequestContext(null), null, null);
        assertThat(evalContext.getParameterValues().size(), is(1));
        assertNull((List<EncounterType>) evalContext.getParameterValue("encounterTypeList"));
    }

    private RequestContext buildRequestContext(String... paramNamesAndValues) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (paramNamesAndValues != null) {
            for (int i = 0; i < paramNamesAndValues.length; i += 2) {
                request.addParameter(paramNamesAndValues[i], paramNamesAndValues[i + 1]);
            }
        }
        RequestContext requestContext = mock(RequestContext.class);
        when(requestContext.getRequest()).thenReturn(request);
        return requestContext;
    }
}