package org.openTwoFactor.server.beans;

import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * actions
 */
public enum TwoFactorAuditAction {
  
  IMPORT_FOB_SERIALS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsImportFobSerials";
    }

  }, 
  /**
   * send a code to a phone
   */
  SEND_EMAIL_TO_ALL_USERS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsSendEmailToAllUsers";
    }
    
  },

  /**
   * send a code to a phone
   */
  TEST_SEND_EMAIL_TO_ALL_USERS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsSendTestEmailToAllUsers";
    }
    
  },

  /**
   * send a code to a phone
   */
  SEND_CODE_TO_PHONE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsSendCodeToPhone";
    }
    
  },

  /**
   * error
   */
  ERROR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsError";
    }
    
  },

  /**
   * assign the profile email address from subject
   */
  SET_PROFILE_EMAIL_FROM_SUBJECT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAssignProfileEmailFromSubject";
    }
    
  },

  
  /**
   * error authenticating
   */
  AUTHN_ERROR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsErrorAuthenticating";
    }
    
  },
  
  /**
   * if a colleague opted me out
   */
  COLLEAGUE_OPTED_ME_OUT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsFriendOptedMeOut";
    }
    
  },
  
  /**
   * error authenticating
   */
  OPTED_OUT_A_COLLEAGUE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptedOutFriend";
    }
    
  },
  
  /**
   * view profile
   */
  VIEW_PROFILE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsViewProfile";
    }
    
  },
  
  /**
   * edit profile
   */
  EDIT_PROFILE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsEditProfile";
    }
    
  },
  
  /**
   * two factor authn
   */
  AUTHN_TWO_FACTOR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTwoStepAuthentication";
    }
    
  }, 
  
  /**
   * one factor authn
   */
  AUTHN_TWO_FACTOR_FORBIDDEN {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTwoStepAuthenticationForbidden";
    }
    
  }, 
  
  /**
   * two factor authn is required
   */
  AUTHN_TWO_FACTOR_REQUIRED {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTwoStepAuthenticationRequired";
    }
    
  }, 
  
  /**
   * authn trusted browser no pass
   */
  AUTHN_TRUSTED_BROWSER {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTrustedBrowserUse";
    }
    
  }, 
  
  /**
   * not opted in authn
   */
  AUTHN_NOT_OPTED_IN {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsNonTwoStepAuthentication";
    }
    
  }, 
  
  /**
   * optin to two factor
   */
  OPTIN_TWO_FACTOR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptIn";
    }
    
  }, 
  
  /**
   * optin to two factor registering a fob serial number
   */
  REGISTER_FOB_SERIAL_NUMBER {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsRegisterFobBySerial";
    }
    
  }, 
  
  /**
   * test adding phone
   */
  ADD_PHONE_TEST {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAddPhoneTest";
    }

  }, 
  
  /**
   * add phone
   */
  ADD_PHONE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAddPhone";
    }
    
  }, 
  
  /**
   * optin to two factor
   */
  INVITE_COLLEAGUES {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsInviteFriendsToOptMeOut";
    }
    
  }, 
  
  /**
   * optin to two factor first part before verified
   */
  OPTIN_TWO_FACTOR_STEP1 {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInStep1";
    }
    
  }, 
  
  /**
   * opt out of two factor
   */
  OPTOUT_TWO_FACTOR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptOut";
    }
    
  }, 

  /**
   * edit report
   */
  ADMIN_REPORT_EDIT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAdminReportEdit";
    }
    
  }, 
  
  /**
   * view report
   */
  REPORT_VIEW {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsReportView";
    }
    
  }, 
  

  /**
   * untrust browsers
   */
  UNTRUST_BROWSERS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsUntrustBrowsers";
    }
    
  }, 
  
  /**
   * authn wrong password
   */
  AUTHN_WRONG_PASSWORD {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsWrongCode";
    }
    
  }, 
  
  /**
   * invalidate passwords
   */
  INVALIDATE_PASSWORDS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsInvalidateSingleUseCodes";
    }
    
  }, 
  
  /**
   * generated passwords (and invalidate old ones)
   */
  GENERATE_PASSWORDS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsGenerateSingleUseCodes";
    }
    
  };
  
  /**
   * get screen action
   * @return the screen action
   */
  public abstract String toStringForUi();
  
  /**
   * take a string and convert to enum
   * @param string
   * @return the enum
   */
  public static TwoFactorAuditAction valueOfIgnoreCase(String string) {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TwoFactorAuditAction.class, string, false, true);
  }

}