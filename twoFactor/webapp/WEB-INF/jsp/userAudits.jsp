<%@ include file="../assetsJsp/commonTop.jsp"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['auditsTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <div id="headerDiv">
    <div class="alignleft"><h1 style="display: inline;">${textContainer.text['pageHeader'] }</h1></div>
    <div class="alignright">
      <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
        <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
      </c:if>    
    </div> 
    <div class="clearboth"></div> 
  </div> 
  <br />
  <b>${textContainer.text['auditsSubheader']}</b>
  <br /><br />
  <div class="paragraphs">
    ${textContainer.text['auditsNamePrefix']}
    ${fn:escapeXml(twoFactorRequestContainer.twoFactorAuditContainer.userString)}${textContainer.text['auditsNameSuffix']}
    <br />
    ${textContainer.text['auditsNameParagraph2']}
  </div>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

  <%@ include file="../assetsJsp/auditsInclude.jsp"%>
  
  <br /><br />
  
  <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
    <div class="logoutBottom" style="font-size: smaller">
      <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
      &nbsp;&nbsp;      
    </div>
  </c:if>    
  
  <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline; font-size: smaller">
    <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfLinkButton"
    type="submit" />
  </form>
  
  <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

