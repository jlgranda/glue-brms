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
package org.eqaula.glue.controller.profile;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.inject.Named;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.profile.ProfileService;
import org.eqaula.glue.util.Dates;

/**
 *
 * @author lucho
 */
@Named(value = "customizeProfileHome")
@ViewScoped
public class CustomizeProfileHome extends BussinesEntityHome<Profile> implements Serializable {

    private static final long serialVersionUID = 3228657631384733490L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(CustomizeProfileHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private ProfileService profileService;
    private Long postingId;

    public CustomizeProfileHome() {
    }

    public Long getProfileId() {
        return (Long) getId();
    }

    public void setProfileId(Long profileId) {
        setId(profileId);
    }

    public Long getPostingId() {
        return postingId;
    }

    public void setPostingId(Long postingId) {
        this.postingId = postingId;
    }

    @Override
    protected Profile createInstance() {
        Date now = Calendar.getInstance().getTime();
        Profile profile = new Profile();
        profile.setCreatedOn(now);
        profile.setLastUpdate(now);
        profile.setActivationTime(now);
        profile.setExpirationTime(Dates.addDays(now, 364));
        profile.setAuthor(null);
        return profile;
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
        log.info("eqaula --> Loaded instance " + getInstance());
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    public boolean isWired() {
        return true;
    }

    public Profile getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
        profileService.setEntityManager(em);
    }

    @TransactionAttribute
    public String saveProfile() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setUsername(getInstance().getEmail());
            getInstance().setPassword(getInstance().getCode());

            create(getInstance());
        }

        return "/pages/accounting/ledger/voucher.xhtml?organizationId=" + getOrganizationId() + "&profileId=" + getInstance().getId() + "&faces-redirect=true&includeViewParams=true";

    }


}
