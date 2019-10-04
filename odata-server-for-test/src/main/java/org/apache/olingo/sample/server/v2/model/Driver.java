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

import java.util.Calendar;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;

/**
 * The type Driver.
 */
@EdmEntityType(namespace = "MyFormula")
@EdmEntitySet(name = "Drivers")
public class Driver {

    @EdmKey
    @EdmProperty
    private Long id;
    @EdmProperty
    private String name;
    @EdmProperty
    private String lastname;
    @EdmProperty
    private String nickname;
    @EdmNavigationProperty(name = "Car")
    private Car car;
    @EdmProperty(type = EdmType.DATE_TIME)
    private Calendar birthday;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
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
     * Gets lastname.
     *
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Sets lastname.
     *
     * @param lastname
     *     the lastname
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * Gets nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets nickname.
     *
     * @param nickname
     *     the nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gets car.
     *
     * @return the car
     */
    public Car getCar() {
        return car;
    }

    /**
     * Sets car.
     *
     * @param car
     *     the car
     */
    public void setCar(Car car) {
        this.car = car;
    }

    /**
     * Gets updated.
     *
     * @return the updated
     */
    public Calendar getUpdated() {
        return birthday;
    }

    /**
     * Sets birthday.
     *
     * @param birthday
     *     the birthday
     */
    public void setBirthday(Calendar birthday) {
        this.birthday = birthday;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Driver{id=" + id + ", name=" + name + ", lastname=" + lastname + ", nickname=" + nickname + ", car id="
                   + car.getId() + ", updated=" + birthday + '}';
    }

}
