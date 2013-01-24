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
package org.eqaula.glue.config;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.config.Setting;
import org.eqaula.glue.service.SettingService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author lucho
 */
@Named(value = "settingHome")
@ViewScoped
public class SettingHome extends BussinesEntityHome<Setting> implements Serializable {

    private static final long serialVersionUID = 4819808125494695197L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(SettingHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private SettingService settingService;
    private Setting settingSelected;
    private Long parentId;

    public SettingHome() {
    }

    public Long getSettingId() {
        return (Long) getId();
    }

    public void setSettingId(Long stockId) {
        setId(stockId);
    }

    public Setting getSettingSelected() {
        return settingSelected;
    }

    public void setSettingSelected(Setting settingSelected) {
        this.settingSelected = settingSelected;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
        log.info("eqaula --> Loaded instance " + getInstance());
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        settingService.setEntityManager(em);

    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Setting createInstance() {
        log.info("eqaula --> SettingHome create instance");
        Date now = Calendar.getInstance().getTime();
        Setting setting = new Setting();
        setting.setCreatedOn(now);
        setting.setLastUpdate(now);
        setting.setActivationTime(now);
        setting.setExpirationTime(Dates.addDays(now, 364));
        return setting;
    }

    @TransactionAttribute
    public String saveSetting() {
        log.info("eqaula --> SettingHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {
            save(getInstance());
            outcome = "/pages/admin/setting/list";
        } else {
            create(getInstance());
            outcome = "/pages/admin/setting/list";
        }
        return outcome;
    }

    @Transactional
    public String deleteSetting() {
        log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Setting is null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));

            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una entidad para ser borrada!", ""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.toString()));
        }
        return "/pages/admin/setting/list";
    }

    public boolean isWired() {
        return true;
    }

    public Setting getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Setting> getEntityClass() {
        return Setting.class;
    }
}
