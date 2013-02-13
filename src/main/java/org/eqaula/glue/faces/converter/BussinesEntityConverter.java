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
package org.eqaula.glue.faces.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.eqaula.glue.model.BussinesEntity;
import org.omnifaces.converter.SelectItemsConverter;
import org.picketlink.idm.impl.api.model.SimpleGroup;

/**
 *
 * @author jlgranda
 */
public class BussinesEntityConverter extends SelectItemsConverter {

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String key = null;
        if (value instanceof SimpleGroup) {
            key = ((SimpleGroup) value).getName();
        } else if (value instanceof BussinesEntity) {
            Long id = ((BussinesEntity) value).getId() ;
            key = (id != null) ? String.valueOf(id) : null;
        }
        return key;
    }
}
