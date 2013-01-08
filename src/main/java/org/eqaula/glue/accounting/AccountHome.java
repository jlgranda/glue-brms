/*
 * Copyright 2013 cesar.
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
package org.eqaula.glue.accounting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.util.Dates;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 *
 * @author cesar
 */
@Named
@ViewScoped
public class AccountHome extends BussinesEntityHome<Account> implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(AccountHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private AccountService accountService;
    private Long parentId;
    private Account accountSelected;
    private String backview;

    public Long getAccountId() {
        return (Long) getId();
    }

    public void setAccountId(Long accountId) {
        setId(accountId);
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
        log.info("eqaula --> Loaded instance " + getInstance());
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        accountService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Account createInstance() {
        log.info("eqaula --> AccountHome create instance");
        Date now = Calendar.getInstance().getTime();
        Account account = new Account();
        account.setCreatedOn(now);
        account.setLastUpdate(now);
        account.setActivationTime(now);
        account.setExpirationTime(Dates.addDays(now, 364));
        //account.setAuthor(accountSecurity.getLoggedIn());
        //account.buildAttributes(bussinesEntityService); //Sólo si se definen tipo personalizados para este tipo de objeto
        return account;
    }

    @TransactionAttribute
    public String saveAccount() {
        log.info("eqaula --> AccountHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {
            save(getInstance());
            outcome = "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getAccountId();
        } else {
            if (getParentId() == null) { //Cuenta raíz
                create(getInstance());
                outcome = "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getAccountId();
            } else {
                getInstance().setParent(findParent(getParentId()));
                create(getInstance()); //
                outcome = "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getParentId();
            }
        }
        return outcome;
    }

    public String deleteAccount() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Account is null");
            }
            if (getInstance().isPersistent()) {
                log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una Cuenta para ser borrada!", ""));
            }

        } catch (Exception e) {
            //System.out.println("deleteBussinessEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        return hasParent() ? "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getParentId() : "/pages/accounting/list.xhtml";
    }

    public boolean isWired() {
        return true;
    }

    public Account getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Account> getEntityClass() {
        return Account.class;
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage("SubAccount Selected ", ((Account) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("SubAccount Unselected ", ((Account) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setBussinesEntity(null);
    }

    public List<Account.Type> getAccountTypes() {
        wire();
        List<Account.Type> list = Arrays.asList(getInstance().getAccountType().values());
        log.info("eqaula --> AccountHome Account Type: " + list.toString());
        return list;
    }

    public boolean hasParent() {
        return getInstance().getParent() != null;
    }

    private Account findParent(Long id) {
        if (id != null) {
            return accountService.getAccountById(id);
        } else {
            return null;
        }
    }

    public Account getAccountSelected() {
        return accountSelected;
    }

    public void setAccountSelected(Account accountSelected) {
        this.accountSelected = accountSelected;
    }

    public String previousView() {
        if ("account".equals(this.getBackView())) {
            return "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getParentId();

        } else {
            return "/pages/accounting/list.xhtml";
        }

    }
}
