/**
 * @author mchyzer
 * $Id: UiMain.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.dojo.DojoComboDataResponse;
import org.openTwoFactor.server.dojo.DojoComboDataResponseItem;
import org.openTwoFactor.server.encryption.TwoFactorOath;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.ui.UiServiceLogicBase;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorOneTimePassRow;
import org.openTwoFactor.server.ui.beans.TwoFactorProfileContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorPassResult;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * UI main 
 */
public class UiMain extends UiServiceLogicBase {

  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void personPicker(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    personPickerHelper(httpServletRequest, httpServletResponse, false);
  }
    

  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   * @param allowInactives 
   */
  public static void personPickerHelper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, boolean allowInactives) {
    //{
    //  query: {name: "A*"},
    //  queryOptions: {ignoreCase: true},
    //  sort: [{attribute:"name", descending:false}],
    //    start: 0,
    //    count: 10
    //}
    
    //https://server.url/twoFactorMchyzer/twoFactorUi/app/UiMain.personPicker?name=ab*&start=0&count=Infinity

//    TwoFactorServerUtils.printToScreen("{\"label\":\"name\", \"identifier\":\"id\",\"items\":[{\"id\":\"10021368\",\"name\":\"Chris Hyzer (mchyzer, 10021368) (active) Staff - Astt And Information Security - Application Architect (also: Alumni)\"},{\"id\":\"10193029\",\"name\":\"Chyze-Whee Ang (angcw, 10193029) (active) Alumni\"}]}", "application/json", false, false);
         
    boolean filteringInactives = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.filteringInactives", false);
    
    String query = TwoFactorServerUtils.defaultString(httpServletRequest.getParameter("id"));
    
    boolean isLookup = true;
    
    if (StringUtils.isBlank(query)) {

      isLookup = false;
      
      query = StringUtils.trimToEmpty(httpServletRequest.getParameter("name"));

    }

    DojoComboDataResponse dojoComboDataResponse = null;

    //if there is no *, then looking for a specific name, return nothing since someone just typed something in and left...
    if (!query.contains("*")) {
      
      isLookup = true;
      
    }

    Set<Subject> subjects = new LinkedHashSet<Subject>();
    boolean enterMoreChars = false;
    
    if (isLookup) {

      Subject subject = SourceManager.getInstance()
          .getSource(TfSourceUtils.SOURCE_NAME).getSubjectByIdOrIdentifier(query, false);
        
      if (subject != null) {
        subjects.add(subject);
      }

    } else {
    
      //take out the asterisk
      query = StringUtils.replace(query, "*", "");
  
      //if its a blank query, then dont return anything...
      if (query.length() > 1) {
        subjects.addAll(TwoFactorServerUtils.nonNull(SourceManager.getInstance()
            .getSource(TfSourceUtils.SOURCE_NAME).searchPage(query).getResults()));
        
        //dont filter inactive if not need to
        if (filteringInactives && allowInactives) {
          
          String queryPrepend = 
            SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME).getInitParam("statusLabel") + "="
            + SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME).getInitParam("statusAllFromUser");
          
          
          query = queryPrepend + " " + query;
        }
        
      } else {
        enterMoreChars = true;
      }
    }

    if (enterMoreChars) {
      DojoComboDataResponseItem dojoComboDataResponseItem = new DojoComboDataResponseItem(null, 
          TextContainer.retrieveFromRequest().getText().get("comboNotEnoughChars"));
      dojoComboDataResponse = new DojoComboDataResponse(TwoFactorServerUtils.toList(dojoComboDataResponseItem));
      
    } else if (subjects.size() == 0) {
      dojoComboDataResponse = new DojoComboDataResponse();
    } else {
      
      List<DojoComboDataResponseItem> items = new ArrayList<DojoComboDataResponseItem>();

      //convert subject to item
      for (Subject subject : subjects) {
        
        //description could be null?
        String description = TfSourceUtils.subjectDescription(subject, null);
        
        DojoComboDataResponseItem item = new DojoComboDataResponseItem(subject.getId(), description);
        items.add(item);
        
      }
      
      dojoComboDataResponse = new DojoComboDataResponse(
        TwoFactorServerUtils.toArray(items, DojoComboDataResponseItem.class));

    }  

    String json = TwoFactorServerUtils.jsonConvertTo(dojoComboDataResponse, false);
    
    //write json to screen
    TwoFactorServerUtils.printToScreen(json, "application/json", false, false);
    
  }
  
  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void index(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
        
    showJsp("twoFactorIndex.jsp");
  }

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    //InetAddress ia = null; //InetAddress.getByAddress(new byte[] {(byte)130,91,(byte)219,(byte)176});
    ////ia = InetAddress.getByAddress(new byte[] {(byte)176, (byte)219, 91, (byte)130});
    //// or 
    //ia = InetAddress.getByName("130.91.219.176");
    //System.out.println(ia.getCanonicalHostName());
    

  }

  
  /**
   * show user audits
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void userAudits(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);

    userAuditsLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("userAudits.jsp");
  }

  /**
   * show recent history of the current user
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   */
  public void userAuditsLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
        final TwoFactorRequestContainer twoFactorRequestContainer,
        final String loggedInUser, final String ipAddress, 
        final String userAgent, final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    //generate the codes
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    auditHelper(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorUser, subjectSource);

  }

  /**
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param twoFactorUser
   * @param subjectSource 
   */
  public static void auditHelper(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      TwoFactorUser twoFactorUser, final Source subjectSource) {
    
    if (subjectSource != null) {
      
      //resolve subject
      Subject subject = subjectSource.getSubjectByIdOrIdentifier(twoFactorUser.getLoginid(), false);
      if (subject != null) {
        
        //try the description, then the name
        if (!StringUtils.isBlank(subject.getDescription())) {
          twoFactorRequestContainer.getTwoFactorAuditContainer().setUserString(subject.getDescription());
        } else if (!StringUtils.isBlank(subject.getName())) {
          twoFactorRequestContainer.getTwoFactorAuditContainer().setUserString(subject.getName());
        }
      }
      //default to the user id
      if (StringUtils.isBlank(twoFactorRequestContainer.getTwoFactorAuditContainer().getUserString())) {
        twoFactorRequestContainer.getTwoFactorAuditContainer().setUserString(twoFactorUser.getLoginid());
      }
    }

    
    //get the most recent 200 items, note a daemon might delete old records
    TfQueryOptions tfQueryOptions = new TfQueryOptions().paging(200, 1, true);
    tfQueryOptions.sortDesc("the_timestamp");

    List<TwoFactorAuditView> twoFactorAuditViews = twoFactorDaoFactory
      .getTwoFactorAudit().retrieveByUser(twoFactorUser.getUuid(), 
        tfQueryOptions);

    int count = tfQueryOptions.getQueryPaging().getTotalRecordCount();
    if (count < 0) {
      throw new RuntimeException("Why is there no count for audits retrieved???");
    }
    twoFactorRequestContainer.getTwoFactorAuditContainer().setTwoFactorAuditViewsTotalCount(count);
    twoFactorRequestContainer.getTwoFactorAuditContainer().setTwoFactorAuditViewsDisplayedCount(TwoFactorServerUtils.length(twoFactorAuditViews));
    
    twoFactorRequestContainer.getTwoFactorAuditContainer().setTwoFactorAuditViews(twoFactorAuditViews);
  }

  /**
   * show one time codes
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void showOneTimeCodes(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = showOneTimeCodesLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));

    if (success) {

      showJsp("showOneTimeCodes.jsp");

    } else {
    
      showJsp("twoFactorIndex.jsp");
    }
  }

  /**
   * show one time codes
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean showOneTimeCodesLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
        //generate the codes
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("codesErrorAlreadyOptedIn"));
          return false;
        }
        
        //see what the user already had
        if (twoFactorUser.getSeqPassIndexGivenToUser() == null || twoFactorUser.getSeqPassIndexGivenToUser() < 500000L) {
          //set to 0 so we can add one
          twoFactorUser.setSeqPassIndexGivenToUser(500000L);
        }
        
        //make sure sequential pass index exists
        if (twoFactorUser.getSequentialPassIndex() == null || twoFactorUser.getSequentialPassIndex() < 500001L) {
          twoFactorUser.setSequentialPassIndex(500001L);
        }
        
        //make the pass index to use equal to the one given to the user plus one
        if (twoFactorUser.getSeqPassIndexGivenToUser() > twoFactorUser.getSequentialPassIndex()) {
          twoFactorUser.setSequentialPassIndex(twoFactorUser.getSeqPassIndexGivenToUser() + 1);
        }
        
        int numberOfOneTimePassesShownOnScreen = TwoFactorServerConfig.retrieveConfig()
          .propertyValueInt("twoFactorServer.hotpSecretsShownOnScreen", 20);
        
        //subtract one since if you show one code, the last you gave to the user is that one...
        twoFactorUser.setSeqPassIndexGivenToUser((twoFactorUser.getSequentialPassIndex() + numberOfOneTimePassesShownOnScreen)-1);
        twoFactorUser.store(twoFactorDaoFactory);
        
        
        List<TwoFactorOneTimePassRow> passRows = new ArrayList<TwoFactorOneTimePassRow>();
        twoFactorRequestContainer.setOneTimePassRows(passRows);
    
        TwoFactorLogicInterface twoFactorLogicInterface = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
        
        Base32 base32 = new Base32();
        byte[] secret = base32.decode(twoFactorUser.getTwoFactorSecretUnencrypted());
        
        long firstNumberLabel = twoFactorUser.getSequentialPassIndex();
        
        if (firstNumberLabel >= 1000) {
          firstNumberLabel = firstNumberLabel % 1000;
        }
        
        for (int i=0;i<numberOfOneTimePassesShownOnScreen/2;i++) {
          TwoFactorOneTimePassRow twoFactorOneTimePassRow = new TwoFactorOneTimePassRow();
          passRows.add(twoFactorOneTimePassRow);
          
          {
            String oneTimePassCol1 = Integer.toString(twoFactorLogicInterface.hotpPassword(secret, twoFactorUser.getSequentialPassIndex()+i));
            oneTimePassCol1 = StringUtils.leftPad(oneTimePassCol1, 6, '0');
            oneTimePassCol1 = StringUtils.leftPad(Long.toString(firstNumberLabel + i), 3, " ") + ". " 
              + oneTimePassCol1;
            twoFactorOneTimePassRow.setOneTimePassCol1(oneTimePassCol1);
          }
          
          {
            String oneTimePassCol2 = Integer.toString(twoFactorLogicInterface.hotpPassword(secret, 
                (twoFactorUser.getSequentialPassIndex()+(numberOfOneTimePassesShownOnScreen/2))+i));
            oneTimePassCol2 = StringUtils.leftPad(oneTimePassCol2, 6, '0');
            oneTimePassCol2 = 
              StringUtils.leftPad(Long.toString(firstNumberLabel
                +(numberOfOneTimePassesShownOnScreen/2) + i), 3, " ") + ". " 
                + oneTimePassCol2;
            twoFactorOneTimePassRow.setOneTimePassCol2(oneTimePassCol2);
          }      
        }
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.GENERATE_PASSWORDS, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid());

        return true;
      }
    });
  }
  
  
  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optin(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);

    OptinView optinView = optinLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    showJsp(optinView.getJsp());
    
  }

  /**
   * optout of the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optout(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    optoutLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
    
    showJsp("twoFactorIndex.jsp");
  }

  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   * @return the view
   */
  public OptinView optinLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
    
    return optinSetup(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, 
        TwoFactorOath.twoFactorGenerateTwoFactorPass(), subjectSource);
  }
  
  /**
   * show qrCode image
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void qrCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
    
    int qrImageWidth = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.qrImageWidth", 400);
    
    File qrImageFile = TwoFactorServerUtils.tempFile(".gif", "qrCodes");
    
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    String twoFactorSecret = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
    String accountSuffix = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.accountSuffix");
    if (accountSuffix.startsWith("@")) {
      accountSuffix = TwoFactorServerUtils.prefixOrSuffix(accountSuffix, "@", false);
    }
    if (StringUtils.isBlank(twoFactorUserLoggedIn.getLoginid())) {
      throw new RuntimeException("Why is login blank??? " + twoFactorUserLoggedIn.getUuid());
    }
    //http://invariantproperties.com/2011/12/23/using-google-authenticator-totp-on-your-site/
    String uri = "otpauth://totp/" + twoFactorUserLoggedIn.getLoginid() + "@" + accountSuffix + "?secret=" + twoFactorSecret;
    TwoFactorServerConfig.retrieveConfig().twoFactorLogic().generateQrFile(uri, qrImageFile, qrImageWidth);

    TwoFactorServerUtils.sendFileToBrowser(qrImageFile.getAbsolutePath(), false, true, true, null, null, true);

  }

  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinTestSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");

    OptinTestSubmitView optinTestSubmitView = optinTestSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), twoFactorPass);

    showJsp(optinTestSubmitView.getJsp());

  }

  /**
   * When someone opts out their colleague
   * @param httpServletRequest 
   * @param httpServletResponse 
   */
  public void optOutColleague(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String userIdOperatingOn = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("userIdOperatingOn");

    optOutColleagueLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn);

    showJsp("helpColleague.jsp");
    
  }

  /**
   * When someone opts out their colleague
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   * @param userIdOperatingOn 
   */
  public void optOutColleagueLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        TwoFactorUser twoFactorUserGettingOptedOut = TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, userIdOperatingOn);
        
        //make sure they have allowed people to opt them out
        if (!twoFactorUserGettingOptedOut.isInvitedColleaguesWithinAllottedTime()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserDidntAllow"));
          return null;
        }
        
        //make sure the user has identified this user to opt them out
        if (!StringUtils.equals(twoFactorUserUsingApp.getUuid(), twoFactorUserGettingOptedOut.getColleagueUserUuid0())
            && !StringUtils.equals(twoFactorUserUsingApp.getUuid(), twoFactorUserGettingOptedOut.getColleagueUserUuid1())
            && !StringUtils.equals(twoFactorUserUsingApp.getUuid(), twoFactorUserGettingOptedOut.getColleagueUserUuid2())
            && !StringUtils.equals(twoFactorUserUsingApp.getUuid(), twoFactorUserGettingOptedOut.getColleagueUserUuid3())
            && !StringUtils.equals(twoFactorUserUsingApp.getUuid(), twoFactorUserGettingOptedOut.getColleagueUserUuid4()) ) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserNotFriend"));
          return null;
          
        }
        
        
        twoFactorUserGettingOptedOut.setTwoFactorSecretTemp(null);
        
        if (StringUtils.isBlank(twoFactorUserGettingOptedOut.getTwoFactorSecret()) || !twoFactorUserGettingOptedOut.isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendWarnNotOptedIn"));
          
        } else {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendSuccess"));

        }
        
        twoFactorUserGettingOptedOut.setTwoFactorSecret(null);
        
        twoFactorUserGettingOptedOut.setOptedIn(false);
        twoFactorUserGettingOptedOut.setSequentialPassIndex(1L);

        twoFactorUserGettingOptedOut.setDateInvitedColleagues(null);
        
        twoFactorUserGettingOptedOut.store(twoFactorDaoFactory);

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTED_OUT_A_COLLEAGUE, ipAddress, 
            userAgent, twoFactorUserUsingApp.getUuid(), twoFactorUserUsingApp.getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("helpFriendAuditDescriptionPrefix") 
              + " " + twoFactorUserGettingOptedOut.getLoginid());

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.COLLEAGUE_OPTED_ME_OUT, ipAddress, 
            userAgent, twoFactorUserGettingOptedOut.getUuid(), twoFactorUserUsingApp.getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("helpFriendAuditDescriptionForFriendPrefix")
              + " " + twoFactorUserUsingApp.getLoginid());

        return OptinTestSubmitView.optinSuccess;
      }
    });

    helpColleagueLogic(twoFactorDaoFactory, 
        twoFactorRequestContainer,
        loggedInUser);

  }

  /**
   * draw the screen to help a colleague
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void helpColleague(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    helpColleagueLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser);
    
    showJsp("helpColleague.jsp");
    
  }
  
  /**
   * logic to help a colleague opt out
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   */
  public void helpColleagueLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    List<TwoFactorUser> usersWhoPickedThisUserToOptOut = twoFactorDaoFactory.getTwoFactorUser().retrieveUsersWhoPickedThisUserToOptThemOut(twoFactorUser.getUuid());

    //TODO batch this up
    for (int i=0;i<TwoFactorServerUtils.length(usersWhoPickedThisUserToOptOut);i++) {
      TwoFactorUser current = usersWhoPickedThisUserToOptOut.get(i);
      current = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, current.getLoginid());
      usersWhoPickedThisUserToOptOut.set(i, current);
    }
    
    twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setColleaguesIdentifiedUser(usersWhoPickedThisUserToOptOut);
    
    twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setHasColleaguesIdentifiedUser(TwoFactorServerUtils.length(usersWhoPickedThisUserToOptOut) > 0);
    
  }
  
  /**
   * 
   */
  public static enum OptinTestSubmitView {
    
    /**
     */
    optin("optin.jsp"),
    
    /**
     */
    index("twoFactorIndex.jsp"),
    
    /**
     */
    optinSuccess("optinSuccess.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinTestSubmitView(String theJsp) {
      this.jsp = theJsp;
    }
    
    /**
     * 
     * @return jsp
     */
    public String getJsp() {
      return this.jsp;
    }
  }
  
  /**
   * 
   */
  public static enum OptinView {
    
    /**
     */
    optin("optin.jsp"),
    
    /**
     */
    profile("profile.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinView(String theJsp) {
      this.jsp = theJsp;
    }
    
    /**
     * 
     * @return jsp
     */
    public String getJsp() {
      return this.jsp;
    }
  }

  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param twoFactorPass 
   * @return error message if there is one and jsp
   */
  public OptinTestSubmitView optinTestSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String twoFactorPass) {
    
    return (OptinTestSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        String twoFactorSecret = twoFactorUser.getTwoFactorSecretTempUnencrypted();
        
        if (StringUtils.isBlank(twoFactorSecret)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSubmitErrorInconsistent"));
          return OptinTestSubmitView.index;
          
        }
        
        if (StringUtils.isBlank(twoFactorPass)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCodeRequired"));
          return OptinTestSubmitView.optin;
        }
        
        //no need to validate the password, the password checker will do that
        TwoFactorPassResult twoFactorPassResult = TwoFactorOath.twoFactorCheckPassword(
            twoFactorSecret, twoFactorPass, null, null, null, 0L, null);
        if (!twoFactorPassResult.isPasswordCorrect()) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCodeInvalid"));
          return OptinTestSubmitView.optin;
        }
        
        //set the object
        twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorUser.getTwoFactorSecretTempUnencrypted());
        twoFactorUser.setTwoFactorSecretTemp(null);
        twoFactorUser.setOptedIn(true);
        twoFactorUser.setSequentialPassIndex(null);
        twoFactorUser.setTokenIndex(0L);
        twoFactorUser.setLastTotpTimestampUsed(null);
        if (twoFactorPassResult.getNextHotpIndex() != null) {
          twoFactorUser.setSequentialPassIndex(twoFactorPassResult.getNextHotpIndex());
        }
        if (twoFactorPassResult.getLastTotp30TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp30TimestampUsed());
        }
        if (twoFactorPassResult.getLastTotp60TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp60TimestampUsed());
        }
        if (twoFactorPassResult.getNextTokenIndex() != null) {
          twoFactorUser.setTokenIndex(twoFactorPassResult.getNextTokenIndex());
        }
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid());
        
        return OptinTestSubmitView.optinSuccess;
      }
    });
  }

  /**
   * optout of two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   */
  public void optoutLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setTwoFactorSecretTemp(null);
        String resultMessage = null;
        
        if (StringUtils.isBlank(twoFactorUser.getTwoFactorSecret())) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optoutErrorNotOptedIn"));
          
        } else {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optoutSuccessMessage"));

        }
        
        twoFactorUser.setTwoFactorSecret(null);
        
        twoFactorUser.setOptedIn(false);
        twoFactorUser.setSequentialPassIndex(1L);
        twoFactorUser.setDateInvitedColleagues(null);

        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTOUT_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid());
        
        return resultMessage;
      }
    });
  }


  /**
   * untrust browsers
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void untrustBrowsers(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    untrustBrowsersLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
    
    showJsp("twoFactorIndex.jsp");
  }
  
  /**
   * untrust browsers of two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   */
  public void untrustBrowsersLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setTwoFactorSecretTemp(null);
        String resultMessage = null;
        
        if (StringUtils.isBlank(twoFactorUser.getTwoFactorSecret())) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("untrustBrowserErrorNotOptedIn"));
          
        } else {
          
          List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
            .retrieveTrustedByUserUuid(twoFactorUser.getUuid());
          
          //untrust browsers since opting in, dont want orphans from last time
          for (TwoFactorBrowser twoFactorBrowser : twoFactorBrowsers) {
            twoFactorBrowser.setWhenTrusted(0);
            twoFactorBrowser.setTrustedBrowser(false);
            twoFactorBrowser.store(twoFactorDaoFactory);
          }

          twoFactorRequestContainer.getTwoFactorUntrustBrowserContainer().setNumberOfBrowsers(TwoFactorServerUtils.length(twoFactorBrowsers));
          String error = TextContainer.retrieveFromRequest().getText().get("untrustBrowserSuccess");
          twoFactorRequestContainer.setError(error);

        }

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.UNTRUST_BROWSERS, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid());
        
        return resultMessage;
      }
    });
  }

  /**
   * show the profile screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void profile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);
    
    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("profile.jsp");
  }

  /**
   * show the profile screen readonly
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void profileView(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);

    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("profileView.jsp");
  }

  /**
   * show the profile screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   */
  public void profileLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    twoFactorRequestContainer.getTwoFactorProfileContainer().setTwoFactorUserOperatingOn(twoFactorUser);
    
    TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();

    twoFactorProfileContainer.setEmail0(twoFactorUser.getEmail0());

    if (StringUtils.isBlank(twoFactorUser.getEmail0()) && subjectSource != null) {
      
      //resolve subject
      Subject subject = subjectSource.getSubjectByIdOrIdentifier(loggedInUser, false);
      if (subject != null) {
        twoFactorProfileContainer.setEmail0(subject.getAttributeValueSingleValued("email"));
        
      }
    }
    
    twoFactorProfileContainer.setPhone0(twoFactorUser.getPhone0());
    twoFactorProfileContainer.setPhone1(twoFactorUser.getPhone1());
    twoFactorProfileContainer.setPhone2(twoFactorUser.getPhone2());

    twoFactorProfileContainer.setPhoneText0((twoFactorUser.getPhoneIsText0() != null && twoFactorUser.getPhoneIsText0()) ? "true" : "");
    twoFactorProfileContainer.setPhoneText1((twoFactorUser.getPhoneIsText1() != null && twoFactorUser.getPhoneIsText1()) ? "true" : "");
    twoFactorProfileContainer.setPhoneText2((twoFactorUser.getPhoneIsText2() != null && twoFactorUser.getPhoneIsText2()) ? "true" : "");

    twoFactorProfileContainer.setPhoneVoice0((twoFactorUser.getPhoneIsVoice0() != null && twoFactorUser.getPhoneIsVoice0()) ? "true" : "");
    twoFactorProfileContainer.setPhoneVoice1((twoFactorUser.getPhoneIsVoice1() != null && twoFactorUser.getPhoneIsVoice1()) ? "true" : "");
    twoFactorProfileContainer.setPhoneVoice2((twoFactorUser.getPhoneIsVoice2() != null && twoFactorUser.getPhoneIsVoice2()) ? "true" : "");
    
    {
      String colleagueUserUuid0 = twoFactorUser.getColleagueUserUuid0();
      if (!StringUtils.isBlank(colleagueUserUuid0)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid0);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin0(colleagueUser.getLoginid());
          
          Subject subject = subjectSource.getSubjectByIdOrIdentifier(colleagueUser.getLoginid(), false);
          twoFactorProfileContainer.setColleagueDescription0(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));

        }
      }
    }

    {
      String colleagueUserUuid1 = twoFactorUser.getColleagueUserUuid1();
      if (!StringUtils.isBlank(colleagueUserUuid1)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid1);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin1(colleagueUser.getLoginid());

          Subject subject = subjectSource.getSubjectByIdOrIdentifier(colleagueUser.getLoginid(), false);
          twoFactorProfileContainer.setColleagueDescription1(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));

        }
      }
    }

    {
      String colleagueUserUuid2 = twoFactorUser.getColleagueUserUuid2();
      if (!StringUtils.isBlank(colleagueUserUuid2)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid2);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin2(colleagueUser.getLoginid());
          
          Subject subject = subjectSource.getSubjectByIdOrIdentifier(colleagueUser.getLoginid(), false);
          twoFactorProfileContainer.setColleagueDescription2(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));

        }
      }
    }

    {
      String colleagueUserUuid3 = twoFactorUser.getColleagueUserUuid3();
      if (!StringUtils.isBlank(colleagueUserUuid3)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid3);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin3(colleagueUser.getLoginid());
          
          Subject subject = subjectSource.getSubjectByIdOrIdentifier(colleagueUser.getLoginid(), false);
          twoFactorProfileContainer.setColleagueDescription3(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));

        }
      }
    }
    {
      String colleagueUserUuid4 = twoFactorUser.getColleagueUserUuid4();
      if (!StringUtils.isBlank(colleagueUserUuid4)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid4);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin4(colleagueUser.getLoginid());
          
          Subject subject = subjectSource.getSubjectByIdOrIdentifier(colleagueUser.getLoginid(), false);
          twoFactorProfileContainer.setColleagueDescription4(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));

        }
      }
    }



  }

  /**
   * profile submit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void profileSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String email0 = null;
    
    if (twoFactorRequestContainer.isEditableEmail()) {
      email0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("email0");
    }
    String colleagueLogin0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin0Name");
    String colleagueLogin1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin1Name");
    String colleagueLogin2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin2Name");
    String colleagueLogin3 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin3Name");
    String colleagueLogin4 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin4Name");
    String phone0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone0");
    //this is "on" if submitted, or null if not
    String phoneVoice0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice0");
    String phoneText0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText0");
    String phone1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone1");
    String phoneVoice1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice1");
    String phoneText1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText1");
    String phone2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone2");
    String phoneVoice2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice2");
    String phoneText2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText2");
    
    boolean profileForOptin = TwoFactorServerUtils.booleanValue(
        TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("profileForOptin"), false);
    
    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);
    
    boolean success = profileSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), email0, colleagueLogin0, colleagueLogin1,
        colleagueLogin2, colleagueLogin3, colleagueLogin4, phone0, phoneVoice0, phoneText0, 
        phone1, phoneVoice1, phoneText1, phone2, phoneVoice2, phoneText2, subjectSource, profileForOptin);
  
    if (success) {
      
      if (profileForOptin) {
        optin(httpServletRequest, httpServletResponse);
        return;
      }
      showJsp("profileView.jsp");
    } else {
    
      showJsp("profile.jsp");
    }
  }

  /**
   * 
   * @param colleagueLogin0 
   * @param colleagueLogin1 
   * @param colleagueLogin2 
   * @param colleagueLogin3 
   * @param colleagueLogin4 
   * @param phone0 
   * @param phone1 
   * @param phone2 
   * @return the count of lifelines
   */
  private static int lifelineCount(String colleagueLogin0, String colleagueLogin1,
      String colleagueLogin2, String colleagueLogin3, String colleagueLogin4,
      String phone0, String phone1, String phone2) {
    int lifelineCount = 0;
    lifelineCount += StringUtils.isBlank(colleagueLogin0) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin1) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin2) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin3) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin4) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(phone0) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(phone1) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(phone2) ? 0 : 1;
    
    return lifelineCount;
  }
  
  /**
   * profile submit
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param email0 
   * @param colleagueLogin0 
   * @param colleagueLogin1 
   * @param colleagueLogin2 
   * @param colleagueLogin3 
   * @param colleagueLogin4 
   * @param phone0 
   * @param phoneVoice0 
   * @param phoneText0 
   * @param phone1 
   * @param phoneVoice1 
   * @param phoneText1 
   * @param phone2 
   * @param phoneVoice2 
   * @param phoneText2 
   * @param subjectSource 
   * @param profileForOptin 
   * @return true if ok, false if not
   */
  public boolean profileSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String email0, final String colleagueLogin0, final String colleagueLogin1,
      final String colleagueLogin2, final String colleagueLogin3, final String colleagueLogin4, 
      final String phone0, final String phoneVoice0, final String phoneText0, 
      final String phone1, final String phoneVoice1, final String phoneText1, final String phone2, 
      final String phoneVoice2, final String phoneText2, final Source subjectSource, final boolean profileForOptin) {
    
    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
        String localEmail0 = email0;
        
        //generate the codes
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();
        
        twoFactorProfileContainer.setEmail0(localEmail0);
        twoFactorProfileContainer.setProfileForOptin(profileForOptin);
        
        if (StringUtils.isBlank(twoFactorProfileContainer.getEmail0()) && subjectSource != null) {
          
          //resolve subject
          Subject subject = subjectSource.getSubjectByIdOrIdentifier(loggedInUser, false);
          if (subject != null) {
            
            twoFactorProfileContainer.setEmail0(subject.getAttributeValueSingleValued("email"));
            localEmail0 = twoFactorProfileContainer.getEmail0();
          }
        }

        
        twoFactorProfileContainer.setColleagueLogin0(colleagueLogin0);
        twoFactorProfileContainer.setColleagueLogin1(colleagueLogin1);
        twoFactorProfileContainer.setColleagueLogin2(colleagueLogin2);
        twoFactorProfileContainer.setColleagueLogin3(colleagueLogin3);
        twoFactorProfileContainer.setColleagueLogin4(colleagueLogin4);
        twoFactorProfileContainer.setPhone0(phone0);
        twoFactorProfileContainer.setPhoneText0(phoneText0);
        twoFactorProfileContainer.setPhoneVoice0(phoneVoice0);
        twoFactorProfileContainer.setPhone1(phone1);
        twoFactorProfileContainer.setPhoneText1(phoneText1);
        twoFactorProfileContainer.setPhoneVoice1(phoneVoice1);
        twoFactorProfileContainer.setPhone2(phone2);
        twoFactorProfileContainer.setPhoneText2(phoneText2);
        twoFactorProfileContainer.setPhoneVoice2(phoneVoice2);
        
        String errorMessage = null;
        
        //if email address isnt editable, then validate it
        if (twoFactorRequestContainer.isEditableEmail()) {
          errorMessage = validateEmail(localEmail0);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone0, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone1"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone1, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone2"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone2, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone3"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone0, phoneText0, phoneVoice0, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone1"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone1, phoneText1, phoneVoice1, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone2"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone2, phoneText2, phoneVoice2, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone3"));
        }
        
        if (lifelineCount(colleagueLogin0, colleagueLogin1, colleagueLogin2, colleagueLogin3, colleagueLogin4,
            phone0, phone1, phone2) < 2) {
          errorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorNotEnoughLifelines");
        }
        if (!StringUtils.isBlank(errorMessage) ) {
          twoFactorRequestContainer.setError(errorMessage);
          return false;
        }
        
        twoFactorUser.setEmail0(localEmail0);

        twoFactorUser.setPhone0(phone0);
        twoFactorUser.setPhone1(phone1);
        twoFactorUser.setPhone2(phone2);
        twoFactorUser.setPhoneIsText0(StringUtils.equals(phoneText0, "true") ? true : false);
        twoFactorUser.setPhoneIsText1(StringUtils.equals(phoneText1, "true") ? true : false);
        twoFactorUser.setPhoneIsText2(StringUtils.equals(phoneText2, "true") ? true : false);
        twoFactorUser.setPhoneIsVoice0(StringUtils.equals(phoneVoice0, "true") ? true : false);
        twoFactorUser.setPhoneIsVoice1(StringUtils.equals(phoneVoice1, "true") ? true : false);
        twoFactorUser.setPhoneIsVoice2(StringUtils.equals(phoneVoice2, "true") ? true : false);
        
        if (StringUtils.isBlank(colleagueLogin0)) {
          twoFactorUser.setColleagueUserUuid0(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin0);
          twoFactorUser.setColleagueUserUuid0(colleagueUser.getUuid());
        }
        
        if (StringUtils.isBlank(colleagueLogin1)) {
          twoFactorUser.setColleagueUserUuid1(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin1);
          twoFactorUser.setColleagueUserUuid1(colleagueUser.getUuid());
        }
        
        if (StringUtils.isBlank(colleagueLogin2)) {
          twoFactorUser.setColleagueUserUuid2(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin2);
          twoFactorUser.setColleagueUserUuid2(colleagueUser.getUuid());
        }
        
        if (StringUtils.isBlank(colleagueLogin3)) {
          twoFactorUser.setColleagueUserUuid3(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin3);
          twoFactorUser.setColleagueUserUuid3(colleagueUser.getUuid());
        }
        
        if (StringUtils.isBlank(colleagueLogin4)) {
          twoFactorUser.setColleagueUserUuid4(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin4);
          twoFactorUser.setColleagueUserUuid4(colleagueUser.getUuid());
        }
        
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.EDIT_PROFILE, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid());
  
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("profileSuccessMessage"));
  
        profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
            loggedInUser, ipAddress, 
            userAgent, subjectSource);

        return true;
      }
    });
  }
  
  /**
   * validate an email
   * @param email
   * @return the error or null
   */
  public static String validateEmail(String email) {
    
    if (StringUtils.isBlank(email)) {
      return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailRequired");
    }
    {
      int atIndex = email.indexOf('@');
      if (atIndex <= 0 || email.endsWith("@")) {
        return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailInvalid");
      }
      int dotIndex = email.indexOf('@', atIndex);
      if (dotIndex <= 0) {
        return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailInvalid");
      }
    }
    return null;
  }
  
  /**
   * pattern of phone number validator
   */
  private static Pattern phonePattern = Pattern.compile("^[0-9+()\\- \\.]+$");
  
  /**
   * validate a phone
   * @param phone
   * @param label
   * @return the error or null
   */
  public static String validatePhone(String phone, String label) {
    if (StringUtils.isBlank(phone)) {
      return null;
    }
    //see if invalid chars
    Matcher matcher = phonePattern.matcher(phone);
    if (!matcher.matches()) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorPhoneInvalidChars");
    }
    //lets count the number of digits... must be at least 10 (note, intl number might have fewer... hmm)
    int digitCount = 0;
    for (int i=0;i<phone.length();i++) {
      if (Character.isDigit(phone.charAt(i))) {
        digitCount++;
      }
    }
    if (digitCount < 10) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorPhoneTooShort");
    }
    return null;
  }

  /**
   * validate phone type checkboxes
   * @param phone
   * @param phoneText
   * @param phoneVoice
   * @param label
   * @return the error or null if no errors
   */
  public static String validatePhoneType(String phone, String phoneText, String phoneVoice, String label) {
    if (!StringUtils.isBlank(phone) && StringUtils.isBlank(phoneText) && StringUtils.isBlank(phoneVoice)) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorTextOrVoiceRequired");
    }
    if (StringUtils.isBlank(phone) && (!StringUtils.isBlank(phoneText) || !StringUtils.isBlank(phoneVoice))) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorNumberRequiredIfChecked");
    }
    return null;
  }
  
  /**
   * custom secret optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinCustom(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String twoFactorCustomCode = httpServletRequest.getParameter("twoFactorCustomCode");
    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);

    OptinView optinView = optinCustomLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), twoFactorCustomCode, subjectSource);

    showJsp(optinView.getJsp());

  }

  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param twoFactorCustomCode
   * @param subjectSource 
   * @return the view to go to
   */
  public OptinView optinCustomLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, String twoFactorCustomCode, final Source subjectSource) {

    //validation
    if (StringUtils.isBlank(twoFactorCustomCode)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretRequired"));
      return OptinView.optin;
    }

    //strip whitespace and validate chars
    boolean isBase32 = false;
    {
      twoFactorCustomCode = twoFactorCustomCode.toLowerCase();
      StringBuilder newString = new StringBuilder();
      for (int i=0;i<twoFactorCustomCode.length(); i++) {
        
        char theChar = twoFactorCustomCode.charAt(i);
        
        if (Character.isWhitespace(theChar)) {
          continue;
        }
        
        //Note: these arent the exact base32 rules, but close enough...
        if ((theChar >= '0' && theChar <= '9') || (theChar >= 'a' && theChar <= 'f')) {
          newString.append(theChar);
          continue;
        }
        if (theChar >= 'g' && theChar <= 'z') {
          newString.append(theChar);
          isBase32 = true;
          continue;
        }
        //bad char
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretInvalid"));
        return OptinView.optin;
        
      }
      twoFactorCustomCode = newString.toString();
    }
    
    if (twoFactorCustomCode.length() < 6) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretNotLongEnough"));
      return OptinView.optin;
    }
    
    if (!isBase32) {
      //convert to base 32:
      byte[] plainText = null;
      try {
        plainText = Hex.decodeHex(twoFactorCustomCode.toCharArray());
      } catch (DecoderException de) {
        throw new RuntimeException("Bad hex in custom secret");
      }

      Base32 codec = new Base32();
      twoFactorCustomCode = codec.encodeAsString(plainText);
      
      //strip whitespace again since base32 puts it in there
      {
        twoFactorCustomCode = twoFactorCustomCode.toLowerCase();
        StringBuilder newString = new StringBuilder();
        for (int i=0;i<twoFactorCustomCode.length(); i++) {
          
          char theChar = twoFactorCustomCode.charAt(i);
          
          if (Character.isWhitespace(theChar)) {
            continue;
          }
          newString.append(theChar);
          
        }
        twoFactorCustomCode = newString.toString();
      }

    }
    
    return optinSetup(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress,
        userAgent, twoFactorCustomCode, subjectSource);
    
  }

  /**
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param twoFactorCode
   * @param subjectSource 
   * @return which view to go to
   * 
   */
  private OptinView optinSetup(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, final String userAgent,
      final String twoFactorCode, final Source subjectSource) {
    return (OptinView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorRequestContainer.getTwoFactorProfileContainer().setProfileForOptin(true);
        
        boolean hasEmail = false;
        
        if (twoFactorRequestContainer.isEditableEmail()) {
          hasEmail = !StringUtils.isBlank(twoFactorUser.getEmail0());
        } else {

          //if not editable, get from the subject source
          Subject subject = subjectSource.getSubjectByIdOrIdentifier(loggedInUser, false);
          if (subject != null) {
            hasEmail = !StringUtils.isBlank(subject.getAttributeValueSingleValued("email"));
          }
        }
        //lets validate the profile
        if (!hasEmail) {
          profileLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorEmailRequired"));
          return OptinView.profile;
        }
        
        if (lifelineCount(twoFactorUser.getColleagueUserUuid0(), twoFactorUser.getColleagueUserUuid1(),
            twoFactorUser.getColleagueUserUuid2(), twoFactorUser.getColleagueUserUuid3(), 
            twoFactorUser.getColleagueUserUuid4(), twoFactorUser.getPhone0(), twoFactorUser.getPhone1(),
            twoFactorUser.getPhone2()) < 2) {
          profileLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
          return OptinView.profile;    
        }

        String pass = twoFactorCode.toUpperCase();
        twoFactorUser.setTwoFactorSecretTempUnencrypted(pass);
        twoFactorUser.setOptedIn(false);
        twoFactorUser.setSeqPassIndexGivenToUser(null);
        twoFactorUser.setSequentialPassIndex(null);
        twoFactorUser.setTokenIndex(0L);
        twoFactorUser.store(twoFactorDaoFactory);
  
        List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser().retrieveTrustedByUserUuid(twoFactorUser.getUuid());
  
        //untrust browsers since opting in, dont want orphans from last time
        for (TwoFactorBrowser twoFactorBrowser : twoFactorBrowsers) {
          twoFactorBrowser.setTrustedBrowser(false);
          twoFactorBrowser.setWhenTrusted(0);
          twoFactorBrowser.store(twoFactorDaoFactory);
        }
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR_STEP1, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid());
        
        return OptinView.optin;
      }
    });
  }

  /**
   * test ui stuff
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void uiTestIndex(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    showJsp("uiTest.jsp");

  }
  
  /**
   * test ui stuff
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void uiTestPersonPickerCombo(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    personPicker(httpServletRequest, httpServletResponse);

  }
  
  
}
  