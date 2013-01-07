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
import java.util.AbstractList;
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
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.Property;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
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
        //BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Account.class.getName());
        Date now = Calendar.getInstance().getTime();
        Account account = new Account();
        account.setCreatedOn(now);
        account.setLastUpdate(now);
        account.setActivationTime(now);
        account.setExpirationTime(Dates.addDays(now, 364));
        //account.setType(_type);
        account.buildAttributes(bussinesEntityService);
        return account;
    }

    @TransactionAttribute
    public String saveAccount() {
        log.info("eqaula --> AccountHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String salida = null;
        if (getInstance().isPersistent()) {
            if (getInstance().getParent() != null && parentId!=null) {
                controllerParent();                 
                save(getInstance());                
                salida = "/pages/accounting/account?faces-redirect=true&accountId=" + getParentId();
            } else {
                log.info("eqaula --> AccountHome save new instance: " + getInstance().getId());                
                save(getInstance());
                salida = "/pages/accounting/account?faces-redirect=true&accountId=" + getParentId();
            }
        }
        return salida;
    }

    @TransactionAttribute
    public String saveNewAccount() {
        log.info("eqaula --> AccountHome save new instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String salida = null;
        if (!getInstance().isPersistent()) {        
            controllerParent();            
            save(getInstance());
            salida = "/pages/accounting/account?faces-redirect=true&accountId=" + getInstance().getId();
        }
        return salida;
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
        return "/admin/accounting/list";
    }

    public boolean isWired() {
        return true;
    }

    public List<Account> getSubAccounts() {
        List<Account> list = new ArrayList<Account>();
        if (getAccountId() != null) {
            Account p = accountService.getAccountById(getAccountId());
            if (p != null) {
                log.info("eqaula --> buscar subCuentas  Parent, id= " + p.getId());
                list = accountService.getSubAccounts(p);
            } else {
                log.info("eqaula --> buscar subCuentas, id= " + getAccountId());
            }
        } else {
            log.info("eqaula --> buscar subCuentas, id= " + getAccountId());
        }
        return list;
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

    public List<Account.Type> getAcountTypes() {
        List<Account.Type> list = Arrays.asList(getInstance().getAccountType().values());
        return list;
    }

    public boolean hasParent() {
        return getInstance().getParent() != null;
    }

    public void controllerParent() {
        if (parentId != null ) {
            Account parent = accountService.getAccountById(parentId);
            parent.addSubAccount(getInstance());
            this.getInstance().setParent(parent);
        } else {
            this.getInstance().setParent(null);
        }
    }
    public Long getSubAccountId(){        
        Account subAccount = getInstance().getSubAccount(getInstance().getName());
        return subAccount.getId();
    }
}
