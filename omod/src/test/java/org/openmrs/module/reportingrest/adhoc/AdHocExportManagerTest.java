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

package org.openmrs.module.reportingrest.adhoc;

import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AdHocExportManagerTest extends BaseModuleWebContextSensitiveTest {

    public static final String DSD_UUID = "for-testing-purposes";

    @Autowired
    AdHocExportManager adHocExportManager;

    @Autowired
    ReportService reportService;

    @Autowired
    ReportDefinitionService reportDefinitionService;

    @Test
    public void testDeleteTransientReportDefinitions() throws Exception {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setUuid(DSD_UUID);
        dsd.setName("Testing");
        adHocExportManager.saveAdHocDataSet(dsd);

        ReportRequest reportRequest = adHocExportManager.buildExportRequest(Arrays.asList(DSD_UUID), new HashMap<String, Object>(), null);
        reportService.runReport(reportRequest);

        assertThat(reportService.getReportRequests(null, null, null).size(), is(1));
        assertThat(reportDefinitionService.getAllDefinitions(true).size(), is(1));

        // this should have no effect, since there's a ReportRequest referencing the ReportDefinition
        adHocExportManager.deleteTransientReportDefinitions();

        assertThat(reportService.getReportRequests(null, null, null).size(), is(1));
        assertThat(reportDefinitionService.getAllDefinitions(true).size(), is(1));

        // delete the request (to simulate this being done by a scheduled task)
        for (ReportRequest request : reportService.getReportRequests(null, null, null)) {
            reportService.purgeReportRequest(request);
        }

        // sanity check
        assertThat(reportService.getReportRequests(null, null, null).size(), is(0));

        // now calling this should delete our no-longer-referenced ReportDefinition
        adHocExportManager.deleteTransientReportDefinitions();
        assertThat(reportDefinitionService.getAllDefinitions(true).size(), is(0));
    }

}
