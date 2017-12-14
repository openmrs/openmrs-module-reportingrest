/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * {@link Converter} for {@link RenderingMode}s
 */
@Handler(supports = Mapped.class, order = 1)
public class MappedReportDefinitionConverter implements Converter<Mapped<ReportDefinition>> {

    @Override
    public SimpleObject asRepresentation(Mapped<ReportDefinition> instance, Representation rep) throws ConversionException {
        System.out.println("In MappedReportDefinitionConverter asRepresentation()");
        return null;
    }

    @Override
    public Object getProperty(Mapped mapped, String propertyName) throws ConversionException {
        try {
            return PropertyUtils.getProperty(mapped, propertyName);
        } catch (Exception e) {
            throw new ConversionException("Unable to retrieve property " + propertyName + " from " + mapped, e);
        }
    }

    /**
     * @see Converter#setProperty(Object, String, Object)
     */
    @Override
    public void setProperty(Object mapped, String propertyName, Object value) throws ConversionException {
        try {
            if ("parameterizable".equals(propertyName)) {
                value = ConversionUtil.convert(value, ReportDefinition.class);
            }
            PropertyUtils.setProperty(mapped, propertyName, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see Converter#newInstance(String)
     */
    @Override
    public Mapped newInstance(String arg0) {
        return new Mapped<ReportDefinition>();
    }

    @Override
    public Mapped getByUniqueId(String string) {
        return null;
    }

}
