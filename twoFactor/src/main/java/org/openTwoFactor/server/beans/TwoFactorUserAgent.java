/**
 * @author mchyzer
 * $Id: TwoFactorUserAgent.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.util.UserAgentUtils;



/**
 * list of user agents from browsers.  Note the java project died, and is not a reliable 
 * user agent -> browser converter
 */
@SuppressWarnings("serial")
public class TwoFactorUserAgent extends TwoFactorHibernateBeanBase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    UserAgentUtils userAgentUtils = new UserAgentUtils();
    String userAgentString = "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:22.0) Gecko/20100101 Firefox/22.0";
    System.out.println(userAgentUtils.browser(userAgentString));
    System.out.println(userAgentUtils.operatingSystem(userAgentString));
    System.out.println(userAgentUtils.mobile(userAgentString));
  }

  /**
   * if this seems like a mobile device from the user agent
   * MOBILE            VARCHAR2(1 CHAR),
   */
  private Boolean mobile;
  
  
  /**
   * if this seems like a mobile device from the user agent
   * MOBILE            VARCHAR2(1 CHAR),
   * @return the mobile
   */
  public Boolean getMobile() {
    return this.mobile;
  }

  /**
   * convert mobile to a string for the DB
   * if this seems like a mobile device from the user agent
   * MOBILE            VARCHAR2(1 CHAR),
   * @return the T or F or null
   */
  public String getMobileDb() {
    return this.mobile == null ? null : (this.mobile ? "T" : "F");
  }
  
  /**
   * convert mobile to a string for the DB
   * if this seems like a mobile device from the user agent
   * MOBILE            VARCHAR2(1 CHAR),
   * @param isMobileDb1
   */
  public void setMobileDb(String isMobileDb1) {
    if (StringUtils.isBlank(isMobileDb1)) {
      this.mobile = null;
    } else if (StringUtils.equals("T", isMobileDb1)) {
      this.mobile = true;
    } else if (StringUtils.equals("F", isMobileDb1)) {
      this.mobile = false;
    } else {
      throw new RuntimeException("Why is DB value: '" + isMobileDb1 + "' when T or F or null is expected????");
    }
  }
  
  /**
   * if this seems like a mobile device from the user agent
   * MOBILE            VARCHAR2(1 CHAR),
   * @param mobile1 the mobile to set
   */
  public void setMobile(Boolean mobile1) {
    this.mobile = mobile1;
  }

  /**
   * assumed OS from the user agent
   * OPERATING_SYSTEM  VARCHAR2(100 CHAR),
   */
  private String operatingSystem;
  
  
  /**
   * assumed OS from the user agent
   * OPERATING_SYSTEM  VARCHAR2(100 CHAR),
   * @return the operatingSystem
   */
  public String getOperatingSystem() {
    return this.operatingSystem;
  }

  
  /**
   * assumed OS from the user agent
   * OPERATING_SYSTEM  VARCHAR2(100 CHAR),
   * @param operatingSystem1 the operatingSystem to set
   */
  public void setOperatingSystem(String operatingSystem1) {
    this.operatingSystem = operatingSystem1;
  }

  /**
   * assumed browser from user agent
   * BROWSER           VARCHAR2(100 CHAR),
   */
  private String browser;
  
  /**
   * assumed browser from user agent
   * BROWSER           VARCHAR2(100 CHAR),
   * @return the browser
   */
  public String getBrowser() {
    return this.browser;
  }
  
  /**
   * assumed browser from user agent
   * BROWSER           VARCHAR2(100 CHAR),
   * @param browser1 the browser to set
   */
  public void setBrowser(String browser1) {
    this.browser = browser1;
  }

  /**
   * user agent string from browser
   * USER_AGENT        VARCHAR2(200 CHAR)          NOT NULL,
   */
  private String userAgent;
  
  /**
   * user agent string from browser
   * USER_AGENT        VARCHAR2(200 CHAR)          NOT NULL,
   * @return the userAgent
   */
  public String getUserAgent() {
    return this.userAgent;
  }
  
  /**
   * user agent string from browser
   * USER_AGENT        VARCHAR2(200 CHAR)          NOT NULL,
   * @param userAgent1 the userAgent to set
   */
  public void setUserAgent(String userAgent1) {
    this.userAgent = userAgent1;
  }

  /**
   * number of inserts and updates
   */
  static int testDeletes = 0;

  /** constant for field name for: browser */
  public static final String FIELD_BROWSER = "browser";

  /** constant for field name for: mobile */
  public static final String FIELD_MOBILE = "mobile";

  /** constant for field name for: operatingSystem */
  public static final String FIELD_OPERATING_SYSTEM = "operatingSystem";

  /** constant for field name for: userAgent */
  public static final String FIELD_USER_AGENT = "userAgent";
  
  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_BROWSER,
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_MOBILE,
      FIELD_OPERATING_SYSTEM,
      FIELD_USER_AGENT,
      FIELD_UUID,
      FIELD_VERSION_NUMBER));

  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_BROWSER,
            FIELD_DELETED_ON,
            FIELD_LAST_UPDATED,
            FIELD_MOBILE,
            FIELD_OPERATING_SYSTEM,
            FIELD_USER_AGENT,
            FIELD_UUID,
            FIELD_VERSION_NUMBER));

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_BROWSER,
            FIELD_DELETED_ON,
            FIELD_LAST_UPDATED,
            FIELD_MOBILE,
            FIELD_OPERATING_SYSTEM,
            FIELD_USER_AGENT,
            FIELD_UUID,
            FIELD_VERSION_NUMBER));

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * retrieve a user agent record by user agent or create, retry if problem
   * @param twoFactorDaoFactory
   * @param userAgent
   * @return the user agent
   */
  public static TwoFactorUserAgent retrieveByUserAgentOrCreate(final TwoFactorDaoFactory twoFactorDaoFactory, final String userAgent) {

    final int LOOP_COUNT = 5;

    //if two threads create it at the same time, then retrieve again
    for (int i=0;i<LOOP_COUNT;i++) {

      try {
        //exception on error if last one
        TwoFactorUserAgent twoFactorUserAgent = retrieveByUserAgentOrCreateHelper(twoFactorDaoFactory, userAgent, i == LOOP_COUNT-1 ? true : false);
        if (twoFactorUserAgent != null) {
          return twoFactorUserAgent;
        }
      } catch (RuntimeException e) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Error in user-agent: " + userAgent, e);
        }
        
        if (i==LOOP_COUNT-1) {
          throw e;
        }

      }
      //wait some time, maybe someone else created it
      TwoFactorServerUtils.sleep((i+1) * 3000);

    }
    throw new RuntimeException("Why are we here?");

  }

  /**
   * retrieve a user agent record by user agent
   * @param twoFactorDaoFactory
   * @param userAgent
   * @param exceptionOnError
   * @return the user agent or null if not found
   */
  private static TwoFactorUserAgent retrieveByUserAgentOrCreateHelper(final TwoFactorDaoFactory twoFactorDaoFactory, final String userAgent, final boolean exceptionOnError) {

    TwoFactorUserAgent twoFactorUserAgent = retrieveByUserAgent(twoFactorDaoFactory, userAgent);
    
    if (twoFactorUserAgent != null) {
      return twoFactorUserAgent;
    }
    
    TwoFactorUserAgent localTwoFactorUserAgent = new TwoFactorUserAgent();
    localTwoFactorUserAgent.setUserAgent(userAgent);
    localTwoFactorUserAgent.setUuid(TwoFactorServerUtils.uuid());
    localTwoFactorUserAgent.calculateBrowserFields();
    Boolean changed = localTwoFactorUserAgent.store(twoFactorDaoFactory, exceptionOnError);
    
    //if not successful, null
    if (changed == null) {
      return null;
    }

    return localTwoFactorUserAgent;
    
  }

  /**
   * retrieve a user agent record by user agent
   * @param twoFactorDaoFactory
   * @param userAgent
   * @return the user agent or null if not found
   */
  public static TwoFactorUserAgent retrieveByUserAgent(final TwoFactorDaoFactory twoFactorDaoFactory, String userAgent) {
  
    if (TwoFactorServerUtils.isBlank(userAgent)) {
      throw new RuntimeException("Why is userAgent blank? ");
    }
    
    //note, this is size 200, but with utf8 chars, maybe it is bigger in the db?
    userAgent = TwoFactorServerUtils.truncateAscii(userAgent, 200);

    final String USER_AGENT = userAgent;
    
    TwoFactorUserAgent result = (TwoFactorUserAgent)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorUserAgent twoFactorUserAgent = twoFactorDaoFactory.getTwoFactorUserAgent().retrieveByUserAgent(USER_AGENT);
        
        return twoFactorUserAgent;
      }
    });
    
    return result;
    
  }


  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    return TwoFactorServerUtils.clone(this, CLONE_FIELDS);
  }


  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorUserAgent twoFactorUserAgent = (TwoFactorUserAgent)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(twoFactorUserAgent, this, DB_VERSION_FIELDS);
    
  }


  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    return DB_NEEDS_UPDATE_FIELDS;
  }


  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));
  
  }


  /**
   * delete this record
   * @param twoFactorDaoFactory is the factor to use
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorDaoFactory.getTwoFactorUserAgent().delete(TwoFactorUserAgent.this);
        testDeletes++;
        return null;
      }
    });
  
  }


  /**
   * store this object and audit
   * @param twoFactorDaoFactory
   * @param exceptionOnError
   * @return if changed, or null if problem
   */
  public Boolean store(final TwoFactorDaoFactory twoFactorDaoFactory, final boolean exceptionOnError) {
    
    if (StringUtils.isBlank(this.userAgent)) {
      throw new RuntimeException("userAgent is null");
    }
    
    //note, this is size 200, but with utf8 chars, maybe it is bigger in the db?
    this.userAgent = TwoFactorServerUtils.truncateAscii(this.userAgent, 200);
    
    TwoFactorUserAgent dbVersion = (TwoFactorUserAgent)this.dbVersion();

    if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, this)) {

      boolean success = twoFactorDaoFactory.getTwoFactorUserAgent().store(this, exceptionOnError);
      if (!success) {
        return null;
      }

      testInsertsAndUpdates++;
      this.dbVersionReset();
      return true;
    }

    return false;

  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorUserAgent.class);

  /**
   * calculate the browser fields from the user agent
   */
  public void calculateBrowserFields() {
    if (StringUtils.isBlank(this.userAgent)) { 
      return;
    }
    try {

      UserAgentUtils userAgentUtils = new UserAgentUtils();
      this.browser = userAgentUtils.browser(this.userAgent);
      this.operatingSystem = userAgentUtils.operatingSystem(this.userAgent);
      this.mobile = userAgentUtils.mobile(this.userAgent);
      
    } catch (Exception e) {

      //just ignore
      LOG.warn("Error looking up user agent: " + this.userAgent);
    }
  }
  
}
