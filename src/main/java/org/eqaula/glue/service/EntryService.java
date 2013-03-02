/*
 * Copyright 2013 lucho.
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
package org.eqaula.glue.service;

import javax.inject.Named;
import javax.enterprise.context.Dependent;

/**
 *
 * @author lucho
 */
@Named(value = "entrieService")
@Dependent
public class EntryService {

    /**
     * Creates a new instance of EntryService
     */
    public EntryService() {
    }
}