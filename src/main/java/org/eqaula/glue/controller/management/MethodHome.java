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
import org.eqaula.glue.model.management.Method;
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
public class MethodHome extends BussinesEntityHome<Method> implements Serializable {

    private static final long serialVersionUID = 4149724037209263297L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OwnerHome.class);
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

    public MethodHome() {
    }

    public Long getMethodId() {
        return (Long) getId();
    }

    public void setMethodId(Long methodId) {
        setId(methodId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Target getTarget() {
        if (target == null) {
            if (targetId == null) {
                target = null;
            } else {
                target = targetService.find(getTargetId());
            }
        }
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
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
    protected Method createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Method.class.getName());
        Date now = Calendar.getInstance().getTime();
        Method method = new Method();
        method.setCode(UUID.randomUUID().toString());
        method.setCreatedOn(now);
        method.setLastUpdate(now);
        method.setActivationTime(now);
        method.setExpirationTime(Dates.addDays(now, 364));
        method.setType(_type);
        method.setTarget(getTarget());
        method.buildAttributes(bussinesEntityService);
        return method;
    }

    @TransactionAttribute
    public String saveMethod() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
        }
        if (getInstance().getTarget().getId() != null) {
            return getOutcome() + "?targetId=" + getInstance().getTarget().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Method getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Method> getEntityClass() {
        return Method.class;
    }

    @Transactional
    public String deleteMethod() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Method is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un método para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getInstance().getTarget().getId() != null) {
            return getOutcome() + "?targetId=" + getInstance().getTarget().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }
    
      public List<Method.Type> getMethodTypes() {
        wire();
        List<Method.Type> list = Arrays.asList(getInstance().getMethodType().values());
        return list;
    }
}
