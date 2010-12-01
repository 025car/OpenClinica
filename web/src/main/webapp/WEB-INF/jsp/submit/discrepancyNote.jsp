<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<jsp:useBean scope='request' id='resolutionStatus' class='java.lang.String' />
<jsp:useBean scope='request' id='resolutionStatus2' class='java.lang.String' />
<jsp:useBean scope='request' id='whichResStatus' class='java.lang.String' />
<jsp:useBean scope='session' id='boxDNMap'  class="java.util.HashMap"/>
<jsp:useBean scope='session' id='boxToShow'  class="java.lang.String"/>


<script language="JavaScript" src="includes/global_functions_javascript.js"></script>
<script language="JavaScript">
<!--
function setStatus(typeId) {
	objtr1=document.getElementById('res1');
	objtr2=document.getElementById('resStatusId');
	if (typeId == 2|| typeId ==4) {//annotation or reason for change

	  	objtr2.value=5;
	  	objtr2.disabled = true;
	} else if (typeId == 3) { //query
		objtr2.value=1; //new
		objtr2.disabled = false;
	} else {
  		if (objtr2.value ==5 && objtr2.disabled) {
   			objtr2.value=1;
  		}

  		objtr2.disabled = false;

	}
}

function setResStatus(resStatusId, destinationUserId) {
	objtr1=document.getElementById('resStatusId');
	objtr2=document.getElementById('userAccountId');
	objtr3=document.getElementById('xxx');
	objtr4=document.getElementById('typeId');

	if (resStatusId == 3 || resStatusId == 4) { //Resolutiuon proposed or Closed
		// objtr2.disabled = false;
		objtr2.value = destinationUserId;
		// disable?
		objtr2.disabled = false;
		objtr3.removeAttribute('disabled');
		//objtr3.value = destinationUserId;
		// disable?
		//objtr3.disabled = false;
	}

	if (resStatusId == 5 && objtr4.value == 4) { // Not applicable AND Reason for Change
		objtr1.disabled = true;
	}
}

function showOnly(strLeftNavRowElementName){
    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "all";
    }
}

function hide(strLeftNavRowElementName){
	
	        var objLeftNavRowElement;
	
	        objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
	        if (objLeftNavRowElement != null) {
	            if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
	            objLeftNavRowElement.display = "none";
	        }
	    }
	    
function setElements(typeId, user1, user2) {
	setStatus(typeId);
	if(typeId == 3) {//query
		leftnavExpand(user1);
		leftnavExpand(user2);	
	}else {
		hide(user1);
		hide(user2);
	}
}

function setValue(elementName, value) {
	var element = MM_findObj(elementName);
	if(element != null) {
		element.value = value;
	}	
}

function timeOutWindow(close,duration) {
	if(close == 'true') {
		window.setTimeout('window.close()', duration);	
	}
}
//-->
</script>

<c:set var="parentId" value="${param.parentId}"/>
<c:set var="boxId" value="${param.boxId}"/>
<c:set var="displayAll" value="none" />
<c:set var="discrepancyNote" value="${boxDNMap[parentId]}"/>
<c:forEach var="boxDN" items="${boxDNMap}">
	<c:if test="${parentId==boxDN.key}">
		<c:set var="discrepancyNote" value="${boxDNMap[boxDN.key]}"/>
	</c:if>
</c:forEach>
<c:if test="${parentId==boxToShow}">
	<c:set var="displayAll" value="all"/>
