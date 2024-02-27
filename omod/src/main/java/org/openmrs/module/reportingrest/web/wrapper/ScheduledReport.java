/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportingrest.web.wrapper;

import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

import java.io.Serializable;

public class ScheduledReport implements Serializable {
  private static final long serialVersionUID = 6711225056254411235L;

  private ReportDefinition reportDefinition;
  private ReportRequest reportScheduleRequest;

  public ScheduledReport() {
  }

  public ScheduledReport(ReportDefinition reportDefinition, ReportRequest reportScheduleRequest) {
    this.reportDefinition = reportDefinition;
    this.reportScheduleRequest = reportScheduleRequest;
  }

  public ReportDefinition getReportDefinition() {
    return reportDefinition;
  }

  public void setReportDefinition(ReportDefinition reportDefinition) {
    this.reportDefinition = reportDefinition;
  }

  public ReportRequest getReportScheduleRequest() {
    return reportScheduleRequest;
  }

  public void setReportScheduleRequest(ReportRequest reportScheduleRequest) {
    this.reportScheduleRequest = reportScheduleRequest;
  }
}
