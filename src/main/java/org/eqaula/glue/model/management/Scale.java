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
package org.eqaula.glue.model.management;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eqaula.glue.model.BussinesEntity;

/**
 *
 * @author dianita
 */

@Entity
@Table(name = "Scale")
@DiscriminatorValue(value = "SCL")
@PrimaryKeyJoinColumn(name = "id")
public class Scale extends BussinesEntity implements Serializable {
    private static final long serialVersionUID = -135048321817423962L;
    @OneToMany(mappedBy = "scale")
    private List<RevisionItem> revisionItems;
    
    @OneToMany(mappedBy = "scale", cascade = CascadeType.ALL)
    private List<Valuation> valuations;

    public List<Valuation> getValuations() {
        return valuations;
    }

    public void setValuations(List<Valuation> valuations) {
        this.valuations = valuations;
    }

    public List<RevisionItem> getRevisionItems() {
        return revisionItems;
    }

    public void setRevisionItems(List<RevisionItem> revisionItems) {
        this.revisionItems = revisionItems;
    }
    
    
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getName()).
                append(getType()).
                toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Scale other = (Scale) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getName(), other.getName()).
                append(getType(), other.getType()).
                isEquals();
    }

    @Override
    public String toString() {
        return "org.eqaula.glue.model.management.Scale[ "
                + "id=" + getId() + ","
                + "name=" + getName() + ","
                + "type=" + getType() + ","
                + " ]";
    }
    
}
