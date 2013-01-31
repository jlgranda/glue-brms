/*
 * Copyright 2013 cesar.
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
package org.eqaula.glue.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ViewScoped;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.profile.ProfileService;
import org.jboss.seam.reports.Report;
import org.jboss.seam.reports.ReportCompiler;
import org.jboss.seam.reports.ReportDefinition;
import org.jboss.seam.reports.ReportRenderer;
import org.jboss.seam.reports.jasper.annotations.Jasper;
import org.jboss.seam.reports.output.PDF;
import org.jboss.solder.resourceLoader.Resource;

/**
 *
 * @author cesar
 */
@Named(value="profileReport")
@RequestScoped
public class ProfileReport implements Serializable{
    
    @Inject
    @Resource("/src/main/resources/ProfileJasperReport.jrxml")     //resources/ProfileJasperReport.jrxml
    private InputStream sourceReport;
    
    @Inject
    @Web
    private EntityManager em;
    
    @Inject
    @Jasper
    private ReportCompiler compiler;
    
    @Jasper
    private JRDataSource jasperDataSource;
    
    @Inject  
    @Jasper  
    @PDF  
    private ReportRenderer pdfRenderer; 
    
    @Inject    
    private ProfileService ps;
    
    public ByteArrayOutputStream generateProfileReport() throws IOException{
        ps.setEntityManager(em);
        ReportDefinition report = compiler.compile(sourceReport);
        Map<String, Object> paramt = new HashMap<String, Object>();
        paramt.put("ReportTitle", "Profile Report");
        
        /* TODO---  faltan librerias de 'seam-reports-jasper' [null = new JRBeanCollectionDataSource()]*/
        Report reporInstance = report.fill(new JRBeanCollectionDataSource(ps.findAll()), paramt);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        pdfRenderer.render(reporInstance, os);
        return os;
        
    }
    
    public void CreatePDF() throws IOException{
        byte[] pdfData = generateProfileReport().toByteArray();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        
        HttpServletResponse response = (HttpServletResponse)externalContext.getResponse();
        response.reset();
        response.setContentType("profile/pdf");
        response.setHeader("Content-disposition", "attachment; filename=\"profileReport.pdf\"");
        
        OutputStream output = response.getOutputStream();
    }
}
