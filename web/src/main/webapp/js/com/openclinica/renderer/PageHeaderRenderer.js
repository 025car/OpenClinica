function PageHeaderRenderer() {
  this.render = function(printTime, pageType, eventName,eventLocation) { 
	  var eventLocation= app_thisStudyEvent?app_thisStudyEvent["@OpenClinica:StudyEventLocation"]:"";
	  var eventEndDate =  app_thisStudyEvent?app_thisStudyEvent["@OpenClinica:EndDate"]:"";
    return RenderUtil.render(RenderUtil.get("print_page_header"), {
      printTime: printTime,
      pageType: pageType,
      studyName: app_studyName, 
      siteName: app_siteName, 
      protocolName: app_protocolName,
      eventName: eventName,
      collectSubjectDOB: app_collectSubjectDOB, 
      personIDRequired: app_personIDRequired,
      showPersonID: app_showPersonID,
      interviewerNameRequired: app_interviewerNameRequired,
      interviewDateRequired: app_interviewDateRequired,
      secondaryLabelViewable: app_secondaryLabelViewable,
      eventLocationRequired: app_eventLocationRequired,eventLocation:eventLocation
    });
  }
}