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
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eqaula.glue.model.BussinesEntity;

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
    
    private String ruc;
    
    private String initials;
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Owner> owners = new ArrayList<Owner>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Theme> themes = new ArrayList<Theme>();
    
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public void setOwners(List<Owner> owners) {
        this.owners = owners;
    }
    
    public boolean addOwner(Owner owner){
        owner.setOrganization(this);
        return getOwners().add(owner);
    }
    
    public boolean removeOwner(Owner owner){
        owner.setOrganization(null);
        return getOwners().remove(owner);
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }
    
    @Override
    public String getCanonicalPath(){
        StringBuilder path = new StringBuilder();
        path.append(getName());
        return path.toString();
    }
    
    @Override
    public String toString() {
        return "org.eqaula.glue.model.management.Organization[ "
                + "id=" + getId() + ","
                + "name=" + getName() + ","
                + "type=" + getType() + ","
                + " ]";
    }
    
}
