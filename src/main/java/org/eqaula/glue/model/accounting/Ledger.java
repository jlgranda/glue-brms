/**
* This file is part of Glue: Adhesive BRMS
*
* Copyright (c)2012 José Luis Granda <jlgranda@eqaula.org> (Eqaula Tecnologías Cia Ltda)
* Copyright (c)2012 Eqaula Tecnologías Cia Ltda (http://eqaula.org)
*
* If you are developing and distributing open source applications under
* the GNU General Public License (GPL), then you are free to re-distribute Glue
* under the terms of the GPL, as follows:
*
* GLue is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Glue is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Glue. If not, see <http://www.gnu.org/licenses/>.
*
* For individuals or entities who wish to use Glue privately, or
* internally, the following terms do not apply:
*
* For OEMs, ISVs, and VARs who wish to distribute Glue with their
* products, or host their product online, Eqaula provides flexible
* OEM commercial licenses.
*
* Optionally, Customers may choose a Commercial License. For additional
* details, contact an Eqaula representative (sales@eqaula.org)
*/
package org.eqaula.glue.model.accounting;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.PersistentObject;

/**
 * Represents a set of accounts and their transactions.
 *
 * @author Travers
 * @adapted José Luis Granda for jpa support
 */
@Entity
@Table(name = "Ledger")
@DiscriminatorValue(value = "LE")
@PrimaryKeyJoinColumn(name = "id")
public class Ledger extends BussinesEntity {

    /**
     *
     */
    private static final long serialVersionUID = 502210551782524052L;
    @javax.persistence.OneToMany(cascade = CascadeType.ALL)
    @javax.persistence.MapKey(name = "name")
    private Map<String, Account> accounts;
    @OneToMany(mappedBy = "ledger", cascade = {CascadeType.REMOVE,
        CascadeType.REFRESH})
    private List<Posting> postings;

    public Ledger() {
        accounts = new HashMap<String, Account>();
        postings = new ArrayList<Posting>();
    }

    @Transient
    public Account newAccount(String name, Account.Type type) {
        // Split name at ':'
        String names[] = name.split(":");

        // Build tree of accounts
        Account lastAccount = null;
        for (int i = 0; i < names.length; i++) {
            String accountName = names[i];

            // Fetch the existing account with names[i]
            Account a = accounts.get(accountName);

            // If account doesn't exist then create it with <names[i], type>
            if (a == null) {
                a = new Account(accountName, type);
            }

            // Add this account to the ledger or the last account found
            if (i == 0) {
                accounts.put(accountName, a);
            } else {
                // Add new account to the previous account
                lastAccount.addSubAccount(a);
            }

            lastAccount = a;
        }

        return lastAccount;
    }

    @Transient
    public Account getAccount(String name) {
        String[] names = name.split(":");

        Account acct = null;
        for (int i = 0; i < names.length; i++) {
            String accountName = names[i];

            if (acct == null) {
                acct = accounts.get(accountName);
            } else {
                acct = acct.getSubAccount(accountName);
            }
        }

        return acct;
    }

    public List<Posting> getPostings() {
        return postings;
    }

    Posting newPosting(Date date, String memo) {
        Posting p = new Posting(this);
        p.setPostingDate(date);
        p.setMemo(memo);
        return p;
    }
}
