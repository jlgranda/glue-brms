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
import org.eqaula.glue.model.management.Initiative;
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.TargetService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */

@Named
@ViewScoped
public class InitiativeHome extends BussinesEntityHome<Initiative> implements Serializable{
    private static final long serialVersionUID = 6026335350401782707L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(InitiativeHome.class);
    
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    
    private Target target;
    private Long targetId;
    @Inject
    private TargetService targetService;

    public InitiativeHome() {
    }

    public Long getInitiativeId(){
        return (Long) getId();
    }
    
    public void setInitiativeId(Long initiativeId){
        setId(initiativeId);
    }
    
    public String getStructureName(){
        return getInstance().getName();
    }

    public Target getTarget() {
        if(target==null){
            if(targetId == null){
                target=null;
            }else{
             target = targetService.find(getTargetId());
            }
        }
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
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
        targetService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }
    
    @Override
    protected Initiative createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Initiative.class.getName());
        Date now = Calendar.getInstance().getTime();
        Initiative initiative = new Initiative();
        initiative.setCode(UUID.randomUUID().toString());
        initiative.setCreatedOn(now);
        initiative.setLastUpdate(now);
        initiative.setActivationTime(now);
        initiative.setExpirationTime(Dates.addDays(now, 364));
        initiative.setType(_type);
        initiative.buildAttributes(bussinesEntityService);
        return initiative;
    }

    @TransactionAttribute
    public String saveInitiative() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setTarget(getTarget());
            create(getInstance());
        }
        if (getTarget()!= null) {
            return getOutcome() + "?balancedScorecardId=" + getTarget().getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Initiative getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Initiative> getEntityClass() {
        return Initiative.class;
    }

    @Transactional
    public String deleteInitiative() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Initiative is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una iniciativa para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getTarget()!= null) {
            return getOutcome() + "?balancedScorecardId=" + getTarget().getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }    

   
}
