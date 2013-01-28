/*
 * Copyright 2013 cesar.
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
package org.eqaula.glue.faces.validator;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.config.SettingHome;
import org.eqaula.glue.model.config.Setting;
import org.eqaula.glue.service.SettingService;
import org.eqaula.glue.util.UI;

/**
 *
 * @author lucho
 */
@RequestScoped
@FacesValidator("wordAvailabilityValidator")
public class WordAvailabilityValidator implements Validator {

    @Inject
    private EntityManager em;
    @Inject
    private SettingService settingService;
    
    @Inject
    private SettingHome settingHome;

    @Override
    public void validate(FacesContext fc, UIComponent uic, Object value)
            throws ValidatorException {
        if (value instanceof String && !value.equals(settingHome.getInstance().getName())) {
            settingService.setEntityManager(em);
            if (!settingService.isWordAvailable((String) value)) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN, UI.getMessages("El nombre indicado para esta propiedad ya está en uso"), null));
            }
        }
    }
}
