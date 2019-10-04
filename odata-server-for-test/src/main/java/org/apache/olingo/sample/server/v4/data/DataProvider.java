/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.sample.server.v4.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.server.v4.edmprovider.CarsEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.hawkore.mule.extensions.odata.test.utils.U;

/**
 * The type Data provider.
 */
public class DataProvider {

    private Map<String, EntityCollection> data;
    private final ServiceMetadata edm;
    private final OData odata;

    /**
     * Instantiates a new Data provider.
     *
     * @param edm
     *     the edm
     * @param odata
     *     the odata
     */
    public DataProvider(ServiceMetadata edm, OData odata) {
        this.edm = edm;
        this.odata = odata;
        generateData();
    }

    /**
     * Generate data.
     */
    public void generateData() {
        data = new HashMap<String, EntityCollection>();
        data.put("Cars", createCars());
        data.put("Manufacturers", createManufacturers());
        data.put("Drivers", createDrivers());
        createAllLinks();
    }

    /**
     * Read all entity collection.
     *
     * @param edmEntitySet
     *     the edm entity set
     * @return the entity collection
     */
    public EntityCollection readAll(EdmEntitySet edmEntitySet) {
        synchronized (data) {
            return data.get(edmEntitySet.getName());
        }
    }

    /**
     * Read entity.
     *
     * @param edmEntitySet
     *     the edm entity set
     * @param keys
     *     the keys
     * @return the entity
     * @throws ODataApplicationException
     *     the o data application exception
     */
    public Entity read(final EdmEntitySet edmEntitySet, final List<UriParameter> keys)
        throws ODataApplicationException {
        synchronized (data) {
            final EdmEntityType entityType = edmEntitySet.getEntityType();
            final EntityCollection entitySet = data.get(edmEntitySet.getName());
            if (entitySet == null) {
                return null;
            } else {
                try {
                    for (final Entity entity : entitySet.getEntities()) {
                        boolean found = false;
                        for (final UriParameter key : keys) {
                            final EdmProperty property = (EdmProperty)entityType.getProperty(key.getName());
                            final EdmPrimitiveType type = (EdmPrimitiveType)property.getType();
                            if (type.valueToString(entity.getProperty(key.getName()).getValue(), property.isNullable(),
                                property.getMaxLength(), property.getPrecision(), property.getScale(),
                                property.isUnicode()).equals(type.fromUriLiteral(key.getText()))) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            return entity;
                        }
                    }
                    return null;
                } catch (final EdmPrimitiveTypeException e) {
                    throw new ODataApplicationException("Wrong key!", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                        Locale.ENGLISH);
                }
            }
        }
    }

    /**
     * Create entity data entity.
     *
     * @param edmEntitySet
     *     the edm entity set
     * @param entityToCreate
     *     the entity to create
     * @param rawServiceUri
     *     the raw service uri
     * @return the entity
     * @throws ODataApplicationException
     *     the o data application exception
     */
    public Entity createEntityData(EdmEntitySet edmEntitySet, Entity entityToCreate, String rawServiceUri)
        throws ODataApplicationException {
        synchronized (data) {
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();
            final EntityCollection entitySet = data.get(edmEntitySet.getName());
            if (entitySet == null) {
                return null;
            }
            return createEntity(edmEntitySet, edmEntityType, entityToCreate, entitySet.getEntities(), rawServiceUri);
        }
    }

