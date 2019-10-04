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
package org.apache.olingo.sample.server.v4.edmprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

/**
 * The type Cars edm provider.
 */
public class CarsEdmProvider extends CsdlAbstractEdmProvider {

    /**
     * The constant NAMESPACE.
     */
    // Service Namespace
    public static final String NAMESPACE = "olingo.odata.sample";
    /**
     * The constant CONTAINER_NAME.
     */
    // EDM Container
    public static final String CONTAINER_NAME = "MyFormula";
    /**
     * The constant CONTAINER_FQN.
     */
    public static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
    /**
     * The constant ET_CAR.
     */
    // Entity Types Names
    public static final FullQualifiedName ET_CAR = new FullQualifiedName(NAMESPACE, "Car");
    /**
     * The constant ET_MANUFACTURER.
     */
    public static final FullQualifiedName ET_MANUFACTURER = new FullQualifiedName(NAMESPACE, "Manufacturer");
    /**
     * The constant ET_DRIVER.
     */
    public static final FullQualifiedName ET_DRIVER = new FullQualifiedName(NAMESPACE, "Driver");
    /**
     * The constant CT_ADDRESS.
     */
    // Complex Type Names
    public static final FullQualifiedName CT_ADDRESS = new FullQualifiedName(NAMESPACE, "Address");
    /**
     * The constant ES_CARS_NAME.
     */
    // Entity Set Names
    public static final String ES_CARS_NAME = "Cars";
    /**
     * The constant ES_DRIVERS_NAME.
     */
    public static final String ES_DRIVERS_NAME = "Drivers";
    /**
     * The constant ES_MANUFACTURER_NAME.
     */
    public static final String ES_MANUFACTURER_NAME = "Manufacturers";

