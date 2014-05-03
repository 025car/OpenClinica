package org.akaza.openclinica.service.crfdata;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanPropertyService{
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;

    private StudyEventDefinitionDao studyEventDefinitionDao;
   

	private StudyEventDao studyEventDAO;
    private ExpressionService expressionService;
 private StudySubjectDao studySubjectDao;
    
 private static final Logger LOGGER = LoggerFactory.getLogger(BeanPropertyService.class);
   

	public BeanPropertyService(DataSource ds) {
    	// itemsAlreadyShown = new ArrayList<Integer>();
        this.ds = ds;
    }


    /**
     * Complete adding to this Service to evaluate Event action
     * @param ruleActionBean
     * @param eow
     */
    public void runAction(RuleActionBean ruleActionBean,ExpressionBeanObjectWrapper eow){
    	boolean statusMatch = false;
        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
    	StudyEvent studyEvent = getStudyEventDAO().fetchByStudyEventDefOID(((EventActionBean)ruleActionBean).getOc_oid_reference(), eow.getStudySubjectBean().getId());
    	RuleActionRunBean runOnStatuses = ruleActionBean.getRuleActionRun();

    	if (studyEvent != null)
    	{
	    	switch (SubjectEventStatus.getByCode(studyEvent.getSubjectEventStatusId()))
	    	{
	    	case NOT_SCHEDULED:
	    		if (runOnStatuses.getNot_started()) statusMatch = true;
	    		break;
	    	case SCHEDULED:
	    		if (runOnStatuses.getScheduled()) statusMatch = true;
	    		break;
	    	case DATA_ENTRY_STARTED:
	    		if (runOnStatuses.getData_entry_started()) statusMatch = true;
	    		break;
	    	case COMPLETED:
	    		if (runOnStatuses.getComplete()) statusMatch = true;
	    		break;
	    	case SKIPPED:
	    		if (runOnStatuses.getSkipped()) statusMatch = true;
	    		break;
	    	case STOPPED:
	    		if (runOnStatuses.getStopped()) statusMatch = true;
	    		break;
	    	case SIGNED:
	    	case LOCKED:
	    	default:
	    		statusMatch = false;
	    		break;
	    	}
    	}
    	//Special case if destination study event doesn't exist yet, ie not scheduled.
    	else
    	{
    		if (runOnStatuses.getNot_started()) statusMatch = true;
    	}
    	
    	if (statusMatch)
    	{
	        for(PropertyBean propertyBean: ((EventActionBean) ruleActionBean).getProperties())
	        {
	            // This will execute the contents of <ValueExpression>SS.ENROLLMENT_DATE + 2</ValueExpression>
	        	LOGGER.debug("Values:expression??::"+propertyBean.getValueExpression().getValue());
	        	Object result = oep.parseAndEvaluateExpression(propertyBean.getValueExpression().getValue());
	            executeAction(result,propertyBean,eow,(EventActionBean)ruleActionBean);
	        }
    	}
    }

    
    
    private void executeAction(Object result,PropertyBean propertyBean,ExpressionBeanObjectWrapper eow,EventActionBean eventAction){
    	String oid = eventAction.getOc_oid_reference();
    	//int index = propertyBean.getOid().indexOf(".");
    	String eventOID = eventAction.getOc_oid_reference();
    	StudyEventDao studyEventdao = getStudyEventDAO();
        String property = propertyBean.getProperty();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StudyEventDefinition sDefBean =  getStudyEventDefinitionBean(eventOID);
        boolean updateFlag = true;
        
        if(oid.startsWith(ExpressionService.STUDY_EVENT_OID_START_KEY))
        {
        	StudyEvent studyEvent = getStudyEventDAO().fetchByStudyEventDefOID(oid, eow.getStudySubjectBean().getId());
        	if(studyEvent==null){
        		studyEvent = new StudyEvent();//the studyevent may not have been created.
            	StudySubject ss = getStudySubjectDao().findById(eow.getStudySubjectBean().getId());
            	StudyEventDefinition sed = getStudyEventDefinitionDao().findByColumnName(eventOID, "oc_oid");
            	studyEvent.setStudyEventDefinition(sed);
            	studyEvent.setStudySubject(ss);
            	studyEvent.setStatusId(1);
            	studyEvent.setSampleOrdinal(1);//TODO:change this to address repeating events.
            	studyEvent.setSubjectEventStatusId(new Integer(1));//The status is changed to started when it doesnt exist. In other cases, the status remains the same. The case of Signed and locked are prevented from validator and are not again checked here.
        	}
        	
        	try {
				studyEvent.setDateStart(df.parse((String) result));
			} catch (ParseException e) {
				e.printStackTrace();
				LOGGER.info(e.getMessage());
			}
        	
        	
        	
        	getStudyEventDAO().saveOrUpdate(studyEvent);
        	

        }
        
  }


  


	private StudyEventDefinition getStudyEventDefinitionBean(String eventOID) {
   return getStudyEventDefinitionDao().findByColumnName(eventOID, "oc_oid");
    	
	}


	public Boolean hasShowingDynGroupInSection(int sectionId, int crfVersionId, int eventCrfId) {
        return dynamicsItemGroupMetadataDao.hasShowingInSection(sectionId, crfVersionId, eventCrfId);
    }

    public Boolean hasShowingDynItemInSection(int sectionId, int crfVersionId, int eventCrfId) {
        return dynamicsItemFormMetadataDao.hasShowingInSection(sectionId, crfVersionId, eventCrfId);
    }

    public DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        return dynamicsItemFormMetadataDao;
    }

    public DynamicsItemGroupMetadataDao getDynamicsItemGroupMetadataDao() {
        return dynamicsItemGroupMetadataDao;
    }

    public void setDynamicsItemGroupMetadataDao(DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao) {
        this.dynamicsItemGroupMetadataDao = dynamicsItemGroupMetadataDao;
    }

    public void setDynamicsItemFormMetadataDao(DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao) {
        this.dynamicsItemFormMetadataDao = dynamicsItemFormMetadataDao;
    }

    public StudyEventDefinitionDao getStudyEventDefinitionDao() {
		return studyEventDefinitionDao;
	}


	public void setStudyEventDefinitionDao(
			StudyEventDefinitionDao studyEventDefinitionDao) {
		this.studyEventDefinitionDao = studyEventDefinitionDao;
	}


    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public StudyEventDao getStudyEventDAO() {
		return studyEventDAO;
	}


	public void setStudyEventDAO(StudyEventDao studyEventDAO) {
		this.studyEventDAO = studyEventDAO;
	}



	 public StudySubjectDao getStudySubjectDao() {
			return studySubjectDao;
		}


		public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
			this.studySubjectDao = studySubjectDao;
		}

	
}
