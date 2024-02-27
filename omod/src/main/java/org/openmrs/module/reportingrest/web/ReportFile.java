/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportingrest.web;

import java.io.Serializable;

/**
 * A single report file.
 */
public class ReportFile implements Serializable {
  private static final long serialVersionUID = 1L;

  private String filename;
  private String contentType;
  private byte[] fileContent;

  public ReportFile(String filename, String contentType, byte[] fileContent) {
    this.filename = filename;
    this.contentType = contentType;
    this.fileContent = fileContent;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * @return the fileContent
   */
  public byte[] getFileContent() {
    return fileContent;
  }

  /**
   * @param fileContent the fileContent to set
   */
  public void setFileContent(byte[] fileContent) {
    this.fileContent = fileContent;
  }

  /**
   * @return the contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
