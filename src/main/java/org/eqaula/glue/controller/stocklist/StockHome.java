/*
 * Copyright 2013 lucho.
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
package org.eqaula.glue.controller.stocklist;

import javax.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.stocklist.Stock;
import org.eqaula.glue.service.StockService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author lucho
 */
@Named(value = "stockHome")
@ViewScoped
public class StockHome extends BussinesEntityHome<Stock> implements Serializable {

    /**
     * Creates a new instance of StockHome
     */
    public StockHome() {
    }
    
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(StockHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private StockService stockService;
    private Stock stockSelected;
    private String backview;
    private Long parentId;

    public Long getStockId() {
        return (Long) getId();
    }

    public void setStockId(Long stockId) {
        setId(stockId);
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
        stockService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);

    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Stock createInstance() {
        log.info("eqaula --> StockHome create instance");
        Date now = Calendar.getInstance().getTime();
        Stock stock = new Stock();
        stock.setCreatedOn(now);
        stock.setLastUpdate(now);
        stock.setActivationTime(now);
        stock.setExpirationTime(Dates.addDays(now, 364));
        return stock;
    }

    @TransactionAttribute
    public String saveStock() {
        log.info("eqaula --> StockHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {
            save(getInstance());
            outcome = "/pages/stocklist/stock/list";
        } else {
            save(getInstance());
            outcome = "/pages/stocklist/stock/list";
        }
        return outcome;
    }

    @Transactional
    public String deleteStock() {
        log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());
        
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Stock is null");
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
        return "/pages/stocklist/stock/list";
    }

    public boolean isWired() {
        return true;
    }

    public Stock getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Stock> getEntityClass() {
        return Stock.class;
    }

    public Stock getStockSelected() {
        return stockSelected;
    }

    public void setStockSelected(Stock stockSelected) {
        this.stockSelected = stockSelected;
    }
    
    
    
    
}
