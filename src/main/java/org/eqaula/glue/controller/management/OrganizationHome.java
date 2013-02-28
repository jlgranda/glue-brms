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
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/**
 *
 * @author cesar
 */
@Named
@ViewScoped
public class OrganizationHome extends BussinesEntityHome<Organization> implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OrganizationHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityService bussinesEntityService;
    
    @Current
    @Inject
    private Profile profile;

    public OrganizationHome() {
    }

    public Long getOrganizationId() {
        return (Long) getId();
    }

    public void setOrganizationId(Long organizationId) {
        setId(organizationId);
    }
    
    public String getStructureName(){
        return getInstance().getName();
    }
    
    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Organization createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Organization.class.getName());
        Date now = Calendar.getInstance().getTime();
        Organization organization = new Organization();        
        organization.setCreatedOn(now);
        organization.setLastUpdate(now);
        organization.setActivationTime(now);
        organization.setExpirationTime(Dates.addDays(now, 364)); 
        organization.setType(_type);
        organization.buildAttributes(bussinesEntityService);
        return organization;
    }

    @TransactionAttribute
    public String saveOrganization() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()){
            save(getInstance());
        } else {                        
            getInstance().setAuthor(this.profile);
            save(getInstance());
        }
        return "/pages/management/organization/list";
    }
    
    public boolean isWired() {
        return true;
    }

    public Organization getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Organization> getEntityClass() {
        return Organization.class;
    }
    
     @Transactional
    public String deleteOrganization() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Organization is null");
            } 
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente
                
            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una organización para ser borrada!", ""));
            }

        } catch (Exception e) {
            //System.out.println("deleteBussinessEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        return "/pages/management/organization/list.xhtml?faces-redirect=true";
    }
        
}
