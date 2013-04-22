/**
 * This file is part of OCPsoft SocialPM: Agile Project Management Tools (SocialPM) 
 *
 * Copyright (c)2011 Lincoln Baxter, III <lincoln@ocpsoft.com> (OCPsoft)
 * Copyright (c)2011 OCPsoft.com (http://ocpsoft.com)
 * 
 * If you are developing and distributing open source applications under 
 * the GNU General Public License (GPL), then you are free to re-distribute SocialPM 
 * under the terms of the GPL, as follows:
 *
 * SocialPM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SocialPM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SocialPM.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For individuals or entities who wish to use SocialPM privately, or
 * internally, the following terms do not apply:
 *  
 * For OEMs, ISVs, and VARs who wish to distribute SocialPM with their 
 * products, or host their product online, OCPsoft provides flexible 
 * OEM commercial licenses.
 * 
 * Optionally, Customers may choose a Commercial License. For additional 
 * details, contact an OCPsoft representative (sales@ocpsoft.com)
 */
package org.eqaula.glue.controller.management;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.security.Account;
import org.jboss.seam.international.status.Messages;
import org.eqaula.glue.service.OrganizationListService;
import org.eqaula.glue.service.OrganizationService;
import org.eqaula.glue.web.ParamsBean;
import org.eqaula.glue.web.constants.UrlConstants;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Named
@ConversationScoped
public class Organizations implements Serializable
{
   private static final long serialVersionUID = -5792291552146633049L;

   private Messages messages;
   private ParamsBean params;
   private Account account;
   private OrganizationListService organizationListService;
   private OrganizationService organizationService;

   private Profile profile;
   private Organization current = new Organization();

   public Organizations()
   {}

   @Inject
   public Organizations(final @Web EntityManager em, final OrganizationService organizationService, final OrganizationListService organizationListService, final Messages messages,
            final ParamsBean params,
            final @Current Profile profile,
            final Account account)
   {
      this.params = params;
      this.profile = profile;
      this.account = account;
      this.messages = messages;
      this.organizationListService = organizationListService;
      this.organizationService = organizationService;
      this.organizationService.setEntityManager(em);
   }

   public String loadCurrent()
   {
      Organization current = getCurrent();
      if (!current.isPersistent() && account.getLoggedIn().isPersistent())
      {
         messages.error("Oops! We couldn't find that project. Want to create one instead?");
         return "/pages/project/create";
      }
      else if (!current.isPersistent())
      {
         messages.error("Oops! We couldn't find the project you asked for.");
         return "/pages/404";
      }
      return null;
   }

   public String create()
   {
      organizationService.create(account.getLoggedIn(), current);
      return UrlConstants.PROJECT_VIEW + "&project=" + current.getCode() + "&profile="
               + account.getLoggedIn().getUsername();
   }

   public long getCount()
   {
      return organizationService.findByProfile(profile).size();
      //count();
   }

   public List<Organization> getAll()
   {
       if (profile.isPersistent()){
           return organizationService.findByProfile(profile);
       } else {
           //TODO mejorar admin case
           return organizationService.getOrganizations();
       }
      
   }
/*
   public int getAssignedTaskCount(final Profile profile, final Organization organization)
   {
      // TODO this is super inefficient, figure this out
      int count = 0;
      for (Story s : organization.getStories()) {
         count += stories.getAssignedTaskCount(profile, s);
      }
      return count;
   }*/
/*
   public int getAssignedStoryCount(final Profile profile, final Project project)
   {
      int count = getAssignedStories(profile, project).size();
      return count;
   }

   public List<Story> getAssignedStories(final Profile profile, final Project project)
   {
      List<Story> result = new ArrayList<Story>();
      for (Story s : project.getStories()) {
         for (Task t : s.getTasks()) {
            if (profile.getUsername().equals(t.getAssignee().getUsername()))
            {
               result.add(s);
            }
         }
      }
      return result;
   }
*/
   @Produces
   @Named("organization")
   @RequestScoped
   public Organization getCurrent()
   {
      if ((current != null) && (params.getOrganizationCode() != null))
      {
         try
         {
            if (profile.isPersistent())
            {
               //Organization found = organizationService.findByProfileAndSlug(profile, params.getProjectSlug());
                Organization found = organizationService.findByProfileAndCode(profile, params.getOrganizationCode());
               if (found != null)
               {
                  current = found;
               }
            }
         }
         catch (NoResultException e)
         {}
      }
      return current;
   }

   /*@Produces
   @Named("currentIteration")
   public Iteration getCurrentIteration()
   {
      Iteration iteration = getCurrent().getCurrentIteration();
      return iteration;
   }*/

   public void setCurrent(final Organization current)
   {
      this.current = current;
   }

   public boolean isActive()
   {
      return this.current.getId() != null;
   }

}
