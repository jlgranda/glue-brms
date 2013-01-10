/*
 * Copyright 2012 jlgranda.
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
package org.eqaula.glue.accounting;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.drools.rule.Collect;
import org.eqaula.glue.model.accounting.Account_;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;
import org.eqaula.glue.util.Strings;

/**
 *
 * @author jlgranda
 */
public class AccountService extends PersistenceUtil<Account> {

    public AccountService() {
        super(Account.class);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    //metodo count
    public long count() {
        return count(Account.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void create(final Account account) {
        super.create(account);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void save(final Account account) {
        super.save(account);
        em.flush();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getRandomName(String seed) {
        String name = Strings.canonicalize(seed);
        while (!isNameAvailable(name)) {
            name += new Random(System.currentTimeMillis()).nextInt() % 100;
        }
        return name;
    }

    public long getAccountCount() {
        return count(Profile.class);
    }

    public List<Account> getAccounts(final int limit, final int offset) {
        return findAll(Account.class);
    }

    public Account getAccountByName(final String name) throws NoResultException {
        TypedQuery<Account> query = em.createQuery("SELECT a FROM Account a WHERE a.name = :name", Account.class);
        query.setParameter("name", name);

        Account result = query.getSingleResult();
        return result;
    }

    public Account getAccountByCode(final String code) throws NoResultException {
        TypedQuery<Account> query = em.createQuery("SELECT a FROM Account a WHERE a.code = :code", Account.class);
        query.setParameter("code", code);

        Account result = query.getSingleResult();
        return result;
    }
    
     
    public  List<Account> getAccounts(){        
        List list = this.findAll(Account.class);
        Collections.sort(list);
        return list;
        
    }
    /*public boolean hasProfileByIdentityKey(final String key) throws NoResultException
     {
     try {
     getProfileByIdentityKey(key);
     return true;
     }
     catch (NoResultException e) {
     return false;
     }
     }

     public Profile getProfileByIdentityKey(final String key) throws NoResultException
     {
     TypedQuery<Profile> query = em.createQuery(
     "SELECT p FROM Profile p JOIN p.identityKeys k WHERE k = :identityKey", Profile.class);
     query.setParameter("identityKey", key);

     Profile result = query.getSingleResult();
     return result;
     }*/
    public Account getAccountById(final Long id) {
        return (Account) findById(Account.class, id);
    }

    public boolean isNameAvailable(String name) {
        try {
            getAccountByName(name);
            return false;
        } catch (NoResultException e) {
            return true;
        }
    }

    public boolean isCodeAvailable(String code) {
        try {
            getAccountByCode(code);
            return false;
        } catch (NoResultException e) {
            return true;
        }
    }
}
