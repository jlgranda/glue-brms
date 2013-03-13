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
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/**
 *
 * @author dianita
 */
@Named
@ViewScoped
public class BalancedScorecardHome extends BussinesEntityHome<BalancedScorecard> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(org.eqaula.glue.controller.management.OwnerHome.class);
    private static final long serialVersionUID = -8602961273211681365L;
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;

    public BalancedScorecardHome() {
    }

    public Long getBalancedScorecardId() {
        return (Long) getId();
    }

    public void setBalancedScorecardId(Long balancedScorecardId) {
        setId(balancedScorecardId);
    }

    public String getBalancedScorecardName() {
        return getInstance().getName();
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
    }

    @Override
    protected BalancedScorecard createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(BalancedScorecard.class.getName());
        Date now = Calendar.getInstance().getTime();
        BalancedScorecard balancedScorecard = new BalancedScorecard();
        balancedScorecard.setCode(UUID.randomUUID().toString());
        balancedScorecard.setCreatedOn(now);
        balancedScorecard.setLastUpdate(now);
        balancedScorecard.setActivationTime(now);
        balancedScorecard.setExpirationTime(Dates.addDays(now, 364));
        balancedScorecard.setType(_type);
        balancedScorecard.buildAttributes(bussinesEntityService);
        return balancedScorecard;
    }

    @TransactionAttribute
    public String saveBalancedScorecard() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setOrganization(getOrganization());
            create(getInstance());
        }
        if (getOrganizationId() != null) {
            return getOutcome() + "?organizationId=" + getOrganizationId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public BalancedScorecard getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<BalancedScorecard> getEntityClass() {
        return BalancedScorecard.class;
    }

    @Transactional
    public String deleteBalancedScorecard() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("BalancedScorecard is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un cuadro de mando integral para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }
}
