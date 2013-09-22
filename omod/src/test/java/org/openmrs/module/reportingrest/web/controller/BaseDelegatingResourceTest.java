package org.openmrs.module.reportingrest.web.controller;

import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
/**
 * Is designed to be extended by classes testing BaseDelegatingResource.
 * <p>
 * Typically aside from implementing abstract methods of this class, you will want to override
 * {@link #validateRefRepresentation()}, {@link #validateDefaultRepresentation()} and
 * {@link #validateFullRepresentation()}.
 *
 * @param <R> resource
 * @param <T> object
 */
public abstract class BaseDelegatingResourceTest<R extends BaseDelegatingResource<T>, T> extends BaseModuleWebContextSensitiveTest {

    private T object;

    private R resource;

    private SimpleObject representation;

    @Before
    public void m() throws Exception{

        executeDataSet("reportAdditional.xml");
    }

    /**
     * Creates an instance of an object that will be used to test the resource.
     *
     * @return the new object
     */
    public abstract T newObject();

    /**
     * Needs to be implemented in order to validate the display property in each representation.
     * <p>
     * It is called by {@link #asRepresentation_shouldReturnValidDefaultRepresentation()},
     * {@link #asRepresentation_shouldReturnValidFullRepresentation()} and
     * {@link #asRepresentation_shouldReturnValidRefRepresentation()} to test precisely each
     * representation.
     *
     * @return the display property
     */
    public abstract String getDisplayProperty();

    /**
     * Needs to be implemented in order to validate the uuid property in each representation.
     * <p>
     * It is called by {@link #asRepresentation_shouldReturnValidDefaultRepresentation()},
     * {@link #asRepresentation_shouldReturnValidFullRepresentation()} and
     * {@link #asRepresentation_shouldReturnValidRefRepresentation()}.
     *
     * @return the uuid property
     */
    public abstract String getUuidProperty();

    /**
     * Validates RefRepresentation of the object returned by the resource.
     * <p>
     * Tests the value of the uuid and  the presence of a self link in the links
     * property.
     *
     * @throws Exception
     */
    public void validateRefRepresentation() throws Exception {
        assertPropEquals("uuid", getUuidProperty());
        assertPropPresent("links");
        assertPropNotPresent("resourceVersion");

        @SuppressWarnings("unchecked")
        List<Hyperlink> links = (List<Hyperlink>) getRepresentation().get("links");
        boolean self = false;
        for (Hyperlink link : links) {
            if (link.getRel().equals("self")) {
                Assert.assertNotNull(link.getUri());
                self = true;
                break;
            }
        }
        Assert.assertTrue(self);
    }

    /**
     * Validates DefaultRepresentation of the object returned by the resource.
     * <p>
     * Tests the value of the uuid property.
     *
     * @throws Exception
     */
    public void validateDefaultRepresentation() throws Exception {
        assertPropEquals("uuid", getUuidProperty());
    }

    /**
     * Validates FullRepresentation of the object returned by the resource.
     * <p>
     * Tests the value of the uuid property
     *
     * @throws Exception
     */
    public void validateFullRepresentation() throws Exception {
        assertPropEquals("uuid", getUuidProperty());
    }

    /**
     * Instantiates BaseDelegatingResource.
     *
     * @return the new resource
     */
    public R newResource() {
        ParameterizedType t = (ParameterizedType) getClass().getGenericSuperclass();
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) t.getActualTypeArguments()[1];
        return (R) Context.getService(RestService.class).getResourceBySupportedClass(clazz);
    }

    /**
     * Returns an instance of an object to test the resource.
     *
     * @return the object
     */
    public T getObject() {
        if (object == null) {
            object = newObject();
        }
        Assert.assertNotNull("newObject must not return null", object);
        return object;
    }

    /**
     * Returns a created representation.
     *
     * @return the representation
     */
    public SimpleObject getRepresentation() {
        Assert.assertNotNull("representation must not be null", representation);
        return representation;
    }

    /**
     * Returns an instantiated resource.
     *
     * @return the resource
     */
    public R getResource() {
        if (resource == null) {
            resource = newResource();
        }
        Assert.assertNotNull("newResource must not return null", resource);
        return resource;
    }

    /**
     * Creates {@link Representation#REF}.
     * <p>
     * Calls {@link BaseDelegatingResource#asRepresentation(Object, Representation)} on the resource
     * with the given object.
     *
     * @return the representation
     * @throws Exception
     */
    public SimpleObject newRefRepresentation() throws Exception {
        return (SimpleObject) getResource().asRepresentation(getObject(), Representation.REF);
    }

    /**
     * Creates {@link Representation#DEFAULT}.
     * <p>
     * Calls {@link BaseDelegatingResource#asRepresentation(Object, Representation)} on the resource
     * with the given object.
     *
     * @return the representation
     * @throws Exception
     */
    public SimpleObject newDefaultRepresentation() throws Exception {
        return (SimpleObject) getResource().asRepresentation(getObject(), Representation.DEFAULT);
    }

    /**
     * Creates {@link Representation#FULL}.
     * <p>
     * Calls {@link BaseDelegatingResource#asRepresentation(Object, Representation)} on the resource
     * with the given object.
     *
     * @return the representation
     * @throws Exception
     */
    public SimpleObject getFullRepresentation() throws Exception {
        return (SimpleObject) getResource().asRepresentation(getObject(), Representation.FULL);
    }

    /**
     * Equivalent to:
     * <p>
     * <code>
     * Assert.assertEquals(property, value, getRepresentation().get(property));
     * </code>
     * <p>
     * Performs data conversion like formatting a date for your convenience.
     *
     * @param property
     * @param value
     */
    public void assertPropEquals(String property, Object value) {
        if (value instanceof Date) {
            value = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format((Date) value);
        } else if (value instanceof Locale) {
            value = value.toString();
        }
        Assert.assertEquals(property, value, getRepresentation().get(property));
    }

    /**
     * Equivalent to:
     * <p>
     * <code>
     * Assert.assertTrue(getRepresentation().containsKey(property));
     * </code>
     *
     * @param property
     */
    public void assertPropPresent(String property) {
        Assert.assertTrue(getRepresentation().containsKey(property));
    }

    /**
     * Equivalent to:
     * <p>
     * <code>
     * Assert.assertFalse(getRepresentation().containsKey(property));
     * </code>
     */
    public void assertPropNotPresent(String property) {
        Assert.assertFalse(getRepresentation().containsKey(property));
    }

    /**
     * Tests {@link Representation#REF}
     *
     * @throws Exception
     */
    @Test
    public void asRepresentation_shouldReturnValidRefRepresentation() throws Exception {
        representation = newRefRepresentation();
        validateRefRepresentation();
    }

    /**
     * Tests {@link Representation#DEFAULT}
     *
     * @throws Exception
     */
    @Test
    public void asRepresentation_shouldReturnValidDefaultRepresentation() throws Exception {
        representation = newDefaultRepresentation();
        validateDefaultRepresentation();
    }

    /**
     * Tests {@link Representation#FULL}
     *
     * @throws Exception
     */
    @Test
    public void asRepresentation_shouldReturnValidFullRepresentation() throws Exception {
        representation = getFullRepresentation();
        validateFullRepresentation();
    }
}
