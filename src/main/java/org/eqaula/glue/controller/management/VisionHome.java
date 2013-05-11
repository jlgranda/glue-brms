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
import org.eqaula.glue.model.management.Mission;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Vision;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */

@Named
@ViewScoped
public class VisionHome extends BussinesEntityHome<Vision> implements Serializable{
    
    private static final long serialVersionUID = -5100489251962933468L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(MissionHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;

    public VisionHome() {
    }

    
    public Long getVisionId() {
        return (Long) getId();
    }

    public void setVisionId(Long visionId) {
        setId(visionId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    @Override
    public Organization getOrganization() {

        if (getOrganizationId() == null && isManaged()) {
            super.setOrganization(getInstance().getOrganization());
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
        bussinesEntityService.setEntityManager(em);
        organizationService.setEntityManager(em);
    }

    @Override
    protected Vision createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Vision.class.getName());
        Date now = Calendar.getInstance().getTime();
        Vision vision = new Vision();
        vision.setCode(UUID.randomUUID().toString());
        vision.setCreatedOn(now);
        vision.setLastUpdate(now);
        vision.setActivationTime(now);
        vision.setExpirationTime(Dates.addDays(now, 364));
        vision.setType(_type);        
        vision.setOrganization(getOrganization());
        vision.buildAttributes(bussinesEntityService);
        return vision;
    }

    @TransactionAttribute
    public String saveVision() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setOrganization(getOrganization());
            create(getInstance());
        }
        if (getOutcome() == null) {
            return null;
        }
        if (getOrganization() != null) {
            return getOutcome() + "?balancedScorecardId=" + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Vision getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Vision> getEntityClass() {
        return Vision.class;
    }

    @Transactional
    public String deleteVision() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Vision is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una visión para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        
        if(getOutcome()==null){
            return null;
        }
        if (getOrganization() != null) {
            return getOutcome() + "?balancedScorecardId=" + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public void createNewVision() {
        setId(null);
        setInstance(null);
        wire();

    }

    public void editVision(Long id) {
        setId(id);
        load();
    }
    
}
