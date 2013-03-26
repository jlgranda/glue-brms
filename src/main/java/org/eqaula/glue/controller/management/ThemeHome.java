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
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BalancedScorecardService;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.service.OwnerService;
import org.eqaula.glue.service.PerspectiveService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class ThemeHome extends BussinesEntityHome<Theme> implements Serializable {

    private static final long serialVersionUID = 6941666069724371093L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OwnerHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityService bussinesEntityService;
    @Current
    @Inject
    private Profile profile;
    private Owner owner;
    private Long ownerId;
    @Inject
    private OwnerService ownerService;
    
    private Perspective perspective;
    private Long perspectiveId;
    @Inject
    private PerspectiveService perspectiveService;

    public ThemeHome() {
    }

    public Long getThemeId() {
        return (Long) getId();
    }

    public void setThemeId(Long themeId) {
        setId(themeId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Transactional
    public Owner getOwner() {
        if (owner == null) {
            if (ownerId == null) {
                owner = null;
            } else {
                owner = ownerService.find(getOwnerId());
            }
        }
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Long getPerspectiveId() {
        return perspectiveId;
    }

    public void setPerspectiveId(Long perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    
    @Transactional
    public Perspective getPerspective() {
        if(perspective==null){
            if(perspectiveId==null){
                perspective=null;
            }else{
                perspective= perspectiveService.find(getPerspectiveId());
            }
        }
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
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
        ownerService.setEntityManager(em);
        perspectiveService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Theme createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Theme.class.getName());
        Date now = Calendar.getInstance().getTime();
        Theme theme = new Theme();
        theme.setCode(UUID.randomUUID().toString());
        theme.setCreatedOn(now);
        theme.setLastUpdate(now);
        theme.setActivationTime(now);
        theme.setExpirationTime(Dates.addDays(now, 364));
        theme.setType(_type);
        theme.buildAttributes(bussinesEntityService);
        return theme;
    }

    @TransactionAttribute
    public String saveTheme() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setPerspective(getPerspective());
            create(getInstance());
        }
        if (getPerspective()!= null) {            
            return getOutcome() + "?balancedScorecardId=" + getInstance().getPerspective().getBalancedScorecard().getId()+ "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true"; 
    }

    public boolean isWired() {
        return true;
    }

    public Theme getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Theme> getEntityClass() {
        return Theme.class;
    }

    @Transactional
    public String deleteTheme() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Theme is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un tema para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getPerspective() != null) {            
            return getOutcome() + "?balancedScorecardId=" + getInstance().getPerspective().getBalancedScorecard().getId()+ "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true"; 
    }
}