    /**
     * This method is invoked for PATCH or PUT requests
     *
     * @param edmEntitySet
     *     the edm entity set
     * @param keyParams
     *     the key params
     * @param updateEntity
     *     the update entity
     * @param httpMethod
     *     the http method
     * @throws ODataApplicationException
     *     the o data application exception
     */
    public void updateEntityData(EdmEntitySet edmEntitySet,
        List<UriParameter> keyParams,
        Entity updateEntity,
        HttpMethod httpMethod) throws ODataApplicationException {

        synchronized (data) {
            EdmEntityType entityType = edmEntitySet.getEntityType();
            final EntityCollection entitySet = data.get(edmEntitySet.getName());
            if (entitySet == null) {
                return;
            } else {
                try {
                    Entity entityToUpdate = null;
                    for (final Entity entity : entitySet.getEntities()) {
                        for (final UriParameter key : keyParams) {
                            final EdmProperty property = (EdmProperty)entityType.getProperty(key.getName());
                            final EdmPrimitiveType type = (EdmPrimitiveType)property.getType();
                            if (type.valueToString(entity.getProperty(key.getName()).getValue(), property.isNullable(),
                                property.getMaxLength(), property.getPrecision(), property.getScale(),
                                property.isUnicode()).equals(type.fromUriLiteral(key.getText()))) {
                                entityToUpdate = entity;
                                break;
                            }
                        }
                        if (entityToUpdate != null) {
                            break;
                        }
                    }
                    if (entityToUpdate != null) {
                        // loop over all properties and replace the values with the values of the given payload
                        // Note: ignoring ComplexType, as we don't have it in our odata model
                        List<Property> existingProperties = entityToUpdate.getProperties();
                        for (Property existingProp : existingProperties) {
                            String propName = existingProp.getName();

                            // ignore the key properties, they aren't updateable
                            if (isKey(entityType, propName)) {
                                continue;
                            }

                            Property updateProperty = updateEntity.getProperty(propName);
                            // the request payload might not consider ALL properties, so it can be null
                            if (updateProperty == null) {
                                // if a property has NOT been added to the request payload
                                // depending on the HttpMethod, our behavior is different
                                if (httpMethod.equals(HttpMethod.PATCH)) {
                                    // as of the OData spec, in case of PATCH, the existing property is not touched
                                    continue; // do nothing
                                } else if (httpMethod.equals(HttpMethod.PUT)) {
                                    // as of the OData spec, in case of PUT, the existing property is set to null (or
                                    // to default value)
                                    existingProp.setValue(existingProp.getValueType(), null);
                                    continue;
                                }
                            }

                            // change the value of the properties
                            existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
                        }
                    } else {
                        throw new ODataApplicationException("Entity not found",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
                    }
                } catch (final EdmPrimitiveTypeException e) {
                    throw new ODataApplicationException("Wrong key!", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                        Locale.ENGLISH);
                }
            }
        }
    }

    /**
     * Delete entity data.
     *
     * @param edmEntitySet
     *     the edm entity set
     * @param keyParams
     *     the key params
     * @throws ODataApplicationException
     *     the o data application exception
     */
    public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams)
        throws ODataApplicationException {
        synchronized (data) {
            EdmEntityType entityType = edmEntitySet.getEntityType();
            final EntityCollection entitySet = data.get(edmEntitySet.getName());
            if (entitySet == null) {
                return;
            } else {
                try {
                    int entityToDeleteIdx = -1;
                    List<Entity> entities = entitySet.getEntities();
                    for (int i = 0; i < entities.size(); i++) {
                        final Entity entity = entities.get(i);
                        for (final UriParameter key : keyParams) {
                            final EdmProperty property = (EdmProperty)entityType.getProperty(key.getName());
                            final EdmPrimitiveType type = (EdmPrimitiveType)property.getType();
                            if (type.valueToString(entity.getProperty(key.getName()).getValue(), property.isNullable(),
                                property.getMaxLength(), property.getPrecision(), property.getScale(),
                                property.isUnicode()).equals(type.fromUriLiteral(key.getText()))) {
                                entityToDeleteIdx = i;
                                // found
                                break;
                            }
                        }
                        if (entityToDeleteIdx != -1) {
                            // found
                            break;
                        }
                    }
                    if (entityToDeleteIdx != -1) {
                        entities.remove(entityToDeleteIdx);
                    } else {
                        throw new ODataApplicationException("Entity not found",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
                    }
                } catch (final EdmPrimitiveTypeException e) {
                    throw new ODataApplicationException("Wrong key!", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                        Locale.ENGLISH);
                }
            }
        }
    }

    /**
     * The type Data provider exception.
     */
    public static class DataProviderException extends ODataException {

        private static final long serialVersionUID = 5098059649321796156L;

        /**
         * Instantiates a new Data provider exception.
         *
         * @param message
         *     the message
         * @param throwable
         *     the throwable
         */
        public DataProviderException(String message, Throwable throwable) {
            super(message, throwable);
        }

        /**
         * Instantiates a new Data provider exception.
         *
         * @param message
         *     the message
         */
        public DataProviderException(String message) {
            super(message);
        }

    }

    private EntityCollection createCars() {
        EntityCollection entitySet = new EntityCollection();
        Entity el = new Entity().addProperty(createPrimitive("Id", "1")).addProperty(createPrimitive("Model", "F1 W03"))
                        .addProperty(createPrimitive("ModelYear", 2012))
                        .addProperty(createPrimitive("Price", 189189.43))
                        .addProperty(createPrimitive("Currency", "EUR"))
                        .addProperty(createPrimitive("Updated", 1392989833964l));
        el.setId(createId(CarsEdmProvider.ES_CARS_NAME, "1"));
        entitySet.getEntities().add(el);

        el = new Entity().addProperty(createPrimitive("Id", "2")).addProperty(createPrimitive("Model", "F1 W04"))
                 .addProperty(createPrimitive("ModelYear", 2013)).addProperty(createPrimitive("Price", 199999.99))
                 .addProperty(createPrimitive("Currency", "EUR"))
                 .addProperty(createPrimitive("Updated", 1392990355793l));
        el.setId(createId(CarsEdmProvider.ES_CARS_NAME, "2"));
        entitySet.getEntities().add(el);

        el = new Entity().addProperty(createPrimitive("Id", "3")).addProperty(createPrimitive("Model", "F2012"))
                 .addProperty(createPrimitive("ModelYear", 2012)).addProperty(createPrimitive("Price", 137285.33))
                 .addProperty(createPrimitive("Currency", "EUR"))
                 .addProperty(createPrimitive("Updated", 1392990355793l));
        el.setId(createId(CarsEdmProvider.ES_CARS_NAME, "3"));
        entitySet.getEntities().add(el);

        el = new Entity().addProperty(createPrimitive("Id", "4")).addProperty(createPrimitive("Model", "F2013"))
                 .addProperty(createPrimitive("ModelYear", 2013)).addProperty(createPrimitive("Price", 145285.00))
                 .addProperty(createPrimitive("Currency", "EUR"))
                 .addProperty(createPrimitive("Updated", 1392973616419l));
        el.setId(createId(CarsEdmProvider.ES_CARS_NAME, "4"));
        entitySet.getEntities().add(el);

        for (Entity entity : entitySet.getEntities()) {
            entity.setType(CarsEdmProvider.ET_CAR.getFullQualifiedNameAsString());
        }
        return entitySet;
    }

    private EntityCollection createManufacturers() {
        EntityCollection entitySet = new EntityCollection();

        Entity el = new Entity().addProperty(createPrimitive("Id", "1"))
                        .addProperty(createPrimitive("Name", "Star Powered Racing")).addProperty(
                createPrimitive("Founded",
                    U.toCalendar(Instant.ofEpochMilli(-489024000000l).atOffset(ZoneOffset.ofHours(1)))))
                        .addProperty(createAddress("Star Street 137", "Stuttgart", "70173", "Germany"));
        el.setId(createId(CarsEdmProvider.ES_MANUFACTURER_NAME, "1"));
        entitySet.getEntities().add(el);

        el = new Entity().addProperty(createPrimitive("Id", "2"))
                 .addProperty(createPrimitive("Name", "Horse Powered Racing")).addProperty(createPrimitive("Founded",
                U.toCalendar(Instant.ofEpochMilli(-1266278400000l).atOffset(ZoneOffset.ofHours(1)))))
                 .addProperty(createAddress("Horse Street 1", "Maranello", "41053", "Italy"));
        el.setId(createId(CarsEdmProvider.ES_MANUFACTURER_NAME, "2"));
        entitySet.getEntities().add(el);

        for (Entity entity : entitySet.getEntities()) {
            entity.setType(CarsEdmProvider.ET_MANUFACTURER.getFullQualifiedNameAsString());
        }
        return entitySet;
    }

    private EntityCollection createDrivers() {
        EntityCollection entitySet = new EntityCollection();
        Entity el = new Entity().addProperty(createPrimitive("Id", 1)).addProperty(createPrimitive("Name", "Mic"))
                        .addProperty(createPrimitive("Lastname", "Shoemaker"))
                        .addProperty(createPrimitive("Nickname", "The Fast"))
                        .addProperty(createPrimitive("Birthday", 488671200000l));
        el.setId(createId(CarsEdmProvider.ES_DRIVERS_NAME, 1));
        entitySet.getEntities().add(el);

        el = new Entity().addProperty(createPrimitive("Id", 2)).addProperty(createPrimitive("Name", "Nico"))
                 .addProperty(createPrimitive("Lastname", "Mulemountain"))
                 .addProperty(createPrimitive("Nickname", null))
                 .addProperty(createPrimitive("Birthday", -31366800000l));
        el.setId(createId(CarsEdmProvider.ES_DRIVERS_NAME, 2));
        entitySet.getEntities().add(el);

        el = new Entity().addProperty(createPrimitive("Id", 3)).addProperty(createPrimitive("Name", "Kimi"))
                 .addProperty(createPrimitive("Lastname", "Heikkinen"))
                 .addProperty(createPrimitive("Nickname", "Iceman"))
                 .addProperty(createPrimitive("Birthday", 308962800000l));
        el.setId(createId(CarsEdmProvider.ES_DRIVERS_NAME, 3));
        entitySet.getEntities().add(el);
        for (Entity entity : entitySet.getEntities()) {
            entity.setType(CarsEdmProvider.ET_DRIVER.getFullQualifiedNameAsString());
        }
        return entitySet;
    }

    private void createAllLinks() {

        setLink(data.get(CarsEdmProvider.ES_DRIVERS_NAME).getEntities().get(0), CarsEdmProvider.ET_CAR.getName(),
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(0));
        setLink(data.get(CarsEdmProvider.ES_DRIVERS_NAME).getEntities().get(1), CarsEdmProvider.ET_CAR.getName(),
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(1));
        setLink(data.get(CarsEdmProvider.ES_DRIVERS_NAME).getEntities().get(2), CarsEdmProvider.ET_CAR.getName(),
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(2));

        setLink(data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(0), CarsEdmProvider.ET_DRIVER.getName(),
            data.get(CarsEdmProvider.ES_DRIVERS_NAME).getEntities().get(0));
        setLink(data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(1), CarsEdmProvider.ET_DRIVER.getName(),
            data.get(CarsEdmProvider.ES_DRIVERS_NAME).getEntities().get(1));
        setLink(data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(2), CarsEdmProvider.ET_DRIVER.getName(),
            data.get(CarsEdmProvider.ES_DRIVERS_NAME).getEntities().get(2));

        setLinks(data.get(CarsEdmProvider.ES_MANUFACTURER_NAME).getEntities().get(0), CarsEdmProvider.ES_CARS_NAME,
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(0),
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(1));
        setLinks(data.get(CarsEdmProvider.ES_MANUFACTURER_NAME).getEntities().get(1), CarsEdmProvider.ES_CARS_NAME,
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(2),
            data.get(CarsEdmProvider.ES_CARS_NAME).getEntities().get(3));
    }

    private Property createAddress(final String street, final String city, final String zipCode, final String country) {
        ComplexValue complexValue = new ComplexValue();
        List<Property> addressProperties = complexValue.getValue();
        addressProperties.add(createPrimitive("Street", street));
        addressProperties.add(createPrimitive("City", city));
        addressProperties.add(createPrimitive("ZipCode", zipCode));
        addressProperties.add(createPrimitive("Country", country));
        return new Property(null, "Address", ValueType.COMPLEX, complexValue);
    }

    private Property createPrimitive(final String name, final Object value) {
        return new Property(null, name, ValueType.PRIMITIVE, value);
    }

    private URI createId(String entitySetName, Object id) {
        try {

            EdmEntitySet eset = edm.getEdm().getEntityContainer().getEntitySet(entitySetName);

            EdmKeyPropertyRef propertyRef = eset.getEntityType().getKeyPropertyRefs().get(0);

            EdmProperty p = propertyRef.getProperty();
            EdmType type = p.getType();
            if (type instanceof EdmPrimitiveType) {
                EdmPrimitiveType stype = (EdmPrimitiveType)type;
                String uriVal = stype.toUriLiteral(stype.valueToString(id, p.isNullable(), p.getMaxLength(),
                    p.getPrecision(), p.getScale(), p.isUnicode()));

                return new URI(entitySetName + "(" + uriVal + ")");
            }
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName);
        } catch (URISyntaxException | ODataException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    private void createLink(final EdmNavigationProperty navigationProperty,
        final Entity srcEntity,
        final Entity destEntity) {
        setLink(navigationProperty, srcEntity, destEntity);

        final EdmNavigationProperty partnerNavigationProperty = navigationProperty.getPartner();
        if (partnerNavigationProperty != null) {
            setLink(partnerNavigationProperty, destEntity, srcEntity);
        }
    }

    private void setLink(final EdmNavigationProperty navigationProperty,
        final Entity srcEntity,
        final Entity targetEntity) {
        if (navigationProperty.isCollection()) {
            setLinks(srcEntity, navigationProperty.getName(), targetEntity);
        } else {
            setLink(srcEntity, navigationProperty.getName(), targetEntity);
        }
    }

    private void setLink(final Entity entity, final String navigationPropertyName, final Entity target) {
        Link link = entity.getNavigationLink(navigationPropertyName);
        if (link == null) {
            link = new Link();
            link.setRel(Constants.NS_NAVIGATION_LINK_REL + navigationPropertyName);
            link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
            link.setTitle(navigationPropertyName);
            link.setHref(target.getId().toASCIIString());

            entity.getNavigationLinks().add(link);
        }
        link.setInlineEntity(target);
    }

    private void setLinks(final Entity entity, final String navigationPropertyName, final Entity... targets) {
        if (targets.length == 0) {
            return;
        }

        Link link = entity.getNavigationLink(navigationPropertyName);
        if (link == null) {
            link = new Link();
            link.setRel(Constants.NS_NAVIGATION_LINK_REL + navigationPropertyName);
            link.setType(Constants.ENTITY_SET_NAVIGATION_LINK_TYPE);
            link.setTitle(navigationPropertyName);
            link.setHref(entity.getId().toASCIIString() + "/" + navigationPropertyName);

            EntityCollection target = new EntityCollection();
            target.getEntities().addAll(Arrays.asList(targets));
            link.setInlineEntitySet(target);

            entity.getNavigationLinks().add(link);
        } else {
            link.getInlineEntitySet().getEntities().addAll(Arrays.asList(targets));
        }
    }

    private boolean isKey(EdmEntityType edmEntityType, String propertyName) {
        List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
        for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
            String keyPropertyName = propRef.getName();
            if (keyPropertyName.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    private Entity createEntity(EdmEntitySet edmEntitySet,
        EdmEntityType edmEntityType,
        Entity entity,
        List<Entity> entityList,
        final String rawServiceUri) throws ODataApplicationException {

        // 1.) Create the entity
        final Entity newEntity = new Entity();
        newEntity.setType(entity.getType());

        // Create the new key of the entity
        Object newId = entity.getProperty("Id").getValue();

        // Add all provided properties
        newEntity.getProperties().addAll(entity.getProperties());

        // Add the key property
        newEntity.getProperties().add(new Property(null, "Id", ValueType.PRIMITIVE, newId));
        newEntity.setId(createId(edmEntitySet.getName(), newId));

        // 2.1.) Apply binding links
        for (final Link link : entity.getNavigationBindings()) {
            final EdmNavigationProperty edmNavigationProperty = edmEntityType.getNavigationProperty(link.getTitle());
            final EdmEntitySet targetEntitySet = (EdmEntitySet)edmEntitySet.getRelatedBindingTarget(link.getTitle());

            if (edmNavigationProperty.isCollection() && link.getBindingLinks() != null) {
                for (final String bindingLink : link.getBindingLinks()) {
                    final Entity relatedEntity = readEntityByBindingLink(bindingLink, targetEntitySet, rawServiceUri);
                    createLink(edmNavigationProperty, newEntity, relatedEntity);
                }
            } else if (!edmNavigationProperty.isCollection() && link.getBindingLink() != null) {
                final Entity relatedEntity = readEntityByBindingLink(link.getBindingLink(), targetEntitySet,
                    rawServiceUri);
                createLink(edmNavigationProperty, newEntity, relatedEntity);
            }
        }

        // 2.2.) Create nested entities
        for (final Link link : entity.getNavigationLinks()) {
            final EdmNavigationProperty edmNavigationProperty = edmEntityType.getNavigationProperty(link.getTitle());
            final EdmEntitySet targetEntitySet = (EdmEntitySet)edmEntitySet.getRelatedBindingTarget(link.getTitle());

            if (edmNavigationProperty.isCollection() && link.getInlineEntitySet() != null) {
                for (final Entity nestedEntity : link.getInlineEntitySet().getEntities()) {
                    final Entity newNestedEntity = createEntityData(targetEntitySet, nestedEntity, rawServiceUri);
                    createLink(edmNavigationProperty, newEntity, newNestedEntity);
                }
            } else if (!edmNavigationProperty.isCollection() && link.getInlineEntity() != null) {
                final Entity newNestedEntity = createEntityData(targetEntitySet, link.getInlineEntity(), rawServiceUri);
                createLink(edmNavigationProperty, newEntity, newNestedEntity);
            }
        }

        entityList.add(newEntity);

        return newEntity;
    }

    private Entity readEntityByBindingLink(final String entityId,
        final EdmEntitySet edmEntitySet,
        final String rawServiceUri) throws ODataApplicationException {

        UriResourceEntitySet entitySetResource = null;
        try {
            entitySetResource = odata.createUriHelper().parseEntityId(edm.getEdm(), entityId, rawServiceUri);

            if (!entitySetResource.getEntitySet().getName().equals(edmEntitySet.getName())) {
                throw new ODataApplicationException(
                    "Execpted an entity-id for entity set " + edmEntitySet.getName() + " but found id for entity set "
                        + entitySetResource.getEntitySet().getName(), HttpStatusCode.BAD_REQUEST.getStatusCode(),
                    Locale.ENGLISH);
            }
        } catch (DeserializerException e) {
            throw new ODataApplicationException(entityId + " is not a valid entity-Id",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }

        return read(entitySetResource.getEntitySet(), entitySetResource.getKeyPredicates());
    }

    private boolean entityIdExists(int id, List<Entity> entityList) {

        for (Entity entity : entityList) {
            Integer existingID = (Integer)entity.getProperty("Id").getValue();
            if (existingID.intValue() == id) {
                return true;
            }
        }

        return false;
    }

}
