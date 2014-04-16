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

package org.openmrs.module.reportingrest.adhoc.task;

import org.openmrs.module.reporting.report.task.AbstractReportsTask;
import org.openmrs.module.reportingrest.adhoc.AdHocExportManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see org.openmrs.module.reportingrest.adhoc.AdHocExportManager#deleteTransientReportDefinitions()
 */
public class DeleteOldOldAdHocReportDefinitionsTask extends AbstractReportsTask  {

    @Autowired
    AdHocExportManager adHocExportManager;

    /**
     * @see org.openmrs.module.reportingrest.adhoc.AdHocExportManager#deleteTransientReportDefinitions()
     */
    @Override
    public synchronized void execute() {
        adHocExportManager.deleteTransientReportDefinitions();
    }

}
