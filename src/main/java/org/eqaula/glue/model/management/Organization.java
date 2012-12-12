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
package org.eqaula.glue.model.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.Group;
import org.eqaula.glue.model.profile.Profile;

/**
 *
 * @author jlgranda
 */
@Entity
@Table(name = "Organization")
@DiscriminatorValue(value = "ORG")
@PrimaryKeyJoinColumn(name = "id")
public class Organization extends BussinesEntity implements Serializable {
    private static final long serialVersionUID = 3095488521256724258L;
    
    
    @ManyToMany(targetEntity = Group.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "organization_profile", joinColumns =
    @JoinColumn(name = "organizationid", referencedColumnName = "id"),
    inverseJoinColumns =
    @JoinColumn(name = "profileid", referencedColumnName = "id"))
    private List<Profile> owners = new ArrayList<Profile>();

    public List<Profile> getOwners() {
        return owners;
    }

    public void setOwners(List<Profile> owners) {
        this.owners = owners;
    }
    
}
