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
package org.eqaula.glue.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import org.apache.commons.lang.SerializationUtils;
import org.eqaula.glue.util.Lists;

/**
 *
 * @author jlgranda
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Property.findByBussinesEntityTypeName",
    query = "select p FROM Property p "
    + "WHERE p.structure.bussinesEntityType.name = :bussinesEntityTypeName ORDER BY p.id")
})
public class Property implements Serializable {

    private static final long serialVersionUID = 1020047606754217515L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "property_id")
    private Long id;
    private String groupName; //nombre de grupo
    private String name;
    private String label;
    private String type;
    private boolean required;
    private String helpInline;
    @Basic(fetch = FetchType.LAZY)
    private byte[] valueByteArray;
    private String fragmentXHTML;
    @Transient
    private Object value;
    @Transient
    private List<String> values;
    private String render;
    private String converter;
    
    @ManyToOne
    @JoinColumn(name = "structure_id")
    private Structure structure;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getHelpInline() {
        return helpInline;
    }

    public void setHelpInline(String helpInline) {
        this.helpInline = helpInline;
    }

    protected byte[] getValueByteArray() {
        return valueByteArray;
    }

    protected void setValueByteArray(byte[] valueByteArray) {
        this.valueByteArray = valueByteArray;
    }

    public Object getValue() {
        return SerializationUtils.deserialize(getValueByteArray());
    }

    public void setValue(Serializable value) {
        setValueByteArray(SerializationUtils.serialize((Serializable) value));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFragmentXHTML() {
        return fragmentXHTML;
    }

    public void setFragmentXHTML(String fragmentXHTML) {
        this.fragmentXHTML = fragmentXHTML;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }
    
    public List<String> getValues(){
        return Lists.stringToList(getValue().toString());
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Property)) {
            return false;
        }
        Property other = (Property) object;
        if ((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id))
                || (this.name != null && !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.eqaula.glue.model.StructureAttribute[ id=" + id + " ]";
    }
}
