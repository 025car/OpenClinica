var INITIALIZED = false;

var app_maxPixelHeight = 610;
var app_maxLandscapePixelHeight = 400;
var app_maxPixelWidth = 850;
var app_maxLandscapePixelWidth = 1350;
var app_itemWidth = 250;
var app_repeatingItemWidth = 250;
var app_repeatingHorizItemWidth = 400;
var JSON_INDENT_LEVEL = 2;
var app_logLevel = util_logInfo;
var app_crfJson;
var app_odmRenderer;
var app_basicDefinitions;
var app_codeLists;
var app_multiSelectLists;
var app_itemGroupDefs;
var app_itemGroupMap;
var app_itemDefs;
var app_formDefs;
var app_studyEventDefs;
var app_eventName = undefined;
var app_eventLocation = undefined;
var app_crfHeader;
var app_crfPageNumber = 0;
var app_templateNames = 
['print_page_header','print_form_def','print_item_def','print_repeating_item_group',
 'print_repeating_item','print_repeating_item_horiz'];
var renderMode;
var app_collectSubjectDOB;
var app_personIDRequired;
var app_showPersonID;
var app_interviewerNameRequired;
var app_interviewDateRequired;
var app_secondaryLabelViewable;
var app_secondaryLabel;
var app_eventLocationRequired;
var app_studyDetails = undefined;
var app_studyEventCoverPageType = "study_event_cover_page_type";
var app_studyCoverPageType = "study_cover_page_type";
var app_studyContentPageType = "study_content_page_type";

debug("console debugging enabled.", util_logDebug);

/***********      @JQUERY INIT    *******************/
$(document).ready(function() {
  if (INITIALIZED == false) {
    INITIALIZED = true;
    RenderUtil.loadTemplates(app_templateNames, function() {
      getPrintableContent();
    });
  }
});
  
  
function getPrintableContent() {
    $('.spinner').css({display: "block"});
  $.get(app_contextPath + '/rest/metadata/json/view/' + app_studyOID + '/*/' + app_formVersionOID, {}, function(data) {
    $('.spinner').css({display: "none"});
    app_pagesArray = new Array();
    setRenderMode();
    app_odmRenderer = new ODMRenderer(data);
    var renderString = app_odmRenderer.renderPrintableStudy(renderMode);
    $('body').html(renderString);
   });
}

function setRenderMode() {
  renderMode = 'UNPOPULATED_FORM_CRF';
  if (app_studyOID != "*" && app_eventOID == "*" && app_formVersionOID == "*") {
    renderMode = 'UNPOPULATED_STUDY_CRFS';
  }
  else if (app_eventOID != "*" && app_formVersionOID == "*") {
    renderMode = 'UNPOPULATED_EVENT_CRFS';
  }
  else if (app_studyOID == "*" && app_eventOID == "*" && app_formVersionOID != "*") {
    renderMode = 'UNPOPULATED_GLOBAL_CRF';
  }
}
