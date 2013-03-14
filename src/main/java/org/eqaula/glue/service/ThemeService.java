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
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.management.Theme_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class ThemeService extends PersistenceUtil<Theme> implements Serializable{
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -3246755068687015421L;

    public ThemeService() {
        super(Theme.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Theme.class);
    }
    
    public List<Theme> getThemes() {
        return findAll(Theme.class);
    }

    public List<Theme> getThemes(final int limit, final int offset) {
        return findAll(Theme.class);
    }

    public Theme getThemeById(final Long id) {
        return (Theme) findById(Theme.class, id);
    }
    
    public Theme findByName(final String name) {
        log.info("find Theme with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Theme> query = builder.createQuery(Theme.class);
        Root<Theme> bussinesEntityType = query.from(Theme.class);
        query.where(builder.equal(bussinesEntityType.get(Theme_.name), name));
        return getSingleResult(query);
    }

    public List<Theme> findByProfile(Profile profile) {
        log.info("find Theme with profile " + profile);
        if (profile == null) return new ArrayList<Theme>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Theme> query = builder.createQuery(Theme.class);
        Root<Theme> bussinesEntityType = query.from(Theme.class);
        query.where(builder.equal(bussinesEntityType.get(Theme_.author), profile));
        return getResultList(query);
    }

    public Theme findByProfileAndCode(Profile profile, String themeCode) {
        log.info("find Theme with profile " + profile + " theme code " + themeCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Theme> query = builder.createQuery(Theme.class);
        Root<Theme> bussinesEntityType = query.from(Theme.class);
        query.where(builder.equal(bussinesEntityType.get(Theme_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Theme current) {
        current.setAuthor(loggedIn);
        create(current);
    }
}
