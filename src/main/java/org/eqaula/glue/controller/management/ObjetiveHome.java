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
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.service.OwnerService;
import org.eqaula.glue.service.PerspectiveService;
import org.eqaula.glue.service.ThemeService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.jboss.solder.logging.Logger;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class ObjetiveHome extends BussinesEntityHome<Objetive> implements Serializable {

    private static final long serialVersionUID = -4224903764118210792L;
    private static Logger log = Logger.getLogger(ObjetiveHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityService bussinesEntityService;
    @Current
    @Inject
    private Profile profile;
    private Theme theme;
    private Long themeId;
    @Inject
    private ThemeService themeService;
    private String outcomeAnother="";

    public ObjetiveHome() {
    }

    public Long getObjetiveId() {
        return (Long) getId();
    }

    public void setObjetiveId(Long objetiveId) {
        setId(objetiveId);
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
                theme = themeService.find(getThemeId());
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

    public String getOutcomeAnother() {
        return outcomeAnother;
    }

    public void setOutcomeAnother(String outcomeAnother) {
        this.outcomeAnother = outcomeAnother;
    }

    @Override
    public Organization getOrganization(){
        if (getOrganizationId() == null && isManaged()){
            //TODO prevenir null
            super.setOrganization(getInstance().getTheme().getPerspective().getOrganization());
        }
        return super.getOrganization();
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
        themeService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Objetive createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Objetive.class.getName());
        Date now = Calendar.getInstance().getTime();
        Objetive objetive = new Objetive();
        objetive.setCode(UUID.randomUUID().toString());
        objetive.setCreatedOn(now);
        objetive.setLastUpdate(now);
        objetive.setActivationTime(now);
        objetive.setExpirationTime(Dates.addDays(now, 364));
        objetive.setType(_type);
        objetive.buildAttributes(bussinesEntityService);
        return objetive;
    }

    @TransactionAttribute
    public String saveObjetive() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setTheme(getTheme());
            create(getInstance());
        }
        //TODO idear una mejor forma de redireccionar
        if (getInstance().getTheme().getId() != null) {
            if (getOutcomeAnother().isEmpty()) {
                return getOutcome() + "?balancedScorecardId=" + getInstance().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
            }else{
                return getOutcomeAnother() + "?faces-redirect=true&includeViewParams=true";
            }
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    @TransactionAttribute
    public String saveAndAddOther() {
       setOutcomeAnother("/pages/management/objetive/objetive");       
       return saveObjetive();
    }

    public boolean isWired() {
        return true;
    }

    public Objetive getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Objetive> getEntityClass() {
        return Objetive.class;
    }

    @Transactional
    public String deleteObjetive() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Objetive is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un objetivo para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        //TODO idear una mejor forma de redireccionar
        if (getInstance().getTheme().getId() != null) {
            return getOutcome() + "?balancedScorecardId=" + getInstance().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    @Override
    public String getCanonicalPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
