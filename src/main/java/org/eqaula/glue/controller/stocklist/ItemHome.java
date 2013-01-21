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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.stocklist.Item;
import org.eqaula.glue.model.stocklist.Warehouse;
import org.eqaula.glue.service.ItemService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author lucho
 */
@Named(value = "itemHome")
@ViewScoped
public class ItemHome extends BussinesEntityHome<Item> implements Serializable {

    private static final long serialVersionUID = 4819808125494695197L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ItemHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private ItemService itemService;
    private Item itemSelected;
    private Long parentId;
    private Long warehouseId;
    private Long stockId;

    public ItemHome() {
    }

    public Long getItemId() {
        return (Long) getId();
    }

    public void setItemId(Long itemId) {
        setId(itemId);
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Item getItemSelected() {
        return itemSelected;
    }

    public void setItemSelected(Item itemSelected) {
        this.itemSelected = itemSelected;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
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
        itemService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);

    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Item createInstance() {
        log.info("eqaula --> ItemHome create instance");
        Date now = Calendar.getInstance().getTime();
        Item item = new Item();
        item.setCreatedOn(now);
        item.setLastUpdate(now);
        item.setActivationTime(now);
        item.setExpirationTime(Dates.addDays(now, 364));
        return item;
    }

    @TransactionAttribute
    public String saveItem() {
        log.info("eqaula --> ItemHome save instance: " + getInstance().getId());
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        String outcome = null;
        if (getInstance().isPersistent()) {

            if (warehouseId != null && stockId != null) {
                save(getInstance());
                outcome = "/pages/stocklist/stock/stock?faces-redirect=true&warehouseId=" + getWarehouseId() + "&stockId=" + getStockId();
            } else {

                if (warehouseId != null) {
                    save(getInstance());
                    outcome = "/pages/stocklist/stock/stock?faces-redirect=true&warehouseId=" + getWarehouseId();
                } else {
                    save(getInstance());
                    outcome = "/pages/stocklist/item/list";
                }
            }

        } else {
            if (warehouseId != null && stockId != null) {
                save(getInstance());
                outcome = "/pages/stocklist/stock/stock?faces-redirect=true&warehouseId=" + getWarehouseId() + getWarehouseId() + "&stockId=" + getStockId();
            } else {
                if (warehouseId != null) {
                    save(getInstance());
                    outcome = "/pages/stocklist/stock/stock?faces-redirect=true&warehouseId=" + getWarehouseId();
                } else {
                    save(getInstance());
                    outcome = "/pages/stocklist/item/list";
                }
            }

        }
        return outcome;
    }

    @Transactional
    public String deleteItem() {
        log.info("eqaula --> ingreso a eliminar: " + getInstance().getId());


        try {
            if (getInstance() == null) {
                throw new NullPointerException("Item is null");
            }
            if (getInstance().isPersistent()) {
                //  outcome = hasParent() ? "/pages/accounting/account.xhtml?faces-redirect=true&accountId=" + getInstance().getParent().getId() : "/pages/accounting/list.xhtml";
                // getInstance().setParent(null);
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                //RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un Producto para ser borrado!", ""));
            }

        } catch (Exception e) {
            //System.out.println("deleteBussinessEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.toString()));
        }
        return "/pages/stocklist/item/list";
    }

    public boolean isWired() {
        return true;
    }

    public Item getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Item> getEntityClass() {
        return Item.class;
    }

    public List<Item> getItems() {
        List list = itemService.getItems();
        return list;
    }

    public boolean isAssociatedToItem() {
        if (getInstance().getStocks().size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
