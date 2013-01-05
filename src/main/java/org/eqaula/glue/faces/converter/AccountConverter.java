/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eqaula.glue.faces.converter;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eqaula.glue.accounting.AccountService;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.profile.Profile;

/**
 *
 * @author jlgranda
 */
@RequestScoped
@FacesConverter("org.eqaula.glue.faces.converter.AccountConverter")
public class AccountConverter implements Converter, Serializable {

    private static final long serialVersionUID = -3057944404700510467L;
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private AccountService as;

    @PostConstruct
    public void setup() {

        System.out.println("UserConverter started up");

    }

    @PreDestroy
    public void shutdown() {

        System.out.println("UserConverter shutting down");

    }

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {


        if (value != null && !value.isEmpty() && as != null) {


            try {
                as.setEntityManager(em);
                return as.getAccountById(getKey(value));
            } catch (NoResultException e) {
                return new Profile();
            }

        }

        return null;
    }

    private Long getKey(String value) {
        //get id value from string
        System.out.println("eqaula--> Account Converter "+value);
        int start = value.indexOf("id=");
        int end = value.indexOf(",") == -1 ? value.indexOf("]") : value.indexOf(",");
        return Long.valueOf(value.substring(start + 3, end).trim());
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object value) {
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }        
    }
}
