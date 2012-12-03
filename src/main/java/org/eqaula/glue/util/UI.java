/**
 * This file is part of Glue: Adhesive BRMS
 * 
* Copyright (c)2012 José Luis Granda <jlgranda@eqaula.org> (Eqaula Tecnologías
 * Cia Ltda) Copyright (c)2012 Eqaula Tecnologías Cia Ltda (http://eqaula.org)
 * 
* If you are developing and distributing open source applications under the GNU
 * General Public License (GPL), then you are free to re-distribute Glue under
 * the terms of the GPL, as follows:
 * 
* GLue is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
* Glue is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
* You should have received a copy of the GNU General Public License along with
 * Glue. If not, see <http://www.gnu.org/licenses/>.
 * 
* For individuals or entities who wish to use Glue privately, or internally,
 * the following terms do not apply:
 * 
* For OEMs, ISVs, and VARs who wish to distribute Glue with their products, or
 * host their product online, Eqaula provides flexible OEM commercial licenses.
 * 
* Optionally, Customers may choose a Commercial License. For additional
 * details, contact an Eqaula representative (sales@eqaula.org)
 */
package org.eqaula.glue.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityAttribute;
import org.eqaula.glue.model.Group;
import org.eqaula.glue.model.Property;

import org.eqaula.glue.model.accounting.Account;

@Named("ui")
@RequestScoped
public class UI {

    @Inject
    @Web
    private EntityManager em;

    public List<Property> getProperties(BussinesEntity entity) {
        if (entity == null) {
            return new ArrayList<Property>();
        }
        
         if (entity.getType() == null) {
            return new ArrayList<Property>();
        }
        
        Query q = em.createNamedQuery("Property.findByBussinesEntityTypeName");
        q.setParameter("bussinesEntityTypeName", entity.getType().getName());
        return q.getResultList();

    }

    public List<BussinesEntityAttribute> getAttributes(BussinesEntity entity, String names) {
        List<String> types = Lists.stringToList(names);
        Query q = em.createNamedQuery("BussinesEntityAttribute.findByBussinesEntityIdAndTypes");
        q.setParameter("bussinesEntityName", entity.getType().getName());
        q.setParameter("types", types);
        return q.getResultList();

    }

    public Group getGroup(BussinesEntity entity, Property p) {
        Query q = em.createNamedQuery("Group.findByBussinesEntityIdAndPropertyId");
        q.setParameter("bussinesEntityId", entity.getId());
        q.setParameter("propertyId", p.getId());
        return (Group) q.getResultList().get(0);

    }

    public Account.Type[] getAccountTypes() {
        return Account.Type.values();
    }

    public List<SelectItem> getAccountTypesAsSelectItem() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        SelectItem item = null;
        item = new SelectItem(null, getMessages("all"));
        items.add(item);
        for (Account.Type t : Account.Type.values()) {
            item = new SelectItem(t, getMessages(t.name()));
            items.add(item);
        }

        return items;
    }

    public List<SelectItem> getValuesAsSelectItem(List<Object> values) {
        List<SelectItem> items = new ArrayList<SelectItem>();
        SelectItem item = null;
        item = new SelectItem(null, getMessages("common.choice"));
        items.add(item);
        for (Object o : values) {
            item = new SelectItem(o, o.toString());
            items.add(item);
        }

        return items;
    }

    public static String getMessages(String key) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Locale myLocale = fc.getExternalContext().getRequestLocale();
        ResourceBundle myResources = ResourceBundle.getBundle("org.eqaula.messages", myLocale);

        return myResources.containsKey(key) ? myResources.getString(key) : key;
    }
}
