package com.agilejava.docbkx.maven;

/*
 * Copyright 2006 Wilfred Springer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A simple {@link URIResolver} decorator that will bail out of the normal way
 * of retrieving entities as soon as the publicId or systemId matches a given
 * URN.
 * 
 * @author Wilfred Springer
 * 
 */
public class StylesheetResolver implements URIResolver {

    private String urn;

    private Source stylesheet;

    private URIResolver wrapped;

    public StylesheetResolver(String urn, Source stylesheet, URIResolver wrapped) {
        this.urn = urn;
        this.stylesheet = stylesheet;
        this.wrapped = wrapped;
    }

    public Source resolve(String href, String base) throws TransformerException {
        if (urn.equals(href)) {
            return stylesheet;
        } else {
            return wrapped.resolve(href, base);
        }
    }

}
