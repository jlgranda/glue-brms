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
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.service.OwnerService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.jboss.solder.logging.Logger;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */

@Named
@ViewScoped
public class ObjetiveHome extends BussinesEntityHome<Objetive> implements Serializable{
    
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
    
    private Owner owner;
    private Long ownerId;
    @Inject
    private OwnerService ownerService;

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
    public Owner getOwner() {
        if(owner==null){
            if(ownerId==null){
                owner=null;
            }else{
                owner = ownerService.find(getOwnerId());
            }
        }
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
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
            getInstance().setOwner(getOwner());
            create(getInstance());
        }
        return "/pages/management/organization/view?organizationId="+getOwner().getOrganization().getId();
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
        return "/pages/management/organization/list.xhtml?faces-redirect=true";
    }

   
    
    
    
    
}