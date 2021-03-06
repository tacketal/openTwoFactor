/**
 * 
 */
package org.openTwoFactor.server.ui.beans;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * main container in request scope that has all the subcontainers lazy loaded inside
 * @author mchyzer
 *
 */
public class TwoFactorRequestContainer {
  
  /**
   * two factor view report container
   */
  private TwoFactorViewReportContainer twoFactorViewReportContainer;
  
  /**
   * @return the twoFactorViewReportContainer
   */
  public TwoFactorViewReportContainer getTwoFactorViewReportContainer() {
    if (this.twoFactorViewReportContainer == null) {
      this.twoFactorViewReportContainer = new TwoFactorViewReportContainer();
    }
    return this.twoFactorViewReportContainer;
  }

  /**
   * is has logout url
   * @return if has logout url
   */
  public boolean isHasLogoutUrl() {
    return !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.logoutUrl"));
  }
  
  /**
   * 
   * @return true if editable email
   */
  public boolean isEditableEmail() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.editableEmail", true);
  }
  
  /**
   * container for adding phone
   */
  private TwoFactorAddPhoneContainer twoFactorAddPhoneContainer = null;
  
  /**
   * container for adding phone
   * @return container
   */
  public TwoFactorAddPhoneContainer getTwoFactorAddPhoneContainer() {
    if (this.twoFactorAddPhoneContainer == null) {
      this.twoFactorAddPhoneContainer = new TwoFactorAddPhoneContainer();
    }
    return this.twoFactorAddPhoneContainer;
  }

  /**
   * admin container bean for users using the admin screen(s)
   */
  private TwoFactorAdminContainer twoFactorAdminContainer = null;
  
  /**
   * admin container bean for users using the admin screen(s)
   * @return the twoFactorAdminContainer
   */
  public TwoFactorAdminContainer getTwoFactorAdminContainer() {
    if (this.twoFactorAdminContainer == null) {
      this.twoFactorAdminContainer = new TwoFactorAdminContainer();
    }
    return this.twoFactorAdminContainer;
  }
  
  /**
   * profile container bean for users using the profile screen(s)
   */
  private TwoFactorProfileContainer twoFactorProfileContainer = null;
  
  /**
   * profile container bean for users using the profile screen(s)
   * @return the twoFactorProfileContainer
   */
  public TwoFactorProfileContainer getTwoFactorProfileContainer() {
    if (this.twoFactorProfileContainer == null) {
      this.twoFactorProfileContainer = new TwoFactorProfileContainer();
    }
    return this.twoFactorProfileContainer;
  }
  
  /**
   * two factor help logging in container
   */
  private TwoFactorHelpLoggingInContainer twoFactorHelpLoggingInContainer;
  
  /**
   * two factor help logging in container
   * @return the twoFactorHelpLoggingInContainer
   */
  public TwoFactorHelpLoggingInContainer getTwoFactorHelpLoggingInContainer() {
    if (this.twoFactorHelpLoggingInContainer == null) {
      this.twoFactorHelpLoggingInContainer = new TwoFactorHelpLoggingInContainer();
    }
    return this.twoFactorHelpLoggingInContainer;
  }

  
  /**
   * two factor audit container
   */
  private TwoFactorAuditContainer twoFactorAuditContainer = null;
  
    /**
   * two factor audit container, lazy load
   * @return the twoFactorAuditContainer
   */
  public TwoFactorAuditContainer getTwoFactorAuditContainer() {
    if (this.twoFactorAuditContainer == null) {
      this.twoFactorAuditContainer = new TwoFactorAuditContainer();
    }
    return this.twoFactorAuditContainer;
  }
  
  /**
   * two factor config container
   */
  private TwoFactorConfigContainer twoFactorConfigContainer = null;
  
    /**
   * two factor config container, lazy load
   * @return the twoFactorConfigContainer
   */
  public TwoFactorConfigContainer getTwoFactorConfigContainer() {
    if (this.twoFactorConfigContainer == null) {
      this.twoFactorConfigContainer = new TwoFactorConfigContainer();
    }
    return this.twoFactorConfigContainer;
  }
  
  /**
   * two factor untrust browser container, lazy load
   * @return the twoFactorUntrustBrowserContainer
   */
  public TwoFactorUntrustBrowserContainer getTwoFactorUntrustBrowserContainer() {
    if (this.twoFactorUntrustBrowserContainer == null) {
      this.twoFactorUntrustBrowserContainer = new TwoFactorUntrustBrowserContainer();
    }
    return this.twoFactorUntrustBrowserContainer;
  }
  
  
  /**
   * when showing one time passes, this is the list of rows
   */
  private List<TwoFactorOneTimePassRow> oneTimePassRows = null;
  
  /**
   * when showing one time passes, this is the list of rows
   * @return the oneTimePassRows
   */
  public List<TwoFactorOneTimePassRow> getOneTimePassRows() {
    return this.oneTimePassRows;
  }

  
  /**
   * when showing one time passes, this is the list of rows
   * @param oneTimePassRows1 the oneTimePassRows to set
   */
  public void setOneTimePassRows(List<TwoFactorOneTimePassRow> oneTimePassRows1) {
    this.oneTimePassRows = oneTimePassRows1;
  }

  /** error for screen */
  private String error;
  
 
  
  /**
   * error for screen
   * @return error
   */
  public String getError() {
    return this.error;
  }

  /**
   * error for screen
   * @param error1
   */
  public void setError(String error1) {
    this.error = error1;
  }

  /**
   * init the request container with a DAO
   * @param twoFactorDaoFactory 
   * @param loggedInUser String loggedInUser = TwoFactorFilterJ2ee.retrieveUserPrincipalNameFromRequest();

   */
  public void init(TwoFactorDaoFactory twoFactorDaoFactory, String loggedInUser) {

    if (TwoFactorServerUtils.isBlank(loggedInUser)) {
      throw new RuntimeException("There is no user logged in");
    }
    if (this.getTwoFactorUserLoggedIn() == null) {

      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, loggedInUser);
      
      this.setTwoFactorUserLoggedIn(twoFactorUser);
      
    }
    
  }
  
  /**
   * retrieve the container from the request or create a new one if not there
   * @return the container
   */
  public static TwoFactorRequestContainer retrieveFromRequest() {
    
    TwoFactorRequestContainer twoFactorRequestContainer = 
        (TwoFactorRequestContainer)TwoFactorFilterJ2ee
        .retrieveHttpServletRequest().getAttribute("twoFactorRequestContainer");
    
    if (twoFactorRequestContainer == null) {
      twoFactorRequestContainer = new TwoFactorRequestContainer();
      TwoFactorFilterJ2ee.retrieveHttpServletRequest().setAttribute(
          "twoFactorRequestContainer", twoFactorRequestContainer);
    }
    
    return twoFactorRequestContainer;
    
  }
  
  /**
   * the two factor user logged in, or lazy load if not exist
   */
  private TwoFactorUser twoFactorUserLoggedIn;

  /**
   * untrust browser container bean
   */
  private TwoFactorUntrustBrowserContainer twoFactorUntrustBrowserContainer = null;
  
  /**
   * the two factor user logged in, or lazy load if not exist
   * @return the two factor user logged in, or lazy load if not exist
   */
  public TwoFactorUser getTwoFactorUserLoggedIn() {
    return this.twoFactorUserLoggedIn;
  }

  /**
   * the two factor user logged in, or lazy load if not exist
   * @param twoFactorUserLoggedIn1
   */
  public void setTwoFactorUserLoggedIn(TwoFactorUser twoFactorUserLoggedIn1) {
    this.twoFactorUserLoggedIn = twoFactorUserLoggedIn1;
  }
  
}
