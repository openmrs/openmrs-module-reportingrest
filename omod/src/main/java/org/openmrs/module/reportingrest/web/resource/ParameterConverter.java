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
	SimpleObject paramMapppp;
	@Override
	public SimpleObject asRepresentation(Parameter param, Representation rep)
			throws ConversionException {
		// convert into a map
		SimpleObject paramMap = new SimpleObject();
		//Object paramMapppp;//=new Integer(4);
		//paramMapppp.put("name", param.getName());
		paramMap.put("name", param.getName());
		paramMap.put("label", param.getLabel());
		paramMap.put("type", param.getType().getName());
		
		return paramMap;
	}

	@Override
	public Object getProperty(Parameter param, String propertyName)
			throws ConversionException {
		/*try {
			return PropertyUtils.getProperty(param, propertyName);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}*/
		
		// fail
		return null;
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		// not used
	}

    @Override
    public Parameter newInstance(String arg0) {
	    // not used
	    return null;
    }

}
