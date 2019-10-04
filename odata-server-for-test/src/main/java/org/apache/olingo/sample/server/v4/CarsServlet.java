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
package org.apache.olingo.sample.server.v4;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.sample.server.v4.data.DataProvider;
import org.apache.olingo.sample.server.v4.edmprovider.CarsEdmProvider;
import org.apache.olingo.sample.server.v4.processor.CarsProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Cars servlet.
 */
public class CarsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CarsServlet.class);
    private ODataHttpHandler handler;

    /**
     * Init.
     *
     * @throws ServletException
     *     the servlet exception
     */
    @Override
    public void init() throws ServletException {
        super.init();
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new CarsEdmProvider(), new ArrayList<EdmxReference>());
        DataProvider dataProvider = new DataProvider(edm, odata);
        LOG.info("Created new data provider.");
        handler = odata.createHandler(edm);
        handler.register(new CarsProcessor(dataProvider));
    }

    /**
     * Service.
     *
     * @param req
     *     the req
     * @param resp
     *     the resp
     * @throws ServletException
     *     the servlet exception
     * @throws IOException
     *     the io exception
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            handler.process(req, resp);
        } catch (RuntimeException e) {
            LOG.error("Server Error", e);
            throw new ServletException(e);
        }
    }

}
