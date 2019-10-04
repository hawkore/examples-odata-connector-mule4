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

import java.util.Date;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;

/**
 * The type Car.
 */
@EdmEntityType(namespace = "MyFormula")
@EdmEntitySet(name = "Cars")
public class Car {

    @EdmKey
    @EdmProperty
    private String id;
    @EdmProperty
    private String model;
    @EdmNavigationProperty
    private Manufacturer manufacturer;
    @EdmNavigationProperty
    private Driver driver;
    @EdmProperty
    private Double price;
    @EdmProperty
    private Integer modelYear;
    @EdmProperty(type = EdmType.DATE_TIME)
    private Date updated;

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
     * Gets model.
     *
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets model.
     *
     * @param model
     *     the model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Gets manufacturer.
     *
     * @return the manufacturer
     */
    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets manufacturer.
     *
     * @param manufacturer
     *     the manufacturer
     */
    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Gets price.
     *
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets price.
     *
     * @param price
     *     the price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets model year.
     *
     * @return the model year
     */
    public int getModelYear() {
        return modelYear;
    }

    /**
     * Sets model year.
     *
     * @param modelYear
     *     the model year
     */
    public void setModelYear(int modelYear) {
        this.modelYear = modelYear;
    }

    /**
     * Gets updated.
     *
     * @return the updated
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * Sets updated.
     *
     * @param updated
     *     the updated
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * Gets driver.
     *
     * @return the driver
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Sets driver.
     *
     * @param driver
     *     the driver
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Car{" + "id=" + id + ", model=" + model + ", manufacturer id=" + manufacturer.getId() + ", driver id="
                   + driver.getId() + ", price=" + price + ", modelYear=" + modelYear + ", updated=" + updated + '}';
    }

}
