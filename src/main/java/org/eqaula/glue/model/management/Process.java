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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eqaula.glue.model.BussinesEntity;

/*
 * @author dianita
 */
@Entity
@Table(name = "Process")
@DiscriminatorValue(value = "PCSS")
@PrimaryKeyJoinColumn(name = "id")
public class Process extends BussinesEntity implements Serializable {

    private static final long serialVersionUID = 8891572386502333388L;
    private String series;
    @ManyToOne
    private Macroprocess macroprocess;

    public Macroprocess getMacroprocess() {
        return macroprocess;
    }

    public void setMacroprocess(Macroprocess macroprocess) {
        this.macroprocess = macroprocess;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
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
        Process other = (Process) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getName(), other.getName()).
                append(getType(), other.getType()).
                isEquals();
    }

    @Override
    public String getCanonicalPath() {
        StringBuilder path = new StringBuilder();
        path.append(getMacroprocess().getCanonicalPath());
        path.append(BussinesEntity.SEPARATOR); //TODO hacer que sea personalizable
        path.append(getName());
        return path.toString();
    }

    @Override
    public String toString() {
        /* return "org.eqaula.glue.model.management.Process[ "
         + "id=" + getId() + ","
         + "name=" + getName() + ","
         + "type=" + getType() + ","
         + " ]";        */

        return getName();
    }
}
