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
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.ObjetiveService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class MeasureHome extends BussinesEntityHome<Measure> implements Serializable{
    private static final long serialVersionUID = -6935266135527132362L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OwnerHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;

    private Objetive objetive;
    private Long objetiveId;
    @Inject
    private ObjetiveService objetiveService;
    
   
    public MeasureHome() {
    }
    
    public Long getMeasureId(){
        return (Long)getId();
    }
    
    public void setMeasureId(Long measureId){
        setId(measureId);
    }
    
    public String getMeasureName(){
        return getInstance().getName();
    }

    public Long getObjetiveId() {
        return objetiveId;
    }

    public void setObjetiveId(Long objetiveId) {
        this.objetiveId = objetiveId;
    }

    @Transactional
    public Objetive getObjetive() {
        if(objetive==null){
            if(objetiveId==null){
                objetive=null;
            }else{
                objetive = objetiveService.find(getObjetiveId());
            }
        }
        return objetive;
    }

    public void setObjetive(Objetive objetive) {
        this.objetive = objetive;
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
        objetiveService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }
    
    @Override
    protected Measure createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Measure.class.getName());
        Date now = Calendar.getInstance().getTime();
        Measure measure = new Measure();
        measure.setCode(UUID.randomUUID().toString());
        measure.setCreatedOn(now);
        measure.setLastUpdate(now);
        measure.setActivationTime(now);
        measure.setExpirationTime(Dates.addDays(now, 364));
        measure.setType(_type);
        measure.setObjetive(getObjetive());
        measure.buildAttributes(bussinesEntityService);
        return measure;
    }

    @TransactionAttribute
    public String saveMeasure() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            
            create(getInstance());
        }
        //TODO idear una mejor forma de redireccionar
        if (getObjetive()!= null){
            return getOutcome() + "?balancedScorecardId=" + getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Measure getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Measure> getEntityClass() {
        return Measure.class;
    }

    @Transactional
    public String deleteMeasure() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Measure is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una medida para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getObjetive()!= null){
            return getOutcome() + "?balancedScorecardId=" + getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

   

}
