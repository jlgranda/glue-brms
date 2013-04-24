/*
 * Copyright 2013 jlgranda.
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
public enum TimeUnit {

    CENTURY(3155692597470L, "Century"),
    DAY(1000L * 60L * 60L * 24L, "Day"),
    DECADE(315569259747L, "Decade"),
    HOUR(1000L * 60L * 60L, "Hour"),
    JUSTNOW(1000L * 60L * 5L, "JustNow"),
    MILLENNIUM(31556926000000L, "Millenium"),
    MILLISECOND(1, "Millisecond"),
    MINUTE(1000L * 60L, "Minute"),
    MONTH(2629743830L, "Month"),
    SECOND(1000L, "Second"),
    WEEK(1000L * 60L * 60L * 24L * 7L, "Week"),
    YEAR(2629743830L * 12L, "Year");
    private long millisPerUnit;
    private String resourceKeyPrefix;

    private TimeUnit(long millisPerUnit, String resourceKeyPrefix) {
        this.millisPerUnit = millisPerUnit;
        this.resourceKeyPrefix = resourceKeyPrefix;
    }

    public String getResourceKeyPrefix() {
        return resourceKeyPrefix;
    }
    
    public long getMillisPerUnit(){
        return millisPerUnit;
    }
}
