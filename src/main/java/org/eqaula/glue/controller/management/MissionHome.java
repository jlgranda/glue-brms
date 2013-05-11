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
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class MissionHome extends BussinesEntityHome<Mission> implements Serializable {

    private static final long serialVersionUID = -717711858547812003L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(MissionHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;

    public MissionHome() {
    }

    public Long getMissionId() {
        return (Long) getId();
    }

    public void setMissionId(Long missionId) {
        setId(missionId);
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
    protected Mission createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Mission.class.getName());
        Date now = Calendar.getInstance().getTime();
        Mission mission = new Mission();
        mission.setCode(UUID.randomUUID().toString());
        mission.setCreatedOn(now);
        mission.setLastUpdate(now);
        mission.setActivationTime(now);
        mission.setExpirationTime(Dates.addDays(now, 364));
        mission.setType(_type);        
        mission.setOrganization(getOrganization());
        mission.buildAttributes(bussinesEntityService);
        return mission;
    }

    @TransactionAttribute
    public String saveMission() {

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

    public Mission getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Mission> getEntityClass() {
        return Mission.class;
    }

    @Transactional
    public String deleteMission() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Mission is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una misión para ser borrada!", ""));
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

    public void createNewMission() {
        setId(null);
        setInstance(null);
        wire();

    }

    public void editMission(Long id) {
        setId(id);
        load();
    }
}
