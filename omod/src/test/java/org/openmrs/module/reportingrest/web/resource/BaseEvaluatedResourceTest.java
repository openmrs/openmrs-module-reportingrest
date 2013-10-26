/*
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

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 *
 */
public abstract class BaseEvaluatedResourceTest<R extends EvaluatedResource<T>, T extends Evaluated> extends BaseModuleWebContextSensitiveTest {

    private R resource;

    /**
     * Instantiates EvaluatedResource.
     *
     * @return the new resource
     */
    public R newResource() {
        ParameterizedType t = (ParameterizedType) getClass().getGenericSuperclass();
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) t.getActualTypeArguments()[1];
        return (R) Context.getService(RestService.class).getResourceBySupportedClass(clazz);
    }

    /**
     * Returns an instantiated resource.
     *
     * @return the resource
     */
    public R getResource() {
        if (resource == null) {
            resource = newResource();
        }
        Assert.assertNotNull("newResource must not return null", resource);
        return resource;
    }

    protected RequestContext buildRequestContext(String... paramNamesAndValues) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        for (int i = 0; i < paramNamesAndValues.length; i += 2) {
            request.addParameter(paramNamesAndValues[i], paramNamesAndValues[i + 1]);
        }
        RequestContext context = new RequestContext();
        context.setRequest(request);
        return context;
    }

    protected Object path(Object object, Object... path) throws Exception {
        for (int i = 0; i < path.length; ++i) {
            if (path[i] instanceof String) {
                object = PropertyUtils.getProperty(object, (String) path[i]);
            } else if (path[i] instanceof Integer) {
                object = ((List) object).get((Integer) path[i]);
            }
        }
        return object;
    }

    protected String toJson(Object object) throws IOException {
        return new ObjectMapper().writeValueAsString(object);
    }

    protected boolean hasLink(Object obj, String rel, String uriEndsWith) throws Exception {
	    List<Hyperlink> links = (List<Hyperlink>) path(obj, "links");
	    for (Hyperlink link : links) {
	    	if (link.getRel().equals(rel) && link.getUri().endsWith(uriEndsWith))
	    		return true;
	    }
	    return false;
    }
}
