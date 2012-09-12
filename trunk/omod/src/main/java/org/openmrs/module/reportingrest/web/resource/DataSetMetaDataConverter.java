package org.openmrs.module.reportingrest.web.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetMetaData;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

@Handler(supports = DataSetMetaData.class, order = 0)
public class DataSetMetaDataConverter implements Converter<DataSetMetaData> {

	@Override
	public DataSetMetaData getByUniqueId(String string) {
		// not used
		return null;
	}

	@Override
	public SimpleObject asRepresentation(DataSetMetaData metadata, Representation rep)
			throws ConversionException {
		// ouput should be:
		// { columns: [
		//			{ name: "internalName", label: "Pretty Name", datatype: "java.lang.String" },
		//			...
		// 		] }
		List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
		for (DataSetColumn column : metadata.getColumns()) {
			Map<String, String> columnMap = new HashMap<String, String>();
			columnMap.put("name", column.getName());
			columnMap.put("label", column.getLabel());
			columnMap.put("datatype", column.getDataType().getName());
			columns.add(columnMap);
		}

		return new SimpleObject().add("columns", columns);
	}

	@Override
	public Object getProperty(DataSetMetaData metadata, String propertyName)
			throws ConversionException {
		try {
			return PropertyUtils.getProperty(metadata, propertyName);
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

	/**
     * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
     */
    @Override
    public DataSetMetaData newInstance(String arg0) {
	    // not used
    	return null;
    }

	/**
     * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
    	// not used
    }

}
