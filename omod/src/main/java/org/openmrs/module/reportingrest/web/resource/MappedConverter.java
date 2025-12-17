package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Handler(supports = Mapped.class, order = 0)
public class MappedConverter implements Converter<Mapped> {

	public DelegatingResourceDescription getResourceDescription(Mapped mapped, Representation representation) {
		if (representation instanceof CustomRepresentation) {
			return ConversionUtil.getCustomRepresentationDescription((CustomRepresentation) representation);
		}
		DelegatingResourceDescription ret = new DelegatingResourceDescription();
		ret.addProperty("parameterMappings", representation);
		ret.addProperty("parameterizable", representation);
		return ret;
	}

	@Override
	public SimpleObject asRepresentation(Mapped o, Representation rep) throws ConversionException {
		SimpleObject ret = new SimpleObject();
		Map<String, DelegatingResourceDescription.Property> props = getResourceDescription(o, rep).getProperties();
		for (String propName : props.keySet()) {
			Object value = getProperty(o,  propName);
			ret.put(propName, ConversionUtil.convertToRepresentation(value, props.get(propName).getRep()));
		}
		return ret;
	}

	@Override
	public Object getProperty(Mapped mapped, String propertyName) throws ConversionException {
		if (propertyName.equalsIgnoreCase("parameterMappings")) {
			List<SimpleObject> ret = new ArrayList<>();
			for (Object parameterName : mapped.getParameterMappings().keySet()) {
				SimpleObject m = new SimpleObject();
				m.put("key", parameterName);
				m.put("value", mapped.getParameterMappings().get(parameterName));
				ret.add(m);
			}
			return ret;
		}
		else if (propertyName.equalsIgnoreCase("parameterizable")) {
			return mapped.getParameterizable();
		}
		else {
			throw new ConversionException("Unknown property: " + propertyName);
		}
	}

	@Override
	public Mapped getByUniqueId(String string) {
		return null;
	}

	/**
     * @see Converter#newInstance(String)
     */
    @Override
    public Mapped newInstance(String arg) {
	    return null;
    }

	/**
	 * @see Converter#setProperty(Object, String, Object)
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		// not used
	}
}