</c:if>
<form name="oneDNForm" method="POST" action="CreateOneDiscrepancyNote" id="form${boxId}"> 
<jsp:include page="../include/showSubmitted.jsp" />
<table border="0" cellpadding="0" cellspacing="0" style="float:left;display:<c:out value="${displayAll}"/>" id="${boxId}">
	<input type="hidden" name="parentId" value="${parentId}"/>
	<input type="hidden" name="viewDNLink${parentId}" value="${viewDNLink}"/>
	<input type="hidden" name="id" value="${param.entityId}"/>
	<input type="hidden" name="name" value="${param.entityType}"/>
	<input type="hidden" name="field" value="${param.field}"/>
	<input type="hidden" name="column" value="${param.column}"/>
	<input type="hidden" name="close${parentId}" value=""/>

	<td valign="top">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	<table border="0">
		<div style="float: right;">
			<p><a href="#" onclick="javascript:leftnavExpand('<c:out value="${boxId}"/>');"><img name="close_box" alt="close_box" src="images/bt_Remove.gif"></a></p>
		</div>
		<tr valign="top">
		<td class="table_cell_noborder"><fmt:message key="description" bundle="${resword}"/>:</td>
		<td class="table_cell_noborder">
		<div class="formfieldL_BG"><input type="text" name="description${parentId}" value="<c:out value="${discrepancyNote.description}"/>" class="formfieldL"></div>
		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description${parentId}"/></jsp:include>
		</td>
		<td class="table_cell_noborder">
		<table>
			<td  class="table_cell_noborder"><fmt:message key="type" bundle="${resword}"/>:</td>
			<td class="table_cell_noborder"><div class="formfieldL_BG">
			<c:choose>
			<c:when test="${parentId > 0}">
				<input type="hidden" name="typeId${parentId}" value="${param.typeId}"/>
				<select name="pTypeId<c:out value="${parentId}"/>" id="pTypeId<c:out value="${parentId}"/>" class="formfieldL" disabled>
					<option value="<c:out value="${param.typeId}"/>" selected><c:out value="${param.typeName}"/>
				</select>
			</c:when>
			<c:otherwise>
				<c:set var="typeId1" value="${discrepancyNote.discrepancyNoteTypeId}"/>
				<c:choose>
				<c:when test="${whichResStatus == 2}">
					<select name="typeId${parentId}" id="typeId${parentId}" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2');">
						<c:forEach var="type" items="${discrepancyTypes2}">
						<c:choose>
						<c:when test="${typeId1 == type.id}">
						 	<c:choose>
						    <c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
									<option value="<c:out value="${type.id}"/>" disabled="true" selected ><c:out value="${type.name}"/>
						    </c:when>
						    <c:otherwise>
						   		<option value="<c:out value="${type.id}"/>" selected ><c:out value="${type.name}"/>
						    </c:otherwise>
						    </c:choose>
						 </c:when>
						 <c:otherwise>
							<c:choose>
							<c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
								<option value="<c:out value="${type.id}"/>" disabled="true"><c:out value="${type.name}"/>
							</c:when>
							<c:otherwise>
								<option value="<c:out value="${type.id}"/>"><c:out value="${type.name}"/>
							</c:otherwise>
							</c:choose>
						 </c:otherwise>
						</c:choose>
						</c:forEach>
					</select>
				</c:when>
				<c:otherwise>
					<select name="typeId${parentId}" id="typeId${parentId}" class="formfieldL" onchange ="javascript:setElements(this.options[selectedIndex].value, 'user1', 'user2');">
						<c:forEach var="type" items="${discrepancyTypes}">
						<c:choose>
						<c:when test="${typeId1 == type.id}">
						 	<c:choose>
						    <c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
									<option value="<c:out value="${type.id}"/>" disabled="true" selected ><c:out value="${type.name}"/>
						    </c:when>
						    <c:otherwise>
						   		<option value="<c:out value="${type.id}"/>" selected ><c:out value="${type.name}"/>
						    </c:otherwise>
						    </c:choose>
						 </c:when>
						 <c:otherwise>
							<c:choose>
							<c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
								<option value="<c:out value="${type.id}"/>" disabled="true"><c:out value="${type.name}"/>
							</c:when>
							<c:otherwise>
								<option value="<c:out value="${type.id}"/>"><c:out value="${type.name}"/>
							</c:otherwise>
							</c:choose>
						 </c:otherwise>
						</c:choose>
						</c:forEach>
					</select>
				</c:otherwise>
				</c:choose>
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="typeId${parentId}"/></jsp:include>
			</c:otherwise>
			</c:choose>
			</div></td>
		</table>
		</td>
		</tr>
		
		<tr valign="top">
		<td  class="table_cell_noborder"><fmt:message key="detailed_note" bundle="${resword}"/>:</td>
		<td class="table_cell_noborder">
		<div class="formtextareaL4_BG">
		  <textarea name="detailedDes${parentId}" rows="4" cols="50" class="formtextareaL4"><c:out value="${discrepancyNote.detailedNotes}"/></textarea>
		</div>
		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="detailedDes${parentId}"/></jsp:include>
		</td>
		
		<td class="table_cell_noborder">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr valign="top" id="res1">
		    <td  class="table_cell_noborder"><fmt:message key="resolution_status" bundle="${resword}"/>:</td>
		    <td class="table_cell_noborder"><div class="formfieldL_BG">
			<c:set var="resStatusId1" value="${discrepancyNote.resolutionStatusId}"/>
		    <select name="resStatusId${parentId}" id="resStatusId${parentId}" class="formfieldL" onchange="javascript:setResStatus(3, <c:out value="${discrepancyNote.ownerId}"/>);">
				<c:choose>
				<c:when test="${whichResStatus == 2 && param.typeId == 3 && parentId > 0}">
					<c:set var="resStatuses" value="${resolutionStatuses2}"/>
				</c:when>
				<c:otherwise>
					<c:set var="resStatuses" value="${resolutionStatuses}"/>
				</c:otherwise>
				</c:choose>
				<c:forEach var="status" items="${resStatuses}">
					<c:choose>
					<c:when test="${resStatusId1 == status.id}">
					   <option value="<c:out value="${status.id}"/>" selected ><c:out value="${status.name}"/>
					</c:when>
					<c:otherwise>
					   <option value="<c:out value="${status.id}"/>" ><c:out value="${status.name}"/>
					</c:otherwise>
					</c:choose>
				</c:forEach>
			</select></div>
		    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="resStatusId${parentId}"/></jsp:include></td>
		</tr>
		
		
		<c:choose>
		<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
        	<tr valign="top" id="user1" style="display:none">
		</c:when>
		<c:otherwise>
			<tr valign="top">
      	</c:otherwise>
		</c:choose>
		<c:if test="${discrepancyNote.discrepancyNoteTypeId != 1 || (discrepancyNote.discrepancyNoteTypeId==1 && discrepancyNote.parentDnId>0)}">
			<td class="table_cell_noborder"><fmt:message key="assign_to_user" bundle="${resword}"/>:</td>
			<td class="table_cell_noborder"><div class="formfieldL_BG">
			<c:choose>
			<c:when test='${discrepancyNote.assignedUserId != ""}'>
				<c:set var="userAccountId1" value="${discrepancyNote.assignedUserId}"/>
			</c:when>
			<c:otherwise>
				<c:set var="userAccountId1" value="0"/>
			</c:otherwise>
			</c:choose>
			<span id="xxx" disabled>
			<select name="userAccountId${parentId}" id="userAccountId${parentId}" class="formfieldL" >
		  		<c:forEach var="user" items="${userAccounts}">
		   		<c:choose>
		     	<c:when test="${userAccountId1 == user.userAccountId}">
		       		<option value="<c:out value="${user.userAccountId}"/>" selected><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
		     	</c:when>
		     	<c:otherwise>
		       		<option value="<c:out value="${user.userAccountId}"/>"><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
		     	</c:otherwise>
		   		</c:choose>
		 		</c:forEach>
			</select>
			</span>
			<input type="hidden" name="userAccountId" value="<c:out value="${userAccountId1}"/>"/>		
			<c:out value="${userAccountId}"/>
			</div>
		  	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="userAccountId${parentId}"/></jsp:include></td>
			</tr>
		
			<c:choose>
			<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
				<tr valign="top" id="user2" style="display:none">
			</c:when>
			<c:otherwise>
				<tr valign="top">
			</c:otherwise>
			</c:choose>
			<%-- should be an option for checked, unchecked, disabled--%>
			<td><input name="sendEmail${parentId}" value="1" type="checkbox"/></td>
			<td  class="table_cell_noborder"><fmt:message key="email_assigned_user" bundle="${resword}"/></td>		
			</tr>
		</c:if>
		
		<tr>
		<c:set var= "noteEntityType" value="${discrepancyNote.entityType}"/>
		<c:if test="${enterData == '1' || canMonitor == '1' || noteEntityType != 'itemData' }">
			<td><input type="submit" name="Submit${parentId}" value="Submit" class="button_medium" style="width:80px"></td>
			<td><input type="submit" name="SubmitExit${parentId}" value="Submit & Exit" class="button_medium" style="width:90px" onclick="javascript:setTrue('close<c:out value="${parentId}"/>','true');"></td>
		</c:if>
		</tr>
		
		<c:if test="${parentId==0}">
			<tr valign="top">
				<td class="table_cell_left">
                	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="newChildAdded${parentId}"/></jsp:include>	
                </td>
			</tr>
		</c:if>
	</table>
    </div>
	</div></div></div></div></div></div></div>
	</td>
</table>
</form>
<div style="clear:both;"></div>
