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

package com.agilejava.maven.docbkx.spec;

public class Parameter {

    private String name;

    private String value;

    private String description;

    private String type = "string";

    public void setTypeFromRefType(String type) {
        if ("attribute set".equals(type)) {
            this.type = "attributeSet";
        } else {
            this.type = type;
        }
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        if ("boolean".equals(type)) {
            String result = description.replaceAll("non-zero (true)", "true");
            result = result.replaceAll("non-zero", "true");
            return result;
        } else {
            return description;
        }
    }

    public String getJavaIdentifier() {
        StringBuffer builder = new StringBuffer();
        int size = name.length();
        boolean nextUpperCase = false;
        for (int i = 0; i < size; i++) {
            char c = name.charAt(i);
            if (c == '.' || c == '-') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    builder.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }
    
}
