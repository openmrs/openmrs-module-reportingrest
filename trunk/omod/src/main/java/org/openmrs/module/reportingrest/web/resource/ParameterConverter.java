package org.openmrs.module.reportingrest.web.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
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
	public Object asRepresentation(Parameter param, Representation rep)
			throws ConversionException {
		// convert into a map
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("name", param.getName());
		paramMap.put("label", param.getLabel());
		paramMap.put("type", param.getType().getName());
		
		return paramMap;
	}

	@Override
	public Object getProperty(Parameter param, String propertyName)
			throws ConversionException {
		try {
			return PropertyUtils.getProperty(param, propertyName);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		// fail
		return null;
	}

	@Override
	public void setProperty(Parameter instance, String propertyName,
			Object value) throws ConversionException {
		// not used
	}
	
	

}
