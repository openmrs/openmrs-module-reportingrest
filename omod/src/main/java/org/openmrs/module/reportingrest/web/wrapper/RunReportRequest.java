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

import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class RunReportRequest {
  private String existingRequestUuid;
  private String reportDefinitionUuid;
  private String renderModeUuid;
  private String schedule;
  private Map<String, Object> reportParameters;

  public String getExistingRequestUuid() {
    return existingRequestUuid;
  }

  public void setExistingRequestUuid(String existingRequestUuid) {
    this.existingRequestUuid = existingRequestUuid;
  }

  public String getReportDefinitionUuid() {
    return blankAsNull(reportDefinitionUuid);
  }

  public void setReportDefinitionUuid(String reportDefinitionUuid) {
    this.reportDefinitionUuid = reportDefinitionUuid;
  }

  public String getRenderModeUuid() {
    return blankAsNull(renderModeUuid);
  }

  public void setRenderModeUuid(String renderModeUuid) {
    this.renderModeUuid = renderModeUuid;
  }

  public String getSchedule() {
    return blankAsNull(schedule);
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  public Map<String, Object> getReportParameters() {
    return reportParameters;
  }

  public void setReportParameters(Map<String, Object> reportParameters) {
    this.reportParameters = reportParameters;
  }

  private String blankAsNull(String text) {
    return StringUtils.isBlank(text) ? null : text;
  }
}
