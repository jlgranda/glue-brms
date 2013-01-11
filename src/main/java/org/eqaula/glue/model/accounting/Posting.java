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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.eqaula.glue.model.BussinesEntity;

import org.eqaula.glue.model.PersistentObject;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * A group of related account entries
 *
 * @author Travers
 * @adapted José Luis Granda for jpa support
 */
@Entity
@Table(name = "Posting")
@DiscriminatorValue(value = "PO")
@PrimaryKeyJoinColumn(name = "id")
public class Posting extends BussinesEntity {

    /**
     *
     */
    private static final long serialVersionUID = -4147260077491075807L;
    @NotNull
    @ManyToOne(optional = false)
    private Ledger ledger;
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postingDate;
    @NotEmpty
    @Column(nullable = false)
    private String memo;
    @OneToMany(mappedBy = "posting", cascade = {CascadeType.REMOVE,
        CascadeType.REFRESH})
    private List<Entry> entries;

    public Posting() {
        super();
        entries = new ArrayList<Entry>();
    }

    public Posting(Ledger l) {
        ledger = l;
        entries = new ArrayList<Entry>();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Date getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(Date postingDate) {
        this.postingDate = postingDate;
    }

    public Entry addEntry(Account a, BigDecimal amount) {
        Entry e = new Entry();
        e.setPosting(this);
        e.setAccount(a);
        e.setAmount(amount);
        entries.add(e);
        return e;
    }

    public Entry addEntry(Account a, int amount) {
        Entry e = new Entry();
        e.setPosting(this);
        e.setAccount(a);
        e.setAmount(BigDecimal.valueOf(amount));
        entries.add(e);
        return e;
    }

    public Entry addEntry(Account a, float amount) {
        Entry e = new Entry();
        e.setPosting(this);
        e.setAccount(a);
        e.setAmount(BigDecimal.valueOf(amount));
        entries.add(e);
        return e;
    }
    
    
    public void addEntry(Entry e) {
       
        if (!entries.contains(e)) {
             System.out.println("metodo addEntryde Posting");
            entries.add(e);
            e.setPosting(this);
        }


    }

    /**
     * Returns true if the posting is balanced. The sum of the credits equals
     * the sum of the debits.
     *
     * @return True or false
     */
    public boolean isBalanced() {
        BigDecimal balance = new BigDecimal(0);

        for (Entry e : entries) {
            balance = balance.add(e.getAmount());
        }

        return balance.floatValue() == 0;
    }

    protected Posting addEntry(String account, BigDecimal amount) throws NonExistantAccountException {
        Entry e = new Entry();
        Account a = ledger.getAccount(account);
        if (a != null) {
            e.setAccount(a);
            e.setAmount(amount);
        } else {
            throw new NonExistantAccountException(account);
        }
        entries.add(e);

        return this;
    }

    protected Posting addEntry(String account, int amount) throws NonExistantAccountException {
        return this.addEntry(account, BigDecimal.valueOf(amount));
    }

    protected Posting addEntry(String account, float amount) throws NonExistantAccountException {
        return this.addEntry(account, BigDecimal.valueOf(amount));
    }

    public Posting credit(String account, BigDecimal amount) throws NonExistantAccountException {
        return addEntry(account, amount);
    }

    public Posting credit(String account, int amount) throws NonExistantAccountException {
        return addEntry(account, amount);
    }

    public Posting credit(String account, float amount) throws NonExistantAccountException {
        return addEntry(account, amount);
    }

    public Posting debit(String account, BigDecimal amount) throws NonExistantAccountException {
        return addEntry(account, amount.multiply(BigDecimal.valueOf(-1)));
    }

    public Posting debit(String account, int amount) throws NonExistantAccountException {
        return addEntry(account, BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(-1)));
    }

    public Posting debit(String account, float amount) throws NonExistantAccountException {
        return addEntry(account, BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(-1)));
    }

    void post() throws PostingUnbalancedException {
        if (isBalanced()) {
            for (Entry entry : entries) {
                entry.getAccount().addEntry(entry);
            }
        } else {
            throw new PostingUnbalancedException(this);
        }
    }
}
