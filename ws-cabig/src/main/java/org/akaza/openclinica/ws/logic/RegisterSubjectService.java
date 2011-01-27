package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCDataValidationFaultException;

import org.w3c.dom.Node;

import java.lang.CharSequence;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

public class RegisterSubjectService {
    
    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    
    public RegisterSubjectService() {
        
    }
    
    public RegisterSubjectBean generateSubjectBean(UserAccountBean user, Node subject, StudyDAO studyDao) throws Exception {
        RegisterSubjectBean subjectBean = new RegisterSubjectBean(user);
        DomParsingService xmlService = new DomParsingService();
        String subjectDOB = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "birthDate", "value");
        String subjectGender = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "sexCode", "code");
        String identifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "identifier", "extension");
        String studyIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studyIdentifier", "extension");
        String studySiteIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studySiteIdentifier", "extension");
        subjectBean.setSiteUniqueIdentifier(studySiteIdentifier);
        subjectBean.setStudyUniqueIdentifier(studyIdentifier);
        // lookup study here, get parameters to affect birthday
        StudyBean studyBean = new StudyBean();
        StudyBean siteBean = new StudyBean();
        
        if (subjectBean.getSiteUniqueIdentifier() != null) {
            siteBean = studyDao.findByUniqueIdentifier(subjectBean.getSiteUniqueIdentifier());
        } 
        studyBean = studyDao.findByUniqueIdentifier(subjectBean.getStudyUniqueIdentifier());
        
        // dry
        if (studyBean.getId() <= 0) {
            // if no study exists with that name, there is an error
            throw new CCBusinessFaultException("No study exists with that name, please review your information and re-submit the request.");
        }
        if (siteBean.getId() > 0) {
            // if there is a site bean, the study bean should be its parent, otherwise there is an error
            if ((siteBean.getParentStudyId() != studyBean.getId()) && (siteBean.getParentStudyId() != 0)) {
                throw new CCBusinessFaultException("Your parent and child study relationship is mismatched." + 
                        "  Please enter correct study and site information.");
            }
            studyBean = siteBean;
        }
        // dry
        
        List<StudyParamsConfig> parentConfigs = studyBean.getStudyParameters();
        for (StudyParamsConfig scg : parentConfigs) {
            System.out.println(scg.getParameter().getName() + " -> " + scg.getValue().getName() + " : " + scg.getValue().getValue());
        }
        subjectBean.setStudyBean(studyBean);
        subjectBean.setUniqueIdentifier(identifier);
        // throw an error if we dont get male or female as an answer
        if (!"male".equals(subjectGender.toLowerCase()) && ! "female".equals(subjectGender.toLowerCase())) {
            throw new CCDataValidationFaultException("Problem parsing sex, it should be either 'Male' or 'Female'.");
        }
        if ("Male".equals(subjectGender) || "male".equals(subjectGender)) {
            subjectBean.setGender("m");
        } else {
            subjectBean.setGender("f");
        }
        // no dases in dates?
        if (subjectDOB.contains("-")) {
            throw new CCDataValidationFaultException("Problem parsing date. Please remove all dashes and re-submit your data.");
        }
        SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
        // figure out the study params here; date only? no date?
        try {
            Date dateOfBirth = local_df.parse(subjectDOB);
            subjectBean.setDateOfBirth(dateOfBirth);
        } catch (ParseException pe) {
            // throw the data fault exception
            throw new CCDataValidationFaultException("Problem parsing date, it should be in YYYYMMDD format.");
        }
        return subjectBean;
    }
    
    public RegisterSubjectBean attachStudyIdentifiers(RegisterSubjectBean rsbean, Node milestone) throws CCDataValidationFaultException {
        DomParsingService xmlService = new DomParsingService();
        // <ns2:informedConsentDate value="20080101"/>
        // <ns2:registrationDate xsi:type="ns1:TS" value="20080825"/>
        // <ns2:registrationSiteIdentifier extension
//        String consentDateStr = xmlService.getElementValue(milestone, 
//                CONNECTOR_NAMESPACE_V1, "informedConsentDate", "value");
        String registrationDateStr = xmlService.getElementValue(milestone, 
                CONNECTOR_NAMESPACE_V1, "registrationDate", "value");
        String registrationSiteIdentifier = xmlService.getElementValue(milestone, 
                CONNECTOR_NAMESPACE_V1, "registrationSiteIdentifier", "extension");
        
        SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date enrollmentDate = local_df.parse(registrationDateStr);
            rsbean.setEnrollmentDate(enrollmentDate);
        } catch (ParseException pe) {
            // throw the data fault exception
            throw new CCDataValidationFaultException("Problem parsing date, it should be in YYYYMMDD format.");
        }
        rsbean.setStudySubjectLabel(registrationSiteIdentifier);
        return rsbean;
    }
    
    public SubjectBean generateSubjectBean(RegisterSubjectBean rsbean) {
        SubjectBean sbean = new SubjectBean();
        sbean.setStatus(Status.AVAILABLE);
        if (rsbean.getDateOfBirth() != null) {
            sbean.setDateOfBirth(rsbean.getDateOfBirth());
            sbean.setDobCollected(true);
        } else {
            sbean.setDobCollected(false);
        }
        sbean.setCreatedDate(new Date(System.currentTimeMillis()));
        char gender = rsbean.getGender().charAt(0);
        sbean.setGender(gender);
        sbean.setLabel(rsbean.getUniqueIdentifier());
        sbean.setName(rsbean.getUniqueIdentifier());
        sbean.setOwner(rsbean.getUser());
        sbean.setStudyIdentifier(rsbean.getStudyUniqueIdentifier());
        sbean.setUniqueIdentifier(rsbean.getUniqueIdentifier());
        return sbean;
    }
    
    public StudySubjectBean generateStudySubjectBean(RegisterSubjectBean subjectBean, SubjectBean finalSubjectBean, StudyBean studyBean) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        studySubjectBean.setEnrollmentDate(subjectBean.getEnrollmentDate());
        studySubjectBean.setStatus(Status.AVAILABLE);
        studySubjectBean.setLabel(subjectBean.getStudySubjectLabel());
        studySubjectBean.setSubjectId(finalSubjectBean.getId());
        studySubjectBean.setStudyId(studyBean.getId());
        // studySubjectBean.setSecondaryLabel(subjectBean.getStudySubjectLabel());
        studySubjectBean.setSecondaryLabel("");
        studySubjectBean.setOwner(subjectBean.getUser());
        return studySubjectBean;
    }
    
    public boolean isSubjectIdentical(RegisterSubjectBean registerBean, SubjectBean subjectBean) {
        char gender = registerBean.getGender().charAt(0);
        if (subjectBean.getGender() != gender) {
            System.out.println("gender fail");
            return false;
        }
        // is below necessary?
//        if (!subjectBean.getLabel().equals(registerBean.getUniqueIdentifier())) {
//            System.out.println("label fail");
//            return false;
//        }
        if (!subjectBean.getUniqueIdentifier().equals(registerBean.getUniqueIdentifier())) {
            System.out.println("ident fail");
            return false;
        }
        if (!subjectBean.getDateOfBirth().equals(registerBean.getDateOfBirth())) {
            System.out.println("dob fail");
            return false;
        }
        return true;
    }
    
    public boolean isStudySubjectIdentical(RegisterSubjectBean subjectBean, SubjectBean finalSubjectBean, StudySubjectBean studySubjectBean, StudyBean studyBean) {
        if (!studySubjectBean.getEnrollmentDate().equals(subjectBean.getEnrollmentDate())) {
            System.out.println("enroll fail");
            return false;
        }
        if (studySubjectBean.getSubjectId() != finalSubjectBean.getId()) {
            System.out.println("subj id fail");
            return false;
        }
        if (!studySubjectBean.getLabel().equals(subjectBean.getStudySubjectLabel())) {
            System.out.println("ss label fail");
            return false;
        }
        if (studySubjectBean.getStudyId() != studyBean.getId()) {
            System.out.println("study fail");
            return false;
        }
        return true;
    }

}
