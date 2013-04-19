/*
 * Copyright 2013 jlgranda.
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
package org.eqaula.glue.controller.accounting;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.apache.http.impl.cookie.DateUtils;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.accounting.Ledger;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.LedgerService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author jlgranda
 */
@Named
@ViewScoped
public class LedgerHome extends BussinesEntityHome<Ledger> implements Serializable {

    private static final long serialVersionUID = -347442906892035526L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(LedgerHome.class);
    @Inject
    @Web
    private EntityManager em;
    private String uuid;
    @Inject
    private LedgerService ledgerService;

    public Long getLedgerId() {
        return (Long) getId();
    }

    public void setLedgerId(Long ledgerId) {
        setId(ledgerId);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
        organizationService.setEntityManager(em);

    }

    @Produces
    @Named("ledger")
    @Current
    @TransactionAttribute
    public Ledger load() {
        if (isIdDefined()) {
            wire();
        } else if (this.instance == null) {
            Date now = Calendar.getInstance().getTime();
            String code = DateUtils.formatDate(now, "dd.MM.yyyy."+getOrganization().getId());
            setInstance(ledgerService.retrivePosting(code, getOrganization()));
        }
        return getInstance();
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Ledger createInstance() {
        log.info("eqaula --> SettingHome create instance");
        Date now = Calendar.getInstance().getTime();
        Ledger ledger = new Ledger();
        ledger.setCreatedOn(now);
        ledger.setLastUpdate(now);
        ledger.setActivationTime(now);
        ledger.setExpirationTime(Dates.addDays(now, 364));
        return ledger;
    }

    @TransactionAttribute
    public String save() {
        log.info("eqaula --> SettingHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {
            save(getInstance());
            outcome = "/pages/admin/setting/list";
        } else {
            create(getInstance());
            outcome = "/pages/admin/setting/list";
        }
        return outcome;
    }

    @Transactional
    public String delete() {
        log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Setting is null");
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
        return "/pages/admin/setting/list";
    }

    public boolean isWired() {
        return true;
    }

    public Ledger getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Ledger> getEntityClass() {
        return Ledger.class;
    }
    
    @Override
    public String getCanonicalPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
