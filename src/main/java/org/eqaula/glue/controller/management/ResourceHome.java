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
import org.eqaula.glue.model.management.Resource;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */

@Named
@ViewScoped
public class ResourceHome extends BussinesEntityHome<Resource> implements Serializable{
    
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ResourceHome.class);
    private static final long serialVersionUID = 2729403385742460165L;
    
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;

    public ResourceHome() {
    }
   
    public Long getResourceId() {
        return (Long) getId();
    }

    public void setResourceId(Long resourceId) {
        setId(resourceId);
    }

    public String getStructureName() {
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
    protected Resource createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Resource.class.getName());
        Date now = Calendar.getInstance().getTime();
        Resource resource = new Resource();
        resource.setCode(UUID.randomUUID().toString());
        resource.setCreatedOn(now);
        resource.setLastUpdate(now);
        resource.setActivationTime(now);
        resource.setExpirationTime(Dates.addDays(now, 364));
        resource.setType(_type);
        resource.buildAttributes(bussinesEntityService);
        return resource;
    }

    @TransactionAttribute
    public String saveResource() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            if (this.profile != null && this.profile.isPersistent()) {
                getInstance().setAuthor(this.profile);
            }
            create(getInstance());
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Resource getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Resource> getEntityClass() {
        return Resource.class;
    }
    
    @Transactional
    public String deleteResource() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Resource is null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un recurso para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

}
