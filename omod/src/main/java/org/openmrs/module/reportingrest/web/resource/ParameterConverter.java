package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

@Handler(supports = Parameter.class, order = 0)
public class ParameterConverter implements Converter<Parameter> {

	@Override
	public Parameter getByUniqueId(String string) {
		// not used
		return null;
	}

	@Override
	public SimpleObject asRepresentation(Parameter param, Representation rep) throws ConversionException {
		// convert into a map
		SimpleObject paramMap = new SimpleObject();
		paramMap.put("name", param.getName());
		paramMap.put("label", param.getLabel());
		paramMap.put("type", param.getType().getName());
        paramMap.put("required",  param.isRequired());
        paramMap.put("display", Context.getMessageSourceService().getMessage(param.getLabelOrName()));

        return paramMap;
	}

	@Override
	public Object getProperty(Parameter param, String propertyName) throws ConversionException {
		try {
			return PropertyUtils.getProperty(param, propertyName);
		}
		catch (Exception e) {
			throw new ConversionException(e);
		}
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		// not used
	}

	/**
     * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
     */
    @Override
    public Parameter newInstance(String arg0) {
	    // not used
	    return null;
    }

}
