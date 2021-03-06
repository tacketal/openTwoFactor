/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 31. July 2004 by Joe Walnes
 */
package org.openTwoFactor.clientExt.com.thoughtworks.xstream.converters.extended;


import java.util.regex.Pattern;

import org.openTwoFactor.clientExt.com.thoughtworks.xstream.converters.Converter;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.converters.MarshallingContext;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.converters.UnmarshallingContext;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Ensures java.util.regex.Pattern is compiled upon deserialization.
 */
public class RegexPatternConverter implements Converter {

    private Converter defaultConverter;

    public RegexPatternConverter(Converter defaultConverter) {
        this.defaultConverter = defaultConverter;
    }

    public boolean canConvert(final Class type) {
        return type.equals(Pattern.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        defaultConverter.marshal(source, writer, context);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Pattern notCompiled = (Pattern) defaultConverter.unmarshal(reader, context);
        return Pattern.compile(notCompiled.pattern(), notCompiled.flags());
    }

}
