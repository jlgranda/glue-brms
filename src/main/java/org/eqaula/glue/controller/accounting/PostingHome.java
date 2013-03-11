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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.apache.http.impl.cookie.DateUtils;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.accounting.Entry;
import org.eqaula.glue.model.accounting.Ledger;
import org.eqaula.glue.model.accounting.Posting;
import org.eqaula.glue.service.LedgerService;
import org.eqaula.glue.service.PostingService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author Luis Flores
 */
@Named
@ViewScoped
public class PostingHome extends BussinesEntityHome<Posting> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(PostingHome.class);
    private static final long serialVersionUID = -4464974576034303670L;
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private PostingService postingService;
    private Posting postingSelected;
    private Ledger ledger;
    private Long ledgerId;
    
    
    @Inject
    private LedgerService ledgerService;

    @Override
    public Class<Posting> getEntityClass() {
        return Posting.class;
    }

    public Long getPostingId() {
        return (Long) getId();
    }

    public void setPostingId(Long accountId) {
        setId(accountId);
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    
    
    @Transactional
    public Ledger getLedger() {
        if (ledger == null) {
            if (getLedgerId() == null){
            //TODO check for a better code format
            Date now = Calendar.getInstance().getTime();
            String code = DateUtils.formatDate(now, "dd.MM.yyyy");
            ledger = ledgerService.retrivePosting(code);}
            else {
                ledger = ledgerService.find(getLedgerId());
            }
        }
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        postingService.setEntityManager(em);
        ledgerService.setEntityManager(em);
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Posting createInstance() {
        Date now = Calendar.getInstance().getTime();
        Posting posting = new Posting();
        posting.setCreatedOn(now);
        posting.setLastUpdate(now);
        posting.setActivationTime(now);
        posting.setExpirationTime(Dates.addDays(now, 364));
        posting.setPaymentDate(now);
        posting.setMemo("PR. ");

        //initialize default entries
        Entry entry = null;
        for (int i = 0; i < 3; i++) {
            entry = new Entry();
            entry.setCode("Entry " + i); //TODO agregar generador
            //TODO init Entry
            posting.addEntry(entry);
        }
        return posting;
    }

    public Entry addEntry() {
        Entry entry = new Entry();
        entry.setCode("Entry" + getInstance().getEntries().size());
        getInstance().addEntry(entry);
        return entry;
    }

    @TransactionAttribute
    public String savePosting() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        getInstance().setPostingDate(now);
        getInstance().setLedger(getLedger());
        String outcome = null;
        for (Entry e : getInstance().getEntries()) {
            e.setLastUpdate(now);
            e.setAmount(e.getCredit() != null ? e.getCredit() : (e.getDebit() != null ? e.getDebit() : new BigDecimal(0)));
        }
        if (getInstance().isPersistent()) {
            save(getInstance());
            outcome = "/pages/accounting/ledger/ledger.xhtml?faces-redirect=true";
        } else {
            save(getInstance());
            outcome = "/pages/accounting/ledger/ledger.xhtml?faces-redirect=true";
        }

        return outcome;
    }

    public String deletePosting() {
        String outcome = null;
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Posting is null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un Asiento Contable para ser borrado!", ""));
            }

        } catch (Exception e) {
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

    public BigDecimal totalDebit(Long id) {
        BigDecimal total = new BigDecimal(0);
        try {
            Posting p = postingService.find(id);
            for (Entry e : p.getEntries()) {
                if (e.getDebit() != null) {

                    total = total.add(e.getDebit());
                }
            }
        } catch (Exception e) {
            log.info("se ejecutó una excepción");
            log.info("se ejecutó una excepción");
        }
        return total;
    }

    public BigDecimal totalCredit(Long id) {
        BigDecimal total = new BigDecimal(0);
        try {
            Posting p = postingService.find(id);
            for (Entry e : p.getEntries()) {
                if (e.getCredit() != null) {
                    total = total.add(e.getCredit());
                }
            }
        } catch (Exception e) {
        }
        return total;
    }

    public BigDecimal total(Long id, String amount) {
        BigDecimal total = new BigDecimal(0);
        try {
            Posting p = postingService.find(id);
            for (Entry e : p.getEntries()) {
                if (amount.equals("credit")) {
                    if (e.getCredit() != null) {
                        total = total.add(e.getCredit());
                    }
                }
                if (amount.equals("debit")) {
                    if (e.getDebit() != null) {
                        total = total.add(e.getDebit());
                    }
                }
            }
        } catch (Exception e) {
        }
        return total;
    }
}
