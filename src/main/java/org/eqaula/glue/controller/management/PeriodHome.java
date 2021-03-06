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
import org.eqaula.glue.model.management.Period;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.MeasureService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */

@Named
@ViewScoped
public class PeriodHome extends BussinesEntityHome<Period> implements Serializable{
    private static final long serialVersionUID = -983206829229967385L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(PeriodHome.class);
    
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private Measure measure;
    private Long measureId;
    @Inject
    private MeasureService measureService;

    public PeriodHome() {
    }
    
    public Long getPeriodId(){
        return (Long)getId();
    }
    
    public void setPeriodId(Long periodId){
        setId(periodId);
    }
    
    public String getStructureName() {
        return getInstance().getName();
    }

    public Measure getMeasure() {
         if (measure == null) {
            if (measureId == null) {
                measure = null;
            } else {
                measure = measureService.find(getMeasureId());
            }
        }
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public Long getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Long measureId) {
        this.measureId = measureId;
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
        measureService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }
    
    @Override
    protected Period createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Period.class.getName());
        Date now = Calendar.getInstance().getTime();
        Period period = new Period();
        period.setCode(UUID.randomUUID().toString());
        period.setCreatedOn(now);
        period.setLastUpdate(now);
        period.setActivationTime(now);
        period.setExpirationTime(Dates.addDays(now, 364));
        period.setType(_type);
        period.buildAttributes(bussinesEntityService);
        return period;
    }

    @TransactionAttribute
    public String savePeriod() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            //getInstance().setMeasure(getMeasure());
            create(getInstance());
        }
        if (getMeasure() != null) {
            return getOutcome() + "?balancedScorecardId=" + getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Period getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Period> getEntityClass() {
        return Period.class;
    }

    @Transactional
    public String deletePeriod() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Period is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un periodo para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getMeasure() != null) {
            return getOutcome() + "?balancedScorecardId=" + getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

   
    
}
