/**
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jsonschema2pojo.Schema;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Applies JsonIgnored json schema rule
 * Created by abyakimenko on 15.09.2016.
 */
public class FeaturesIgnoreArrayRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    public static final String JSON_IGNORED_COMMENT_TEXT = "\n(JsonIgnore)";

    protected FeaturesIgnoreArrayRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) {

        List<String> jsonIgnoredFieldMethods = new ArrayList<>();
        JsonNode properties = schema.getContent().get("properties");

        for (Iterator<JsonNode> iterator = node.elements(); iterator.hasNext(); ) {

            JsonNode jsonIgnoreField = iterator.next();
            String ignoreArrayItem = jsonIgnoreField.fieldNames().next();

            JsonNode propertyNode = null;

            if (properties != null) {
                propertyNode = properties.findValue(ignoreArrayItem);
            }

            String fieldName = ruleFactory.getNameHelper().getPropertyName(ignoreArrayItem, propertyNode);
            JFieldVar field = jclass.fields().get(fieldName);

            if (field == null) {
                continue;
            }

            addJavaDoc(field);

            field.annotate(JsonIgnore.class);
            //field.annotations().remove(JsonProperty.class);
            // в зависимотсти от содержимого схемы отработать с геттерами и сеттерами, поставив им соответствующую
            // аннотацию @JsonIgnore
//
            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {

            }
//            String gg = "";
//
//            jsonIgnoredFieldMethods.add(getGetterName(fieldName, field.type(), node));
//            jsonIgnoredFieldMethods.add(getSetterName(fieldName, node));
        }

        return jclass;
    }

    private void addJavaDoc(JDocCommentable docCommentable) {
        JDocComment javadoc = docCommentable.javadoc();
        javadoc.append(JSON_IGNORED_COMMENT_TEXT);
    }

    private void addNotNullAnnotation(JFieldVar field) {
        field.annotate(NotNull.class);
    }

    private String getSetterName(String propertyName, JsonNode node) {
        return ruleFactory.getNameHelper().getSetterName(propertyName, node);
    }

    private String getGetterName(String propertyName, JType type, JsonNode node) {
        return ruleFactory.getNameHelper().getGetterName(propertyName, type, node);
    }

}
