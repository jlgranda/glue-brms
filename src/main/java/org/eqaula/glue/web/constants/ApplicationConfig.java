/**
* This file is part of Glue: Adhesive BRMS
*
* Copyright (c)2012 José Luis Granda <jlgranda@eqaula.org> (Eqaula Tecnologías Cia Ltda)
* Copyright (c)2012 Eqaula Tecnologías Cia Ltda (http://eqaula.org)
*
* If you are developing and distributing open source applications under
* the GNU General Public License (GPL), then you are free to re-distribute Glue
* under the terms of the GPL, as follows:
*
* GLue is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Glue is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Glue. If not, see <http://www.gnu.org/licenses/>.
*
* For individuals or entities who wish to use Glue privately, or
* internally, the following terms do not apply:
*
* For OEMs, ISVs, and VARs who wish to distribute Glue with their
* products, or host their product online, Eqaula provides flexible
* OEM commercial licenses.
*
* Optionally, Customers may choose a Commercial License. For additional
* details, contact an Eqaula representative (sales@eqaula.org)
*/
package org.eqaula.glue.web.constants;

import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
public class ApplicationConfig
{
   public static final String GUEST_ACCOUNT_NAME = "guest";

   private String siteName = "Glue";
   private String blogUrl = "http://eqaula.org/glue";
   private boolean analyticsEnabled = false;
   private String analyticsId = "";

   public String getBlogUrl()
   {
      return blogUrl;
   }

   public void setBlogUrl(final String blogUrl)
   {
      this.blogUrl = blogUrl;
   }

   public String getSiteName()
   {
      return siteName;
   }

   public void setSiteName(final String siteName)
   {
      this.siteName = siteName;
   }

   public boolean isAnalyticsEnabled()
   {
      return analyticsEnabled;
   }

   public void setAnalyticsEnabled(final boolean analyticsEnabled)
   {
      this.analyticsEnabled = analyticsEnabled;
   }

   public String getAnalyticsId()
   {
      return analyticsId;
   }

   public void setAnalyticsId(final String analyticsId)
   {
      this.analyticsId = analyticsId;
   }

   public boolean isDebugMode()
   {
      return ProjectStage.Development.equals(FacesContext.getCurrentInstance().getApplication().getProjectStage());
   }
}