    /**
     * Gets entity type.
     *
     * @param entityTypeName
     *     the entity type name
     * @return the entity type
     * @throws ODataException
     *     the o data exception
     */
    @Override
    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {
        if (ET_CAR.equals(entityTypeName)) {
            return new CsdlEntityType().setName(ET_CAR.getName())
                       .setKey(Arrays.asList(new CsdlPropertyRef().setName("Id"))).setProperties(Arrays.asList(
                    new CsdlProperty().setName("Id").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("Model").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("ModelYear").setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName())
                        .setMaxLength(4),
                    new CsdlProperty().setName("Price").setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName())
                        .setScale(2),
                    new CsdlProperty().setName("Currency").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                        .setMaxLength(3), new CsdlProperty().setName("Updated").setPrecision(3)
                                              .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName())))
                       .setNavigationProperties(Arrays.asList(
                           new CsdlNavigationProperty().setName("Manufacturer").setType(ET_MANUFACTURER),
                           new CsdlNavigationProperty().setName("Driver").setType(ET_DRIVER)));
        } else if (ET_MANUFACTURER.equals(entityTypeName)) {
            return new CsdlEntityType().setName(ET_MANUFACTURER.getName())
                       .setKey(Arrays.asList(new CsdlPropertyRef().setName("Id"))).setProperties(Arrays.asList(
                    new CsdlProperty().setName("Id").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("Name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("Founded").setPrecision(3)
                        .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName()),
                    new CsdlProperty().setName("Address").setType(CT_ADDRESS))).setNavigationProperties(
                    Arrays.asList(new CsdlNavigationProperty().setName("Cars").setType(ET_CAR).setCollection(true)));
        } else if (ET_DRIVER.equals(entityTypeName)) {
            return new CsdlEntityType().setName(ET_DRIVER.getName())
                       .setKey(Arrays.asList(new CsdlPropertyRef().setName("Id"))).setProperties(Arrays.asList(
                    new CsdlProperty().setName("Id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()),
                    new CsdlProperty().setName("Name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("Lastname").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("Nickname").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("Birthday").setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName())))
                       .setNavigationProperties(
                           Arrays.asList(new CsdlNavigationProperty().setName("Car").setType(ET_CAR)));
        }
        return null;
    }

    /**
     * Gets complex type.
     *
     * @param complexTypeName
     *     the complex type name
     * @return the complex type
     * @throws ODataException
     *     the o data exception
     */
    @Override
    public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
        if (CT_ADDRESS.equals(complexTypeName)) {
            return new CsdlComplexType().setName(CT_ADDRESS.getName()).setProperties(Arrays.asList(
                new CsdlProperty().setName("Street").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                new CsdlProperty().setName("City").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                new CsdlProperty().setName("ZipCode").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                new CsdlProperty().setName("Country").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())));
        }
        return null;
    }

    /**
     * Gets entity set.
     *
     * @param entityContainer
     *     the entity container
     * @param entitySetName
     *     the entity set name
     * @return the entity set
     * @throws ODataException
     *     the o data exception
     */
    @Override
    public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName)
        throws ODataException {
        if (CONTAINER_FQN.equals(entityContainer)) {
            if (ES_CARS_NAME.equals(entitySetName)) {
                return new CsdlEntitySet().setName(ES_CARS_NAME).setType(ET_CAR).setNavigationPropertyBindings(Arrays
                                                                                                                   .asList(
                                                                                                                       new CsdlNavigationPropertyBinding()
                                                                                                                           .setPath(
                                                                                                                               "Manufacturer")
                                                                                                                           .setTarget(
                                                                                                                               CONTAINER_FQN
                                                                                                                                   .getFullQualifiedNameAsString()
                                                                                                                                   + "/"
                                                                                                                                   + ES_MANUFACTURER_NAME),
                                                                                                                       new CsdlNavigationPropertyBinding()
                                                                                                                           .setPath(
                                                                                                                               "Driver")
                                                                                                                           .setTarget(
                                                                                                                               CONTAINER_FQN
                                                                                                                                   .getFullQualifiedNameAsString()
                                                                                                                                   + "/"
                                                                                                                                   + ES_DRIVERS_NAME)

                                                                                                                   )

                );
            } else if (ES_MANUFACTURER_NAME.equals(entitySetName)) {
                return new CsdlEntitySet().setName(ES_MANUFACTURER_NAME).setType(ET_MANUFACTURER)
                           .setNavigationPropertyBindings(Arrays.asList(
                               new CsdlNavigationPropertyBinding().setPath("Cars")
                                   .setTarget(CONTAINER_FQN.getFullQualifiedNameAsString() + "/" + ES_CARS_NAME)));
            } else if (ES_DRIVERS_NAME.equals(entitySetName)) {
                return new CsdlEntitySet().setName(ES_DRIVERS_NAME).setType(ET_DRIVER).setNavigationPropertyBindings(
                    Arrays.asList(new CsdlNavigationPropertyBinding().setPath("Car")
                                      .setTarget(CONTAINER_FQN.getFullQualifiedNameAsString() + "/" + ES_CARS_NAME)));
            }
        }

        return null;
    }

    /**
     * Gets schemas.
     *
     * @return the schemas
     * @throws ODataException
     *     the o data exception
     */
    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);
        // EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.add(getEntityType(ET_CAR));
        entityTypes.add(getEntityType(ET_MANUFACTURER));
        entityTypes.add(getEntityType(ET_DRIVER));
        schema.setEntityTypes(entityTypes);

        // ComplexTypes
        List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
        complexTypes.add(getComplexType(CT_ADDRESS));
        schema.setComplexTypes(complexTypes);

        // EntityContainer
        schema.setEntityContainer(getEntityContainer());
        schemas.add(schema);

        return schemas;
    }

    /**
     * Gets entity container.
     *
     * @return the entity container
     * @throws ODataException
     *     the o data exception
     */
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName(CONTAINER_FQN.getName());

        // EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        container.setEntitySets(entitySets);
        entitySets.add(getEntitySet(CONTAINER_FQN, ES_CARS_NAME));
        entitySets.add(getEntitySet(CONTAINER_FQN, ES_MANUFACTURER_NAME));
        entitySets.add(getEntitySet(CONTAINER_FQN, ES_DRIVERS_NAME));
        return container;
    }

    /**
     * Gets entity container info.
     *
     * @param entityContainerName
     *     the entity container name
     * @return the entity container info
     * @throws ODataException
     *     the o data exception
     */
    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
        throws ODataException {
        if (entityContainerName == null || CONTAINER_FQN.equals(entityContainerName)) {
            return new CsdlEntityContainerInfo().setContainerName(CONTAINER_FQN);
        }
        return null;
    }

}
