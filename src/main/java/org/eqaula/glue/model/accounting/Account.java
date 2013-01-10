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
package org.eqaula.glue.model.accounting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.Property;
import org.hibernate.mapping.Collection;

/**
 *
 * @author Travers
 * @adapted José Luis Granda for jpa support
 */
@Entity
@Table(name = "Account")
@DiscriminatorValue(value = "AC")
@PrimaryKeyJoinColumn(name = "id")
public class Account extends BussinesEntity implements Serializable, Comparable<Account> {

    /**
     *
     */
    private static final long serialVersionUID = -2909291380571371481L;

    public enum Type {

        SCHEMA(0), //Esquema contable
        GENDER(0), //Elemento de estado financiero
        GROUP(0), //Grupo de cuentas
        ACCOUNT(0), //Cuentas
        CHARGE(0), //Efectivo y equivalentes
        ASSET(-1), // Activo
        LIABILITY(1), //Pasivo
        INCOME(1), //Entrada
        EXPENSE(-1); //Gasto
        private int normalBalanceSign;

        private Type(int normalBalanceSign) {
            this.normalBalanceSign = normalBalanceSign;
        }

        /**
         * Returns the usual sign for this type of account. Normally debit
         * accounts (like Assets and Expenses) are negative (-1), normally
         * credit accounts are positive (1).
         *
         * @return -1 or 1
         */
        public int getNormalBalanceSign() {
            return normalBalanceSign;
        }
    }

    protected static class AccountName {

        public String firstName;
        public String restOfName;
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Account.Type accountType;
    @OneToMany(mappedBy = "account", cascade = {CascadeType.REMOVE,
        CascadeType.REFRESH})
    protected List<Entry> entries;
    @ManyToOne
    @JoinColumn(name = "parent")
    public BussinesEntity parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @MapKey(name = "id")
    private Map<String, Account> subAccounts;

    static private AccountName toAccountName(String name) {
        AccountName acctName = new AccountName();
        acctName.restOfName = null;
        acctName.firstName = name;
        int separator = name.indexOf(':');
        if (separator > 0) {
            acctName.firstName = name.substring(0, separator);
            acctName.restOfName = name.substring(separator + 1);
        } else if (separator == 0) {
            acctName.firstName = name.substring(1);
        }
        return acctName;
    }

    public Account() {
        //this("Test", Account.Type.ASSET);
    }

    public Account(String name, Account.Type type) {
        AccountName acctName = toAccountName(name);

        setName(acctName.firstName);
        this.accountType = type;
        this.entries = new ArrayList<Entry>();
        this.subAccounts = new HashMap<String, Account>();

        if (acctName.restOfName != null) {
            Account sub = new Account(acctName.restOfName, type);
            subAccounts.put(sub.getName(), sub);
        }
    }

    public List<Entry> getEntries() {
        List<Entry> allEntries = new ArrayList<Entry>(entries);
        //entries from subaccounts
        for (Account a : subAccounts.values()) {
            allEntries.addAll(a.getEntries());
        }

        return allEntries;
    }

    public Account.Type getAccountType() {
        return accountType;
    }

    public void setAccountType(Account.Type type) {
        this.accountType = type;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    protected void addEntry(Entry e) {
        this.entries.add(e);
    }

    public BussinesEntity getParent() {
        return parent;
    }

    public void setParent(BussinesEntity parent) {
        this.parent = parent;
    }

    public Map<String, Account> getSubAccounts() {
        return subAccounts;
    }

    public void setSubAccounts(Map<String, Account> subAccounts) {
        this.subAccounts = subAccounts;
    }

    public void addSubAccount(Account a) {
        a.setParent(this);
        subAccounts.put(a.getName(), a);
    }

    public void removeSubAccount(Account a) {
        this.subAccounts.remove(a.getName());
    }

    public Account getSubAccount(String accountName) {
        return subAccounts.get(accountName);
    }

    public List<Account> getSubAccountsAsList() {
        List sblist = new ArrayList<Account>(subAccounts.values());
        Collections.sort(sblist);
        return sblist;
    }

    public BigDecimal getTrialBalance() {
        BigDecimal balance = new BigDecimal(0);

        for (Entry e : getEntries()) {
            balance = balance.add(e.getAmount());
        }

        return balance;
    }

    public int getTrialBalanceAsInt() {
        BigDecimal balance = getTrialBalance();
        return balance.intValue();
    }

    public float getTrialBalanceAsFloat() {
        BigDecimal balance = getTrialBalance();
        return balance.floatValue();
    }
    
    @Override
    public String toString() {
        return "org.eqaula.glue.model.Accounting.Account[ "
                + "id=" + this.getId() + ","
                + "name=" + this.getName() + ","
                + "code=" + this.getCode() + ","
                + "description=" + this.getDescription() + ","                
                + " ]";
    }
    //+ "parent=" + (this.getParent() == null ? "null" : this.getParent()) + ","
    
    @Override
    public int compareTo(Account o) {
        return this.getCode().compareTo(o.getCode());
    }
}
