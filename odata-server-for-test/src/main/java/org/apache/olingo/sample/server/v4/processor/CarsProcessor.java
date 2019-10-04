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
package org.apache.olingo.sample.server.v4.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.sample.server.v4.data.DataProvider;
import org.apache.olingo.sample.server.v4.data.FilterExpressionVisitor;
import org.apache.olingo.sample.server.v4.util.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.deserializer.batch.BatchOptions;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.api.processor.BatchProcessor;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.processor.ReferenceCollectionProcessor;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

/**
 * This processor will deliver entity collections, single entities as well as properties of an entity.
 * This is a very simple example which should give you a rough guideline on how to implement such an processor.
 * See the JavaDoc of the server.api interfaces for more information.
 */
public class CarsProcessor
    implements EntityCollectionProcessor, EntityProcessor, PrimitiveProcessor, PrimitiveValueProcessor,
                   ComplexProcessor, ReferenceProcessor, ReferenceCollectionProcessor, BatchProcessor {

    private OData odata;
    private final DataProvider dataProvider;
    private ServiceMetadata edm;

    /**
     * Instantiates a new Cars processor.
     *
     * @param dataProvider
     *     the data provider
     */
    // This constructor is application specific and not mandatory for the Olingo library. We use it here to simulate the
    // database access
    public CarsProcessor(final DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Init.
     *
     * @param odata
     *     the odata
     * @param edm
     *     the edm
     */
    @Override
    public void init(OData odata, ServiceMetadata edm) {
        this.odata = odata;
        this.edm = edm;
    }

    /**
     * Read entity collection.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param responseFormat
     *     the response format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void readEntityCollection(ODataRequest request,
        ODataResponse response,
        UriInfo uriInfo,
        ContentType responseFormat) throws ODataApplicationException, SerializerException {

        // 1st retrieve the requested EntitySet from the uriInfo
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2nd: fetch the data from backend for this requested EntitySetName
        EntityCollection entityCollection = dataProvider.readAll(edmEntitySet);

        // 3rd: apply System Query Options
        // modify the result set according to the query options, specified by the end user
        List<Entity> fullEntityList = entityCollection.getEntities();
        EntityCollection returnEntityCollection = new EntityCollection();

        FilterOption filterOption = uriInfo.getFilterOption();
        Expression filterExpression = filterOption != null ? filterOption.getExpression() : null;

        List<Entity> entityList = new ArrayList<>();

        // handle $filter
        for (Entity entity : fullEntityList) {
            try {
                if (filterExpression != null) {
                    FilterExpressionVisitor expressionVisitor = new FilterExpressionVisitor(entity);
                    // Start evaluating the expression
                    Object visitorResult = filterExpression.accept(expressionVisitor);

                    // The result of the filter expression must be of type Edm.Boolean
                    if (visitorResult instanceof Boolean) {
                        if (!Boolean.TRUE.equals(visitorResult)) {
                            // The expression evaluated to false (or null), not add it
                            continue;
                        }
                    } else {
                        throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean",
                            HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
                    }
                }
            } catch (ExpressionVisitException e) {
                throw new ODataApplicationException("Exception in filter evaluation",
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
            }
            entityList.add(entity);
        }

        // handle $skip
        SkipOption skipOption = uriInfo.getSkipOption();
        if (skipOption != null) {
            int skipNumber = skipOption.getValue();
            if (skipNumber >= 0) {
                if (skipNumber <= entityList.size()) {
                    entityList = entityList.subList(skipNumber, entityList.size());
                } else {
                    // The client skipped all entities
                    entityList.clear();
                }
            } else {
                throw new ODataApplicationException("Invalid value for $skip",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }

        // handle $top
        TopOption topOption = uriInfo.getTopOption();
        if (topOption != null) {
            int topNumber = topOption.getValue();
            if (topNumber >= 0) {
                if (topNumber <= entityList.size()) {
                    entityList = entityList.subList(0, topNumber);
                }  // else the client has requested more entities than available => return what we have
            } else {
                throw new ODataApplicationException("Invalid value for $top",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }

        // handle $count: return the original number of entities, ignore $top and $skip
        CountOption countOption = uriInfo.getCountOption();
        if (countOption != null) {
            boolean isCount = countOption.getValue();
            if (isCount) {
                returnEntityCollection.setCount(entityList.size());
            }
        }

        // after applying the query options, create EntityCollection based on the reduced list
        returnEntityCollection.getEntities().addAll(entityList);

        // 4th: create a serializer based on the requested format (json)
        ODataSerializer serializer = odata.createSerializer(responseFormat);

        // and serialize the content: createHttpClientFactoryWrapper from the EntitySet object to InputStream
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        final ExpandOption expand = uriInfo.getExpandOption();
        final SelectOption select = uriInfo.getSelectOption();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().contextURL(
            isODataMetadataNone(responseFormat) ? null : getContextUrl(edmEntitySet, false, expand, select, null))
                                                     .id(id).count(countOption).expand(expand).select(select).build();
        InputStream serializedContent = serializer.entityCollection(edm, edmEntityType, returnEntityCollection, opts)
                                            .getContent();

        // 5th: configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    /**
     * Read entity.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param requestedContentType
     *     the requested content type
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void readEntity(final ODataRequest request,
        ODataResponse response,
        final UriInfo uriInfo,
        final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
        // First we have to figure out which entity set the requested entity is in
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

        // Next we fetch the requested entity from the database
        Entity entity;
        try {
            entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
        } catch (ODataApplicationException e) {
            throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
        }

        if (entity == null) {
            // If no entity was found for the given key we throw an exception.
            throw new ODataApplicationException("No entity found for this key",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        } else {
            // If an entity was found we proceed by serializing it and sending it to the client.
            ODataSerializer serializer = odata.createSerializer(requestedContentType);
            final ExpandOption expand = uriInfo.getExpandOption();
            final SelectOption select = uriInfo.getSelectOption();
            InputStream serializedContent = serializer.entity(edm, edmEntitySet.getEntityType(), entity,
                EntitySerializerOptions.with().contextURL(isODataMetadataNone(requestedContentType)
                                                              ? null
                                                              : getContextUrl(edmEntitySet, true, expand, select, null))
                    .expand(expand).select(select).build()).getContent();
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
        }
    }

    /**
     * Read primitive.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param format
     *     the format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
        throws ODataApplicationException, SerializerException {
        readProperty(response, uriInfo, format, false);
    }

    /**
     * Read complex.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param format
     *     the format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
        throws ODataApplicationException, SerializerException {
        readProperty(response, uriInfo, format, true);
    }

    /**
     * Read primitive value.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param format
     *     the format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
        throws ODataApplicationException, SerializerException {
        // First we have to figure out which entity set the requested entity is in
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
        // Next we fetch the requested entity from the database
        final Entity entity;
        try {
            entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
        } catch (ODataApplicationException e) {
            throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
        }
        if (entity == null) {
            // If no entity was found for the given key we throw an exception.
            throw new ODataApplicationException("No entity found for this key",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        } else {
            // Next we get the property value from the entity and pass the value to serialization
            UriResourceProperty uriProperty = (UriResourceProperty)uriInfo.getUriResourceParts()
                                                                       .get(uriInfo.getUriResourceParts().size() - 1);
            EdmProperty edmProperty = uriProperty.getProperty();
            Property property = entity.getProperty(edmProperty.getName());
            if (property == null) {
                throw new ODataApplicationException("No property found", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ENGLISH);
            } else {
                if (property.getValue() == null) {
                    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
                } else {
                    String value = String.valueOf(property.getValue());
                    ByteArrayInputStream serializerContent = new ByteArrayInputStream(
                        value.getBytes(Charset.forName("UTF-8")));
                    response.setContent(serializerContent);
                    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
                    response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());
                }
            }
        }
    }

    private void readProperty(ODataResponse response, UriInfo uriInfo, ContentType contentType, boolean complex)
        throws ODataApplicationException, SerializerException {
        // To read a property we have to first get the entity out of the entity set
        final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
        Entity entity;
        try {
            entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
        } catch (ODataApplicationException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
                Locale.ENGLISH);
        }

        if (entity == null) {
            // If no entity was found for the given key we throw an exception.
            throw new ODataApplicationException("No entity found for this key",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        } else {
            // Next we get the property value from the entity and pass the value to serialization
            UriResourceProperty uriProperty = (UriResourceProperty)uriInfo.getUriResourceParts()
                                                                       .get(uriInfo.getUriResourceParts().size() - 1);
            EdmProperty edmProperty = uriProperty.getProperty();
            Property property = entity.getProperty(edmProperty.getName());
            if (property == null) {
                throw new ODataApplicationException("No property found", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ENGLISH);
            } else {
                if (property.getValue() == null) {
                    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
                } else {
                    ODataSerializer serializer = odata.createSerializer(contentType);
                    final ContextURL contextURL = isODataMetadataNone(contentType)
                                                      ? null
                                                      : getContextUrl(edmEntitySet, true, null, null,
                                                          edmProperty.getName());
                    InputStream serializerContent = complex
                                                        ? serializer.complex(edm, (EdmComplexType)edmProperty.getType(),
                        property, ComplexSerializerOptions.with().contextURL(contextURL).build()).getContent()
                                                        : serializer
                                                              .primitive(edm, (EdmPrimitiveType)edmProperty.getType(),
                                                                  property, PrimitiveSerializerOptions.with()
                                                                                .contextURL(contextURL)
                                                                                .scale(edmProperty.getScale())
                                                                                .nullable(edmProperty.isNullable())
                                                                                .precision(edmProperty.getPrecision())
                                                                                .maxLength(edmProperty.getMaxLength())
                                                                                .unicode(edmProperty.isUnicode())
                                                                                .build()).getContent();
                    response.setContent(serializerContent);
                    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
                    response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
                }
            }
        }
    }

    private Entity readEntityInternal(final UriInfoResource uriInfo, final EdmEntitySet entitySet)
        throws ODataApplicationException {
        // This method will extract the key values and pass them to the data provider
        final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet)uriInfo.getUriResourceParts().get(0);
        return dataProvider.read(entitySet, resourceEntitySet.getKeyPredicates());
    }

    private EdmEntitySet getEdmEntitySet(final UriInfoResource uriInfo) throws ODataApplicationException {
        final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        /*
         * To get the entity set we have to interpret all URI segments
         */
        if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Invalid resource type for first segment.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        /*
         * Here we should interpret the whole URI but in this example we do not support navigation so we throw an
         * exception
         */

        final UriResourceEntitySet uriResource = (UriResourceEntitySet)resourcePaths.get(0);
        return uriResource.getEntitySet();
    }

    private ContextURL getContextUrl(final EdmEntitySet entitySet,
        final boolean isSingleEntity,
        final ExpandOption expand,
        final SelectOption select,
        final String navOrPropertyPath) throws SerializerException {

        return ContextURL.with().entitySet(entitySet).selectList(
            odata.createUriHelper().buildContextURLSelectList(entitySet.getEntityType(), expand, select))
                   .suffix(isSingleEntity ? Suffix.ENTITY : null).navOrPropertyPath(navOrPropertyPath).build();
    }

    /**
     * Update primitive.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param requestFormat
     *     the request format
     * @param responseFormat
     *     the response format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws DeserializerException
     *     the deserializer exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void updatePrimitive(final ODataRequest request,
        final ODataResponse response,
        final UriInfo uriInfo,
        final ContentType requestFormat,
        final ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Primitive property update is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Update primitive value.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param requestFormat
     *     the request format
     * @param responseFormat
     *     the response format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void updatePrimitiveValue(final ODataRequest request,
        ODataResponse response,
        final UriInfo uriInfo,
        final ContentType requestFormat,
        final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Primitive property update is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Delete primitive.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @throws ODataApplicationException
     *     the o data application exception
     */
    @Override
    public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo)
        throws ODataApplicationException {
        throw new ODataApplicationException("Primitive property delete is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Delete primitive value.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void deletePrimitiveValue(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
        throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Primitive property update is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Update complex.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param requestFormat
     *     the request format
     * @param responseFormat
     *     the response format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws DeserializerException
     *     the deserializer exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void updateComplex(final ODataRequest request,
        final ODataResponse response,
        final UriInfo uriInfo,
        final ContentType requestFormat,
        final ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Complex property update is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Delete complex.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @throws ODataApplicationException
     *     the o data application exception
     */
    @Override
    public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
        throws ODataApplicationException {
        throw new ODataApplicationException("Complex property delete is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Is o data metadata none boolean.
     *
     * @param contentType
     *     the content type
     * @return the boolean
     */
    public static boolean isODataMetadataNone(final ContentType contentType) {
        return contentType.isCompatible(ContentType.APPLICATION_JSON) && ContentType.VALUE_ODATA_METADATA_NONE
                                                                             .equalsIgnoreCase(contentType.getParameter(
                                                                                 ContentType.PARAMETER_ODATA_METADATA));
    }

    /**
     * Create entity.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param requestFormat
     *     the request format
     * @param responseFormat
     *     the response format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws DeserializerException
     *     the deserializer exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void createEntity(ODataRequest request,
        ODataResponse response,
        UriInfo uriInfo,
        ContentType requestFormat,
        ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {

        // 1. Retrieve the entity type from the URI
        EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 2. create the data in backend
        // 2.1. retrieve the payload from the POST request for the entity to create and deserialize it
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
        Entity requestEntity = result.getEntity();
        // 2.2 do the creation in backend, which returns the newly created entity
        Entity createdEntity = dataProvider.createEntityData(edmEntitySet, requestEntity, request.getRawBaseUri());

        // 3. serialize the response (we have to return the created entity)
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl)
                                              .build(); // expand and select currently not supported

        ODataSerializer serializer = this.odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(edm, edmEntityType, createdEntity, options);

        //4. configure the response object
        response.setContent(serializedResponse.getContent());
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    /**
     * Update entity.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @param requestFormat
     *     the request format
     * @param responseFormat
     *     the response format
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws DeserializerException
     *     the deserializer exception
     * @throws SerializerException
     *     the serializer exception
     */
    @Override
    public void updateEntity(ODataRequest request,
        ODataResponse response,
        UriInfo uriInfo,
        ContentType requestFormat,
        ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {

        // 1. Retrieve the entity set which belongs to the requested entity
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 2. update the data in backend
        // 2.1. retrieve the payload from the PUT request for the entity to be updated
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
        Entity requestEntity = result.getEntity();
        // 2.2 do the modification in backend
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        // Note that this updateEntity()-method is invoked for both PUT or PATCH operations
        HttpMethod httpMethod = request.getMethod();
        dataProvider.updateEntityData(edmEntitySet, keyPredicates, requestEntity, httpMethod);

        //3. configure the response object
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }

    /**
     * Delete entity.
     *
     * @param request
     *     the request
     * @param response
     *     the response
     * @param uriInfo
     *     the uri info
     * @throws ODataApplicationException
     *     the o data application exception
     */
    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
        throws ODataApplicationException {

        // 1. Retrieve the entity set which belongs to the requested entity
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. delete the data in backend
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        dataProvider.deleteEntityData(edmEntitySet, keyPredicates);

        //3. configure the response object
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }

    /**
     * Read reference collection.
     *
     * @param oDataRequest
     *     the o data request
     * @param oDataResponse
     *     the o data response
     * @param uriInfo
     *     the uri info
     * @param contentType
     *     the content type
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void readReferenceCollection(ODataRequest oDataRequest,
        ODataResponse oDataResponse,
        UriInfo uriInfo,
        ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Read reference collection is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Read reference.
     *
     * @param oDataRequest
     *     the o data request
     * @param oDataResponse
     *     the o data response
     * @param uriInfo
     *     the uri info
     * @param contentType
     *     the content type
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void readReference(ODataRequest oDataRequest,
        ODataResponse oDataResponse,
        UriInfo uriInfo,
        ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Read reference is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Create reference.
     *
     * @param oDataRequest
     *     the o data request
     * @param oDataResponse
     *     the o data response
     * @param uriInfo
     *     the uri info
     * @param contentType
     *     the content type
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void createReference(ODataRequest oDataRequest,
        ODataResponse oDataResponse,
        UriInfo uriInfo,
        ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Create reference is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Update reference.
     *
     * @param oDataRequest
     *     the o data request
     * @param oDataResponse
     *     the o data response
     * @param uriInfo
     *     the uri info
     * @param contentType
     *     the content type
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void updateReference(ODataRequest oDataRequest,
        ODataResponse oDataResponse,
        UriInfo uriInfo,
        ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Update reference is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Delete reference.
     *
     * @param oDataRequest
     *     the o data request
     * @param oDataResponse
     *     the o data response
     * @param uriInfo
     *     the uri info
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void deleteReference(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo)
        throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Delete reference is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Process batch.
     *
     * @param facade
     *     the facade
     * @param request
     *     the request
     * @param response
     *     the response
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public void processBatch(final BatchFacade facade, final ODataRequest request, final ODataResponse response)
        throws ODataApplicationException, ODataLibraryException {

        // 1. Extract the boundary
        final String boundary = facade.extractBoundaryFromContentType(request.getHeader(HttpHeader.CONTENT_TYPE));

        // 2. Prepare the batch options
        final BatchOptions options = BatchOptions.with().rawBaseUri(request.getRawBaseUri())
                                         .rawServiceResolutionUri(request.getRawServiceResolutionUri()).build();

        // 3. Deserialize the batch request
        final List<BatchRequestPart> requestParts = odata.createFixedFormatDeserializer()
                                                        .parseBatchRequest(request.getBody(), boundary, options);

        // 4. Execute the batch request parts
        final List<ODataResponsePart> responseParts = new ArrayList<ODataResponsePart>();
        for (final BatchRequestPart part : requestParts) {
            responseParts.add(facade.handleBatchRequest(part));
        }
        // 5. Create a new boundary for the response
        final String responseBoundary = "batch_" + UUID.randomUUID().toString();

        // 6. Serialize the response content
        final InputStream responseContent = odata.createFixedFormatSerializer()
                                                .batchResponse(responseParts, responseBoundary);

        // 7. Setup response
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.MULTIPART_MIXED + ";boundary=" + responseBoundary);
        response.setContent(responseContent);
        response.setStatusCode(HttpStatusCode.ACCEPTED.getStatusCode());
    }

    /**
     * Process change set o data response part.
     *
     * @param facade
     *     the facade
     * @param requests
     *     the requests
     * @return the o data response part
     * @throws ODataApplicationException
     *     the o data application exception
     * @throws ODataLibraryException
     *     the o data library exception
     */
    @Override
    public ODataResponsePart processChangeSet(final BatchFacade facade, final List<ODataRequest> requests)
        throws ODataApplicationException, ODataLibraryException {
        /*
         * OData Version 4.0 Part 1: Protocol Plus Errata 02
         *      11.7.4 Responding to a Batch Request
         *
         *      All operations in a change set represent a single change unit so a service MUST successfully process and
         *      apply all the requests in the change set or else apply none of them. It is up to the service
         * implementation
         *      to define rollback semantics to undo any requests within a change set that may have been applied before
         *      another request in that same change set failed and thereby apply this all-or-nothing requirement.
         *      The service MAY execute the requests within a change set in any order and MAY return the responses to
         *  the
         *       individual requests in any order. The service MUST include the Content-ID header in each response
         * with the
         *      same value that the client specified in the corresponding request, so clients can correlate requests
         *      and responses.
         *
         * To keep things simple, we dispatch the requests within the change set to the other processor interfaces.
         */
        final List<ODataResponse> responses = new ArrayList<ODataResponse>();

        try {
            // storage.beginTransaction();

            for (final ODataRequest request : requests) {
                // Actual request dispatching to the other processor interfaces.
                final ODataResponse response = facade.handleODataRequest(request);

                // Determine if an error occurred while executing the request.
                // Exceptions thrown by the processors get caught and result in a proper OData response.
                final int statusCode = response.getStatusCode();
                if (statusCode < 400) {
                    // The request has been executed successfully. Return the response as a part of the change set
                    responses.add(response);
                } else {
                    // Something went wrong. Undo all previous requests in this Change Set
                    //  storage.rollbackTranscation();

                    /*
                     * In addition the response must be provided as follows:
                     *
                     * OData Version 4.0 Part 1: Protocol Plus Errata 02
                     *     11.7.4 Responding to a Batch Request
                     *
                     *     When a request within a change set fails, the change set response is not represented using
                     *     the multipart/mixed media type. Instead, a single response, using the application/http
                     * media type
                     *     and a Content-Transfer-Encoding header with a value of binary, is returned that applies to
                     *  all requests
                     *     in the change set and MUST be formatted according to the Error Handling defined
                     *     for the particular response format.
                     *
                     * This can be simply done by passing the response of the failed ODataRequest to a new instance of
                     * ODataResponsePart and setting the second parameter "isChangeSet" to false.
                     */
                    return new ODataResponsePart(response, false);
                }
            }

            // Everything went well, so commit the changes.
            // storage.commitTransaction();
            return new ODataResponsePart(responses, true);
        } catch (ODataApplicationException e) {
            // See below
            // storage.rollbackTranscation();
            throw e;
        } catch (ODataLibraryException e) {
            // The request is malformed or the processor implementation is not correct.
            // Throwing an exception will stop the whole batch request not only the change set!
            //   storage.rollbackTranscation();
            throw e;
        }
    }

}
