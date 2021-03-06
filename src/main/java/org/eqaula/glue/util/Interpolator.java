/*
 * Copyright 2012 jlgranda.
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
package org.eqaula.glue.util;

/**
 *
 * @author jlgranda
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Populates an interpolated string using the given template and parameters:
 * <p/>
 * <b>For example:</b><br>
 * Template: <code>"This is a {0} template with {1} parameters. Just {1}."</code><br>
 * Parameters: <code>"simple", 2</code><br>
 * Result: <code>"This is a simple template with 2 parameters. Just 2"</code>
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Interpolator {
    private static final String templateRegex = "\\{(\\d+)\\}";
    private static final Pattern templatePattern = Pattern.compile(templateRegex);

    /**
     * Populate a template with the corresponding parameters.
     */
    public static String interpolate(final String template, final Object... params) {
        StringBuffer result = new StringBuffer();
        if ((template != null) && (params != null)) {
            Matcher matcher = templatePattern.matcher(template);
            while (matcher.find()) {
                int index = Integer.valueOf(matcher.group(1));
                Object value = matcher.group();

                if (params.length > index) {
                    if (params[index] != null) {
                        value = params[index];
                    }
                }
                matcher.appendReplacement(result, value.toString());
            }
            matcher.appendTail(result);
        } else if (template != null) {
            result = new StringBuffer(template);
        }
        return result.toString();
    }
}
