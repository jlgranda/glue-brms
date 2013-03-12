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
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.jboss.solder.logging.Logger;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */


@Named
@ViewScoped
public class PerspectiveHome extends BussinesEntityHome<Perspective> implements Serializable {

    private static Logger log = Logger.getLogger(Perspective.class);
    private static final long serialVersionUID = 1140758227557893365L;
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityService bussinesEntityService;
    @Current
    @Inject
    private Profile profile;

    // agregar los atributos de theme service
    public PerspectiveHome() {
    }

    public Long getPerspectiveId() {
        return (Long) getId();
    }

    public void setPerspectiveId(Long perspectiveId) {
        setId(perspectiveId);
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
    protected Perspective createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Perspective.class.getName());
        Date now = Calendar.getInstance().getTime();
        Perspective perspective = new Perspective();
        perspective.setCode(UUID.randomUUID().toString());
        perspective.setCreatedOn(now);
        perspective.setLastUpdate(now);
        perspective.setActivationTime(now);
        perspective.setExpirationTime(Dates.addDays(now, 364));
        perspective.setType(_type);
        perspective.buildAttributes(bussinesEntityService);
        return perspective;
    }

    @TransactionAttribute
    public String savePerspective() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            if (this.profile != null && this.profile.isPersistent()) {
                getInstance().setAuthor(this.profile);
            }
            //fijar theme
            create(getInstance());
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Perspective getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Perspective> getEntityClass() {
        return Perspective.class;
    }
    
    @Transactional
    public String deletePerspective() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Perspective is null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una perspectiva para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }
}
