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
package org.eqaula.glue;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.config.Setting;
import org.eqaula.glue.service.SettingService;

/**
 *
 * @author jlgranda
 */
@Named 
@RequestScoped
public class Glue implements Serializable {
    
    @Inject
    @Web
    private EntityManager em;
    
    @Inject
    SettingService settingService;
    
    public String getProperty(String name, final String defaultValue){
        Setting s = settingService.findByName(name);
        return s== null ? defaultValue : s.getValue();
    }
    
    @PostConstruct
    public void init() {
        settingService.setEntityManager(em);
    }
}
