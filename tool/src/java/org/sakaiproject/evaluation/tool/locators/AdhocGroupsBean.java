package org.sakaiproject.evaluation.tool.locators;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This is not a true Bean Locator. It's primary purpose is
 * for handling the calls for adhoc groups from the
 * modify_adhoc_groups page.
 * 
 * @author Steven Githens
 */
public class AdhocGroupsBean {
   private static Log log = LogFactory.getLog(AdhocGroupsBean.class);
   
   public static final String SAVED_NEW_ADHOCGROUP = "added-adhoc-group";
    
   private Long adhocGroupId;
   private String adhocGroupTitle;
   private String newAdhocGroupUsers;
   
   public List<String> acceptedInternalUsers = new ArrayList<String>();
   public List<String> acceptedAdhocUsers = new ArrayList<String>();
   public List<String> rejectedUsers = new ArrayList<String>();
   
   /*
    * Adds more users to an existing adhocgroup using the data entered with
    * adhocGroupId and newAdhocGroupUsers
    */
   public void addUsersToAdHocGroup() {
       String currentUserId = externalLogic.getCurrentUserId();
       EvalAdhocGroup group = evalAdhocSupport.getAdhocGroupById(new Long(adhocGroupId));
       adhocGroupId = group.getId();
       
       /*
        * You can only change the adhoc group if you are the owner.
        */
       if (!currentUserId.equals(group.getOwner())) {
          throw new SecurityException("Only EvalAdhocGroup owners can change their groups: " + group.getId() + " , " + currentUserId);
       }
   
   }
   
   /**
    * Adds a new Adhoc Group using the data entered into newAdhocGroupUsers.
    * 
    * @return
    */
   public String addNewAdHocGroup() {
      String currentUserId = externalLogic.getCurrentUserId();
      /*
       * At the moment we allow any registered user to create adhoc groups.
       */
      if (externalLogic.isUserAnonymous(currentUserId)) {
         throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
      }
      
      EvalAdhocGroup group = new EvalAdhocGroup(currentUserId, adhocGroupTitle);

      log.info("About to save Adhoc group: " + adhocGroupTitle);
      
      List<String> participants = new ArrayList<String>();
      checkAndAddToParticipantsList(newAdhocGroupUsers, participants);
     
      group.setParticipantIds(participants.toArray(new String[] {}));
      
      evalAdhocSupport.saveAdhocGroup(group);
      adhocGroupId = group.getId();

      log.info("Saved adhoc group: " + adhocGroupId);
	
      return SAVED_NEW_ADHOCGROUP;
   }
   
   /*
    * Adds folks to the participants list and does validation and stuff.
    */
   private void checkAndAddToParticipantsList(String data, List<String> participants) {
       String[] potentialMembers = data.split("\n");
       
       Boolean useAdhocusers = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS);
       /*
        * As we go through the newline seperated list of folks we look for 2 things.
        * 1. Is the id for an existing user in the system?
        * 2. If Adhoc users are allowed, is this a valid email address?
        * 3. Otherwise add it to the garbage list.
        */
       for (String next: potentialMembers) {
    	   String potentialId = next.trim();
           if (externalLogic.getUserId(potentialId) != null) {
               participants.add(externalLogic.getUserId(potentialId));
               acceptedInternalUsers.add(potentialId);  
           }
           else if (useAdhocusers && EvalUtils.isValidEmail(potentialId)) {
               EvalAdhocUser newuser = new EvalAdhocUser(externalLogic.getCurrentUserId(), potentialId);
               evalAdhocSupport.saveAdhocUser(newuser);
               participants.add(newuser.getUserId());
               acceptedAdhocUsers.add(potentialId);
           }
           else {
               rejectedUsers.add(potentialId);
           }
       }
   }
   
   /*
    * Boilerplate Getters and Setters below.
    */
   
   public Long getAdhocGroupId() {
      return adhocGroupId;
   }
   public void setAdhocGroupId(Long adhocGroupId) {
      this.adhocGroupId = adhocGroupId;
   }

   public String getAdhocGroupTitle() {
      return adhocGroupTitle;
   }

   public void setAdhocGroupTitle(String adhocGroupTitle) {
      this.adhocGroupTitle = adhocGroupTitle;
   }

   public String getNewAdhocGroupUsers() {
       return newAdhocGroupUsers;
   }

   public void setNewAdhocGroupUsers(String newAdhocGroupUsers) {
       this.newAdhocGroupUsers = newAdhocGroupUsers;
   }
   
   private EvalAdhocSupport evalAdhocSupport;
   public void setEvalAdhocSupport(EvalAdhocSupport bean) {
      this.evalAdhocSupport = bean;
   }
   
   private EvalExternalLogic externalLogic;
   public void setEvalExternalLogic(EvalExternalLogic logic) {
      this.externalLogic = logic;
   }
   
   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }
   
   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
	  this.messages = messages;
   }
   
}
