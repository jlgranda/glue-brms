/*
 * Copyright 2013 dianita.
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
package org.eqaula.glue.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.management.Question;
import org.eqaula.glue.model.management.Question_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class QuestionService extends PersistenceUtil<Question> implements Serializable{
    private static final long serialVersionUID = -7930925024008068910L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public QuestionService() {
        super(Question.class);
    }
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Question.class);
    }
    
    public List<Question> getQuestions() {
        return findAll(Question.class);
    }

    public List<Question> getQuestions(final int limit, final int offset) {
        return findAll(Question.class);
    }

    public Question getQuestionById(final Long id) {
        return (Question) findById(Question.class, id);
    }
    
    public Question findByName(final String name) {
        log.info("find Question with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Question> query = builder.createQuery(Question.class);
        Root<Question> bussinesEntityType = query.from(Question.class);
        query.where(builder.equal(bussinesEntityType.get(Question_.name), name));
        return getSingleResult(query);
    }

    public List<Question> findByProfile(Profile profile) {
        log.info("find Question with profile " + profile);
        if (profile == null) return new ArrayList<Question>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Question> query = builder.createQuery(Question.class);
        Root<Question> bussinesEntityType = query.from(Question.class);
        query.where(builder.equal(bussinesEntityType.get(Question_.author), profile));
        return getResultList(query);
    }

    public Question findByProfileAndCode(Profile profile, String questionCode) {
        log.info("find Question with profile " + profile + " question code " + questionCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Question> query = builder.createQuery(Question.class);
        Root<Question> bussinesEntityType = query.from(Question.class);
        query.where(builder.equal(bussinesEntityType.get(Question_.author), profile));
        return getSingleResult(query);
    }
    
    public void create(Profile loggedIn, Question current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
    
}
