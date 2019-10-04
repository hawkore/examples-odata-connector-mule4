/*******************************************************************************
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
 ******************************************************************************/
package org.apache.olingo.sample.server.v2.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;

/**
 * The type Manufacturer.
 */
@EdmEntityType(namespace = "MyFormula")
@EdmEntitySet(name = "Manufacturers")
public class Manufacturer {

    @EdmKey
    @EdmProperty
    private String id;
    @EdmProperty
    private String name;
    @EdmProperty
    private Calendar founded;
    @EdmProperty
    private Address address;
    @EdmNavigationProperty(name = "Cars", toType = Car.class, toMultiplicity = Multiplicity.MANY)
    private List<Car> cars = new ArrayList<Car>();

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id
     *     the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name
     *     the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets updated.
     *
     * @return the updated
     */
    public Calendar getUpdated() {
        return founded;
    }

    /**
     * Sets founded.
     *
     * @param updated
     *     the updated
     */
    public void setFounded(Calendar updated) {
        this.founded = updated;
    }

    /**
     * Gets address.
     *
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets address.
     *
     * @param address
     *     the address
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Gets cars.
     *
     * @return the cars
     */
    public List<Car> getCars() {
        return cars;
    }

    /**
     * Sets cars.
     *
     * @param cars
     *     the cars
     */
    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Manufacturer{" + "id=" + id + ", name=" + name + ", updated=" + founded + ", address=" + address
                   + ", cars=" + cars.size() + '}';
    }

}
