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

import org.eqaula.glue.service.WarehouseService;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.controller.accounting.PostingHome;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.model.accounting.Posting;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.model.stocklist.Item;
import org.eqaula.glue.model.stocklist.Warehouse;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author lucho
 */
@Named(value = "warehouseHome")
@ViewScoped
public class WarehouseHome extends BussinesEntityHome<Warehouse> implements Serializable {

    /**
     * Creates a new instance of WarehouseHome
     */
    public WarehouseHome() {
    }
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(WarehouseHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private WarehouseService warehouseService;
    private Warehouse warehouseSelected;
    private String backview;
    private Long parentId;
    @Current
    @Inject
    private Profile profile;

    public Long getWarehouseId() {
        return (Long) getId();
    }

    public void setWarehouseId(Long warehouseId) {
        setId(warehouseId);
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
        warehouseService.setEntityManager(em);
        organizationService.setEntityManager(em);

    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Warehouse createInstance() {
        log.info("eqaula --> WarehouseHome create instance");
        Date now = Calendar.getInstance().getTime();
        Warehouse warehouse = new Warehouse();
        warehouse.setCreatedOn(now);
        warehouse.setLastUpdate(now);
        warehouse.setActivationTime(now);
        warehouse.setExpirationTime(Dates.addDays(now, 364));
        return warehouse;
    }

    @TransactionAttribute
    public String saveWarehouse() {
        log.info("eqaula --> WarehouseHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {
            getInstance().setAuthor(this.profile);
            getInstance().setOrganization(getOrganization());
            save(getInstance());
            outcome = "/pages/stocklist/warehouse/list";
        } else {
            save(getInstance());
            outcome = "/pages/stocklist/warehouse/list";
        }
        return outcome;
    }

    @Transactional
    public String deleteWarehouse() {
        log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());


        try {
            if (getInstance() == null) {
                throw new NullPointerException("Warehouse is null");
            }
            if (getInstance().isPersistent()) {
                //  outcome = hasParent() ? "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getInstance().getParent().getId() : "/pages/accounting/list.xhtml";
                // getInstance().setParent(null);
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                //RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe entidad para ser borrada!", ""));
            }

        } catch (Exception e) {
            //System.out.println("deleteBussinessEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.toString()));
        }
        return "/pages/stocklist/warehouse/list";
    }

    public boolean isWired() {
        return true;
    }

    public Warehouse getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Warehouse> getEntityClass() {
        return Warehouse.class;
    }

    public Warehouse getWarehouseSelected() {
        return warehouseSelected;
    }

    public void setWarehouseSelected(Warehouse warehouseSelected) {
        this.warehouseSelected = warehouseSelected;
    }

    public boolean isAssociatedToWarehouse() {
        if (getInstance().getStocks().size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
