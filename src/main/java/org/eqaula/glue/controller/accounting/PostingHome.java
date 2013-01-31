/*
 * Copyright 2013 Hp.
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

import org.eqaula.glue.service.PostingService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.accounting.AccountService;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.model.accounting.Entry;
import org.eqaula.glue.model.accounting.Ledger;
import org.eqaula.glue.model.accounting.Posting;
import org.eqaula.glue.profile.ProfileService;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;

/**
 *
 * @author Luis Flores
 */
@Named
@ViewScoped
public class PostingHome extends BussinesEntityHome<Posting> implements Serializable {
    
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(PostingHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private PostingService postingService;
    @Inject
    private AccountService accountService;
    @Inject
    private ProfileService profileService;
    private Long parentId;
    private Posting postingSelected;
    private String backview;
    
    public Long getPostingId() {
        return (Long) getId();
    }
    
    public void setPostingId(Long accountId) {
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
        postingService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
        accountService.setEntityManager(em);
        profileService.setEntityManager(em);
    }
    
    @TransactionAttribute
    public void wire() {
        getInstance();
    }
    
    @Override
    protected Posting createInstance() {
        log.info("eqaula --> PostingHome create instance");
        Date now = Calendar.getInstance().getTime();
        Posting posting = new Posting();
        posting.setCreatedOn(now);
        posting.setLastUpdate(now);
        posting.setActivationTime(now);
        posting.setExpirationTime(Dates.addDays(now, 364));
        
//        Ledger ledger = new Ledger();
//        ledger.setName("Libro diario");
//        ledger.setDescription("esta es una descripción de prueba para ledger");
//        ledger.setCreatedOn(now);
//        ledger.setLastUpdate(now);        
//        posting.setLedger(ledger);
        //AGregar entries manualmente

       
        
        Account a = accountService.getAccountById(Long.parseLong("757"));
        posting.addEntry(a, 40);
        //account.setAuthor(accountSecurity.getLoggedIn());
        //account.buildAttributes(bussinesEntityService); //Sólo si se definen tipo personalizados para este tipo de objeto
        return posting;
    }
    
    public String addEntry(ActionEvent e) {
        getInstance().addEntry(new Entry());
        return null;
    }
    
    @TransactionAttribute
    public String savePosting() {
        log.info("eqaula --> PostingHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {
            save(getInstance());
            outcome = "/pages/home.xhtml";
        } else {
            create(getInstance()); //
            outcome = "/pages/home.xhtml";
        }
        
        return outcome;
    }
    
    public String deletePosting() {
        log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());
        String outcome = null;
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Posting is null");
            }
            if (getInstance().isPersistent()) {
                //  outcome = hasParent() ? "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getInstance().getParent().getId() : "/pages/accounting/list.xhtml";
                // getInstance().setParent(null);
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                //RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un Asiento Contable para ser borrado!", ""));
            }
            
        } catch (Exception e) {
            //System.out.println("deleteBussinessEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.toString()));
        }
        return outcome;
    }
    
    public boolean isWired() {
        return true;
    }
    
    public Posting getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }
    
    @Override
    public Class<Posting> getEntityClass() {
        return Posting.class;
    }

//     public boolean hasParent() {
//        return getInstance().getParent() != null;
//    }
    private Posting findParent(Long id) {
        if (id != null) {
            return postingService.getPostingById(id);
        } else {
            return null;
        }
    }
    
    public Posting getPostingSelected() {
        return postingSelected;
    }
    
    public void setPostingSelected(Posting postingSelected) {
        this.postingSelected = postingSelected;
    }
    
    public List<Posting.Type> getPostingTypes() {
        wire();
        List<Posting.Type> list = Arrays.asList(getInstance().getPostingType().values());
        log.info("eqaula --> PostingHome Posting Type: " + list.toString());
        return list;
    }
    
    public SelectItem[] getAccounts() {
        return UI.getSelectItems(accountService.getAccounts(), false);
    }
    
    public SelectItem[] getProfiles() {
        return UI.getSelectItems(profileService.getProfiles(), false);
    }
//    public List<Account> getAccounts() {
//
//        List list = accountService.getAccounts();
//        for (Account a : list) {
//        log.info("Cuenta: " + a.getName());
//            
//        }
//        
//        
//        return list;
////        try {
////            return new ArrayList<Account>(accountService.getAccounts());
////        } catch (NullPointerException ex) {
////            //java.util.logging.Logger.getLogger(PostingHome.class.getName()).log(Level.SEVERE, null, ex);
////        }
////        return new ArrayList<Account>();
//
//
//    }
}
