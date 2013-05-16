/*
 * Copyright 2013 dianita.
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
package org.eqaula.glue.controller.management;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.Macroprocess;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.ThemeService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class MacroprocessHome extends BussinesEntityHome<Macroprocess> implements Serializable {

    private static final long serialVersionUID = -4910890037541510739L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(MacroprocessHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private Theme theme;
    private Long themeId;
    @Inject
    private ThemeService themeServices;

    public MacroprocessHome() {
    }

    public Long getMacroprocessId() {
        return (Long) getId();
    }

    public void setMacroprocessId(Long macroprocessId) {
        setId(macroprocessId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    @Transactional
    public Theme getTheme() {
        if (theme == null) {
            if (themeId == null) {
                theme = null;
            } else {
                theme = themeServices.find(getThemeId());
            }
        }
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        themeServices.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Macroprocess createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Macroprocess.class.getName());
        Date now = Calendar.getInstance().getTime();
        Macroprocess macroprocess = new Macroprocess();
        macroprocess.setCode(UUID.randomUUID().toString());
        macroprocess.setCreatedOn(now);
        macroprocess.setLastUpdate(now);
        macroprocess.setActivationTime(now);
        macroprocess.setExpirationTime(Dates.addDays(now, 364));
        macroprocess.setType(_type);
        macroprocess.buildAttributes(bussinesEntityService);
        return macroprocess;
    }

    @TransactionAttribute
    public String saveMacroprocess() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setTheme(getTheme());
            create(getInstance());
        }
        if(getOutcome()==null){
            return null;
        }
            
        if (getThemeId() != null) {
            return getOutcome() + "?organizationId=" + getInstance().getTheme().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    @TransactionAttribute
    public String saveAnotherMacroprocess() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);

        getInstance().setAuthor(this.profile);
        getInstance().setTheme(getTheme());
        create(getInstance());

        if (getThemeId() != null) {
            log.info("get outcome: " + getOutcome());
            log.info("get outcome: " + getOutcome());
            log.info("get outcome: " + getOutcome());
            log.info("get outcome: " + getOutcome());
            return "/pages/management/macroprocess/macroprocess" +"?themeId=" + getInstance().getTheme().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Macroprocess getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Macroprocess> getEntityClass() {
        return Macroprocess.class;
    }

    @Transactional
    public String deleteMacroprocess() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Macroprocess is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un macroproceso para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if(getOutcome()==null){
            return null;
        }
        if (getThemeId() != null) {
            return getOutcome() + "?organizationId=" + getInstance().getTheme().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public void createNewMacroprocess() {
        setId(null);
        setInstance(null);
        wire();
    }

    public void editMacroprocess(Long id) {
        setId(id);
        load();
    }
    
    @TransactionAttribute
    private void saveMacroprocessDialog(){
        saveMacroprocess();
    }
   
}
