package org.openmrs.module.reportingrest.web.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetMetaData;
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
	public Object asRepresentation(DataSetMetaData metadata, Representation rep)
			throws ConversionException {
		// convert into a map
		// [ { name: "name", label: "Pretty Name", datatype: "java.lang.String"}, { }, ... ]
		List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
		for (DataSetColumn column : metadata.getColumns()) {
			Map<String, String> columnMap = new HashMap<String, String>();
			columnMap.put("name", column.getName());
			columnMap.put("label", column.getLabel());
			columnMap.put("datatype", column.getDataType().getName());
			columns.add(columnMap);
		}
		
		return columns;
	}

	@Override
	public Object getProperty(DataSetMetaData metadata, String propertyName)
			throws ConversionException {
		try {
			return PropertyUtils.getProperty(metadata, propertyName);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// fail
		return null;
	}

	@Override
	public void setProperty(DataSetMetaData instance, String propertyName,
			Object value) throws ConversionException {
		// not used
	}
	
	

}
