/**
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
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

/**
 * {@link Converter} for {@link RenderingMode}s
 */
@Handler(supports = RenderingMode.class, order = 1)
public class RenderingModeConverter implements Converter<RenderingMode> {

	/**
	 * @see Converter#asRepresentation(Object, Representation)
	 */
	@Override
	public SimpleObject asRepresentation(RenderingMode mode, Representation rep) throws ConversionException {
		SimpleObject so = new SimpleObject();
		so.add("rendererType", mode.getRenderer().getClass().getName());
		so.add("argument", mode.getArgument());
		so.add("label", mode.getLabel());
		so.add("sortWeight", mode.getSortWeight());
		return so;
	}

	/**
	 * @see Converter#getByUniqueId(String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public RenderingMode getByUniqueId(String id) {
		RenderingMode mode = new RenderingMode();
		String[] typeArgSplit = id.split("!");
		try {
			Class<? extends ReportRenderer> c = (Class<? extends ReportRenderer>)Context.loadClass(typeArgSplit[0]);
			mode.setRenderer(c.newInstance());
			if (typeArgSplit.length == 2) {
				mode.setArgument(typeArgSplit[1]);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to get a report renderer by id: " + id);
		}
		return mode;
	}

	/**
	 * @see Converter#getProperty(Object, String)
	 */
	@Override
	public Object getProperty(RenderingMode renderer, String propertyName) throws ConversionException {
		try {
			return PropertyUtils.getProperty(renderer, propertyName);
		}
		catch (Exception e) {
			throw new ConversionException("Unable to retrieve property " + propertyName + " from " + renderer, e);
		}
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(Object renderer, String propertyName, Object value) throws ConversionException {
		//not used
	}

	/**
     * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
     */
    @Override
    public RenderingMode newInstance(String arg0) {
	    // not used
	    return null;
    }
    
}
