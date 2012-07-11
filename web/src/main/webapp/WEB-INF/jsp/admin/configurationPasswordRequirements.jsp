<%@ page contentType="text/html; charset=UTF-8"
         import="javax.servlet.http.HttpServletRequest,
                 java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='presetValues' class='java.util.HashMap' />

<h1><span class="title_manage"><fmt:message key="configure_password_requirements" bundle="${resword}"/></span></h1>

<form action="ConfigurePasswordRequirements" method="post">
<jsp:include page="../include/showSubmitted.jsp" />

<div style="width: 400px">

<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


		<!-- Table Contents -->

<%! String checked(HttpServletRequest request, String attrName) {
      Map formValues = (Map) request.getAttribute("presetValues");
      Boolean attr = (Boolean) formValues.get(attrName);
      if (attr == null || !attr.booleanValue())
        return "";
      return "checked=\'checked\'";
    } %>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <td class="formlabel">
      <label for="pwd.chars.min"><fmt:message bundle="${resword}" key="pwd_reqs_min_length" /></label></td>
    <td>
      <div class="formfieldM_BG">
        <input class="formfieldM"
               id="pwd.chars.min"
               name="pwd.chars.min"
               type="text"
               value="${presetValues['pwd.chars.min']}" /></div></td>
    <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="pwd.chars.min" /></jsp:include></td>
  </tr>
  <tr>
    <td class="formlabel"><label for="pwd.chars.max"><fmt:message bundle="${resword}" key="pwd_reqs_max_length" /></label></td>
    <td>
      <div class="formfieldM_BG">
        <input class="formfieldM"
               id="pwd.chars.max"
               name="pwd.chars.max"
               type="text"
               value="${presetValues['pwd.chars.max']}" /></div></td>
    <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="pwd.chars.max" /></jsp:include></td>
  </tr>
  <tr>
    <td class="formlabel">
      <div><fmt:message bundle="${resword}" key="pwd_reqs_must_contain" /></div>
    </td>
    <td>
      <div>
        <input type="checkbox"
               id="pwd.chars.case.lower"
               name="pwd.chars.case.lower"
               value="true"
               <%= checked(request, "pwd.chars.case.lower") %> />
        <label for="pwd.chars.case.lower"><fmt:message bundle="${resword}" key="pwd_reqs_lower_case" /></label>
      </div>
      <div>
        <input type="checkbox"
               id="pwd.chars.case.upper"
               name="pwd.chars.case.upper"
               value="true"
               <%= checked(request, "pwd.chars.case.upper") %> />
        <label for="pwd.chars.case.upper"><fmt:message bundle="${resword}" key="pwd_reqs_upper_case" /></label>
      </div>
      <div>
        <input type="checkbox"
               id="pwd.chars.digits"
               name="pwd.chars.digits"
               value="true"
               <%= checked(request, "pwd.chars.digits") %> />
        <label for="pwd.chars.digits"><fmt:message bundle="${resword}" key="pwd_reqs_digits" /></label>
        </div>
      <div>
        <input type="checkbox"
               id="pwd.chars.specials"
               name="pwd.chars.specials"
               value="true"
               <%= checked(request, "pwd.chars.specials") %> />
        <label for="pwd.chars.specials"><fmt:message bundle="${resword}" key="pwd_reqs_special_chars" /></label>
      </div>
  </td></tr>
  <tr>
    <td class="formlabel"><fmt:message bundle="${resword}" key="pwd_reqs_allow_reuse" /></td>
    <td>
      <input type="radio"
             id="pwd_allow_reuse_yes"
             name="pwd.allow.reuse"
             value="true"
             <%= checked(request, "pwd.allow.reuse") %> />
      <label for="pwd_allow_reuse_yes"><fmt:message key="yes" bundle="${resword}" /></label>
      <input type="radio"
             id="pwd_allow_reuse_no"
             name="pwd.allow.reuse"
             value="false"
             <% Map formValues = (Map) request.getAttribute("presetValues");
                Boolean attr   = (Boolean) formValues.get("pwd.allow.reuse"); %>
             <%= attr == null || !attr.booleanValue() ? "checked=\'checked\'" : "" %> />
      <label for="pwd_allow_reuse_no" ><fmt:message key="no"  bundle="${resword}" /></label>
  </td></tr>
  <tr>
    <td class="formlabel"><label for="pwd.history.size"><fmt:message bundle="${resword}" key="pwd_reqs_history_size" /></label></td>
    <td>
      <div class="formfieldM_BG">
        <input class="formfieldM"
               id="pwd.history.size"
               name="pwd.history.size"
               type="text"
               value="${presetValues['pwd.history.size']}" /></div>
    <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="pwd.history.size" /></jsp:include></td>
  </tr>
</table>

	</div>

	</div></div></div></div></div></div></div></div>

	</div>

<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium">
<input type="button" onclick="confirmCancel('ListUserAccounts');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

</form>

<jsp:include page="../include/footer.jsp"/>
