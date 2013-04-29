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
package org.eqaula.glue.controller.config;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.config.Frequency;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/*
 * @author dianita
 */
@Named(value = "frequencyHome")
@ViewScoped
public class FrequencyHome extends BussinesEntityHome<Frequency> implements Serializable {

    private static final long serialVersionUID = -1330050683808105824L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(FrequencyHome.class);
    @Inject
    @Web
    private EntityManager em;

    public FrequencyHome() {
    }

    public Long getFrequencyId() {
        return (Long) getId();
    }

    public void setFrequencyId(Long frequencyId) {
        setId(frequencyId);
    }

    @Produces
    @Current
    @TransactionAttribute
    public Frequency load() {
        if (isIdDefined()) {
            wire();
        }
        return getInstance();
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Frequency createInstance() {
        Date now = Calendar.getInstance().getTime();
        Frequency frequency = new Frequency();
        frequency.setCreatedOn(now);
        frequency.setLastUpdate(now);
        frequency.setActivationTime(now);
        frequency.setExpirationTime(Dates.addDays(now, 364));
        return frequency;
    }

    @TransactionAttribute
    public String saveFrequency() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
            return "/pages/admin/frequency/list";
        } else {
            create(getInstance());
            return "/pages/admin/frequency/list";
        }
    }

    @Transactional
    public String deleteFrequency() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Frequency is null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));

            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una entidad para ser borrada!", ""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.toString()));
        }
        return "/pages/admin/frequency/list";
    }

    public boolean isWired() {
        return true;
    }

    public Frequency getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Frequency> getEntityClass() {
        return Frequency.class;
    }
}
