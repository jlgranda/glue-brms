/*
 * Copyright 2012 cesar.
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
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.OwnerListService;
import org.eqaula.glue.service.OwnerService;
import org.eqaula.glue.service.ThemeService;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */


@Named
@ViewScoped
public class OwnerHome extends BussinesEntityHome<Owner> implements Serializable {

    private static final long serialVersionUID = 1182642574142830175L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OwnerHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    @Inject
    private ThemeService themeService;

    public OwnerHome() {
    }

    public Long getOwnerId() {
        return (Long) getId();
    }

    public void setOwnerId(Long ownerId) {
        setId(ownerId);
    }

    public String getOwnerName() {
        return getInstance().getName();
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
        organizationService.setEntityManager(em);
        themeService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    public Organization getOrganization() {
        if (getOrganizationId() == null && isManaged()) {
            super.setOrganization(getInstance().getOrganization());
        }
        return super.getOrganization();
    }

    @Override
    public Owner createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Owner.class.getName());
        Date now = Calendar.getInstance().getTime();
        Owner owner = new Owner();
        owner.setCode(UUID.randomUUID().toString());
        owner.setCreatedOn(now);
        owner.setLastUpdate(now);
        owner.setActivationTime(now);
        owner.setExpirationTime(Dates.addDays(now, 364));
        owner.setType(_type);
        owner.setOrganization(getOrganization());
        owner.buildAttributes(bussinesEntityService);
        return owner;
    }

    @TransactionAttribute
    public String saveOwner() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {          
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);  
            create(getInstance());
        }
        //TODO idear una mejor forma de redireccionar
        if (getOutcome() == null) {
            return null;
        }
        if (getInstance().getOrganization() != null) {
            return getOutcome() + "?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Owner getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Owner> getEntityClass() {
        return Owner.class;
    }

    @Transactional
    public String deleteOwner() {
        boolean band;
        band = false;
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Owner is Null");
            }
            if (getInstance().isPersistent()) {
                if (hasValuesBussinesEntity()) {
                    delete(getInstance());
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                    RequestContext.getCurrentInstance().execute("editDlg.hide()");
                } else {
                    //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.property.hasValues") + " " + getInstance().getName(), ""));
                    //RequestContext.getCurrentInstance().execute("editDlg.hide()");
                    band = true;
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, getInstance().getName() + " : " + UI.getMessages("module.stocklist.delete.confirm"), "");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un gerente para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        
        if(getOutcome()==null){
            return "/pages/management/owner/list.xhtml?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        if (band) {            
            return null;
        }
        if (getInstance().getOrganization() != null) {
            return getOutcome() + "?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean hasValuesBussinesEntity() {
        boolean ban = themeService.findByOwner(getInstance()).isEmpty();
        return ban;
    }
    
    public void createNewOwner() {
        setId(null);
        setInstance(null);
        load();
    }

    public void editOwner(Long id) {
        setId(id);        
        load();        
    }
     
    @Transactional
    public String saveOwnerDialog() {
        saveOwner();
        //return "/pages/management/owner/list.xhtml?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        return null;
    }
    
}