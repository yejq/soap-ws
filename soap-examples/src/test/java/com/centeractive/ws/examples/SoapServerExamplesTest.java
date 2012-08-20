/**
 * Copyright (c) 2012 centeractive ag. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.centeractive.ws.examples;

import com.centeractive.ws.builder.core.SoapBuilder;
import com.centeractive.ws.builder.core.SoapOperation;
import com.centeractive.ws.builder.core.SoapParser;
import com.centeractive.ws.builder.soap.XmlUtils;
import com.centeractive.ws.builder.utils.ResourceUtils;
import com.centeractive.ws.server.core.SoapServer;
import com.centeractive.ws.server.responder.AbstractResponder;
import com.centeractive.ws.server.responder.AutoResponder;
import org.junit.Test;
import org.springframework.ws.soap.SoapMessage;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.net.URL;

/**
 * @author Tom Bujok
 * @since 1.0.0
 */
public class SoapServerExamplesTest {

    @Test
    public void createServer() {
        SoapServer server = SoapServer.builder()
                .httpPort(9090)
                .build();
        server.start();
        server.stop();
    }

    @Test
    public void createServer_registerAutoResponder() throws WSDLException {
        SoapServer server = SoapServer.builder()
                .httpPort(9090)
                .build();
        server.start();

        QName bindingName = new QName("http://centeractive.com/stockquote.wsdl", "StockQuoteSoapBinding");
        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("/", "stockquote-service.wsdl");

        SoapParser parser = new SoapParser(wsdlUrl);
        AutoResponder responder = new AutoResponder(parser.getBuilder(bindingName));

        server.registerRequestResponder("/service", responder);
        server.stop();
    }

    @Test
    public void createServer_registerCustomResponder() throws WSDLException {
        SoapServer server = SoapServer.builder()
                .httpPort(9090)
                .build();
        server.start();

        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("/", "stockquote-service.wsdl");
        SoapParser parser = new SoapParser(wsdlUrl);
        // assumption -> we take the first binding
        final SoapBuilder builder = parser.getBuilder(parser.getBindings().get(0));
        AbstractResponder customResponder = new AbstractResponder(builder) {
            @Override
            public Source respond(SoapOperation invokedOperation, SoapMessage message) {
                try {
                    // build the response using builder
                    String response = builder.buildOutputMessage(invokedOperation);
                    // here you can tweak the response -> for example with XSLT
                    //...
                    return XmlUtils.xmlStringToSource(response);
                } catch (Exception e) {
                    // will automatically generate SOAP-FAULT
                    throw new RuntimeException("my custom error", e);
                }
            }
        };


        server.registerRequestResponder("/service", customResponder);
        server.stop();
    }

}