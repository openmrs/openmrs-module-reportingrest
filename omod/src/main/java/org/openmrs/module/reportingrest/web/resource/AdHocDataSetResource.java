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

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.RowPerObjectDataSetDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reportingrest.adhoc.AdHocDataSet;
import org.openmrs.module.reportingrest.adhoc.AdHocExportManager;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.openmrs.module.webservices.rest.web.resource.api.Purgeable;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/adhocdataset",
        supportedClass = AdHocDataSet.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*", "1.10.*, 1.11.*", "1.12.*", "2.0.*"})
public class AdHocDataSetResource implements Creatable, Purgeable {

    private ObjectMapper jackson = new ObjectMapper();

    // since Resources are not Spring beans, we cannot autowire these properties
    private AdHocExportManager manager;
    private AllDefinitionLibraries libraries;

    private AdHocExportManager getManager() {
        if (manager == null) {
            manager = Context.getRegisteredComponents(AdHocExportManager.class).get(0);
        }
        return manager;
    }

    private AllDefinitionLibraries getLibraries() {
        if (libraries == null) {
            libraries = Context.getRegisteredComponents(AllDefinitionLibraries.class).get(0);
        }
        return libraries;
    }

    @Override
    public Object create(SimpleObject post, RequestContext requestContext) throws ResponseException {
        AdHocDataSet adHocDataSet = jackson.convertValue(post, AdHocDataSet.class);

        try {
            RowPerObjectDataSetDefinition dsd = adHocDataSet.toDataSetDefinition(getManager(), getLibraries());
            getManager().saveAdHocDataSet(dsd);
            return new AdHocDataSet(dsd);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getUri(Object delegate) {
        if (delegate == null)
            return "";

        Resource res = getClass().getAnnotation(Resource.class);
        return RestConstants.URI_PREFIX + res.name() + "/" + getUniqueId((AdHocDataSet) delegate);
    }

    private String getUniqueId(AdHocDataSet adHocDataSet) {
        return adHocDataSet.getUuid();
    }

    @Override
    public void purge(String uuid, RequestContext context) throws ResponseException {
        RowPerObjectDataSetDefinition dsd = getManager().getAdHocDataSetByUuid(uuid);
        getManager().purgeAdHocDataSet(dsd);
    }

}
