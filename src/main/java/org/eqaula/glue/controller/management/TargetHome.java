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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.service.MeasureService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;


/*
 * @author dianita
 */
@Named
@ViewScoped
public class TargetHome extends BussinesEntityHome<Target> implements Serializable {

    private static final long serialVersionUID = 5699917875695874467L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(TargetHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityService bussinesEntityService;
    @Current
    @Inject
    private Profile profile;
    private Measure measure;
    private Long measureId;
    @Inject
    private MeasureService measureService;
    
    private Method selectedMethod;
    private List<Method> methods;
    

    public TargetHome() {
    }

    public Long getTargetId() {
        return (Long) getId();
    }

    public void setTargetId(Long targetId) {
        setId(targetId);
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

    public Method getSelectedMethod() {
        return selectedMethod;
    }

    public void setSelectedMethod(Method selectedMethod) {
        this.selectedMethod = selectedMethod;
    }

    public List<Method> getMethods() {
        methods = new ArrayList<Method>();
        methods= getInstance().getMethods();
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
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
    protected Target createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Target.class.getName());
        Date now = Calendar.getInstance().getTime();
        Target target = new Target();
        target.setCode(UUID.randomUUID().toString());
        target.setCreatedOn(now);
        target.setLastUpdate(now);
        target.setActivationTime(now);
        target.setExpirationTime(Dates.addDays(now, 364));
        target.setType(_type);
        target.buildAttributes(bussinesEntityService);
        return target;
    }

    @TransactionAttribute
    public String saveTarget() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setMeasure(getMeasure());
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

    public Target getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Target> getEntityClass() {
        return Target.class;
    }

    @Transactional
    public String deleteTarget() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Target is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una meta para ser borrada!", ""));
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

    public List<Target.Type> getTargetTypes() {
        wire();
        List<Target.Type> list = Arrays.asList(getInstance().getTargetType().values());
        return list;
    }

    @Override
    public String getCanonicalPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
