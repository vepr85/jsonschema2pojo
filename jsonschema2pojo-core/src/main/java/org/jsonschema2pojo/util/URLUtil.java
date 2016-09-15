/**
 * Copyright ¬© 2010-2014 Nokia
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.util;

import org.apache.commons.lang.StringUtils;
import org.jsonschema2pojo.URLProtocol;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtil {

    public static URLProtocol parseProtocol(String input) {
        return URLProtocol.fromString(StringUtils.substringBefore(input, ":"));
    }

    public static URL parseURL(String input) {
        try {
            switch (parseProtocol(input)) {
                case NO_PROTOCOL:
                    return new File(input).toURI().toURL();
                default:
                    return URI.create(input).toURL();
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Unable to parse source: %s", input), e);
        }
    }

    public static File getFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("URL contains an invalid URI syntax: %s", url), e);
        }
    }

    final static String patternIllegal = "[!&\\?\\-;~,#@*+%{}\\(\\)<>\\[\\]|\"\'^]";
    final static String patternLegal = "[\\.\\/\\\\:_]";

    public static String splitFileNameToPackages(String schemaId) {

        if (containsPattern(schemaId, patternIllegal)) {
            throw new IllegalArgumentException("Schema id contains illegal split symbols");
        }


        Matcher matcher = getMatcherPattern(schemaId, patternLegal);
        if (!matcher.find()) {
            return schemaId;
        } else {
            // допустимые разделители имеются
            String legalSymbol = matcher.group();
            // проверяем однородность разделителей - есть ли в строке хотя бы ещё 1 допустимый разделитель
            if (containsDifferentLegals(schemaId, legalSymbol)) {
                throw new IllegalArgumentException("Schema id contains different split symbols");
            } else {
                //TODO: проверить, что нет подряд 2 допустимых символов!!!!!
                return schemaId;
            }
        }
    }


    private static boolean containsDifferentLegals(String toExamine, String legalSymbol) {

        String otherPattern = clearLegalPattern(legalSymbol);
        Pattern pattern = Pattern.compile(otherPattern);
        Matcher matcher = pattern.matcher(toExamine);

        return matcher.find();
    }

    private static String clearLegalPattern(String symbol) {

        switch (symbol) {

            case ".":
                return patternLegal.replace("\\.", "");
            case "/":
                return patternLegal.replace("\\/", "");
            case "\\":
                return patternLegal.replace("\\\\", "");
            case ":":
                return patternLegal.replace(":", "");
            case "_":
                return patternLegal.replace(":", "");

            default:
                return symbol;
        }
    }

    private static boolean containsPattern(String toExamine, String patternStr) {

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }

    private static Matcher getMatcherPattern(String toExamine, String patternStr) {

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(toExamine);
        return matcher;
    }
}
