/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.logic.CreateStudyService;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

public class CreateStudyEndpoint extends AbstractCabigDomEndpoint {
    public CreateStudyService studyService;

    public CreateStudyEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {

        super(dataSource, messages, coreResources);

        studyService = new CreateStudyService();
    }

    private StudyBean createStudyParameters(StudyBean newStudy) {
        StudyParameterValueBean spv = new StudyParameterValueBean();
        spv.setStudyId(newStudy.getId());
        spv.setParameter("collectDob");
        spv.setValue(newStudy.getStudyParameterConfig().getCollectDob());
        getStudyParamValueDao().create(spv);

        spv.setParameter("discrepancyManagement");
        spv.setValue(newStudy.getStudyParameterConfig().getDiscrepancyManagement());
        getStudyParamValueDao().create(spv);

        spv.setParameter("genderRequired");
        spv.setValue(newStudy.getStudyParameterConfig().getGenderRequired());
        getStudyParamValueDao().create(spv);

        spv.setParameter("subjectPersonIdRequired");
        spv.setValue(newStudy.getStudyParameterConfig().getSubjectPersonIdRequired());
        getStudyParamValueDao().create(spv);

        spv.setParameter("interviewerNameRequired");
        spv.setValue(newStudy.getStudyParameterConfig().getInterviewerNameRequired());
        getStudyParamValueDao().create(spv);

        spv.setParameter("interviewerNameDefault");
        spv.setValue(newStudy.getStudyParameterConfig().getInterviewerNameDefault());
        getStudyParamValueDao().create(spv);

        spv.setParameter("interviewerNameEditable");
        spv.setValue(newStudy.getStudyParameterConfig().getInterviewerNameEditable());
        getStudyParamValueDao().create(spv);

        spv.setParameter("interviewDateRequired");
        spv.setValue(newStudy.getStudyParameterConfig().getInterviewDateRequired());
        getStudyParamValueDao().create(spv);

        spv.setParameter("interviewDateDefault");
        spv.setValue(newStudy.getStudyParameterConfig().getInterviewDateDefault());
        getStudyParamValueDao().create(spv);

        spv.setParameter("interviewDateEditable");
        spv.setValue(newStudy.getStudyParameterConfig().getInterviewDateEditable());
        getStudyParamValueDao().create(spv);

        spv.setParameter("subjectIdGeneration");
        spv.setValue(newStudy.getStudyParameterConfig().getSubjectIdGeneration());
        getStudyParamValueDao().create(spv);

        spv.setParameter("subjectIdPrefixSuffix");
        spv.setValue(newStudy.getStudyParameterConfig().getSubjectIdPrefixSuffix());
        getStudyParamValueDao().create(spv);

        spv.setParameter("personIdShownOnCRF");
        spv.setValue(newStudy.getStudyParameterConfig().getPersonIdShownOnCRF());
        getStudyParamValueDao().create(spv);

        spv.setParameter("secondaryLabelViewable");
        spv.setValue(newStudy.getStudyParameterConfig().getSecondaryLabelViewable());
        getStudyParamValueDao().create(spv);

        spv.setParameter("adminForcedReasonForChange");
        spv.setValue(newStudy.getStudyParameterConfig().getAdminForcedReasonForChange());
        getStudyParamValueDao().create(spv);

        logger.info("study parameters created done");
        return newStudy;
    }

    // private StudyBean generateStudyParameters(StudyBean newStudy) {
    // // this.getStudyParamValueDao().findParameterByHandle(handle);
    // newStudy.getStudyParameterConfig().setCollectDob(getStudyParamValueDao().findParameterByHandle("collectDob").getDefaultValue());
    // newStudy.getStudyParameterConfig().setDiscrepancyManagement(getStudyParamValueDao().findParameterByHandle("discrepancyManagement").getDefaultValue());
    // newStudy.getStudyParameterConfig().setGenderRequired(getStudyParamValueDao().findParameterByHandle("genderRequired").getDefaultValue());
    //
    // newStudy.getStudyParameterConfig().setInterviewerNameRequired(
    // getStudyParamValueDao().findParameterByHandle("interviewerNameRequired").getDefaultValue());
    // newStudy.getStudyParameterConfig().setInterviewerNameDefault(getStudyParamValueDao().findParameterByHandle("interviewerNameDefault").getDefaultValue());
    // newStudy.getStudyParameterConfig().setInterviewDateEditable(getStudyParamValueDao().findParameterByHandle("interviewDateEditable").getDefaultValue());
    // newStudy.getStudyParameterConfig().setInterviewDateRequired(getStudyParamValueDao().findParameterByHandle("interviewDateRequired").getDefaultValue());
    // newStudy.getStudyParameterConfig().setInterviewerNameEditable(
    // getStudyParamValueDao().findParameterByHandle("interviewerNameEditable").getDefaultValue());
    // newStudy.getStudyParameterConfig().setInterviewDateDefault(getStudyParamValueDao().findParameterByHandle("interviewDateDefault").getDefaultValue());
    //
    // newStudy.getStudyParameterConfig().setSubjectIdGeneration("non-editable");
    // newStudy.getStudyParameterConfig().setSubjectPersonIdRequired(
    // getStudyParamValueDao().findParameterByHandle("subjectPersonIdRequired").getDefaultValue());
    // newStudy.getStudyParameterConfig().setSubjectIdPrefixSuffix(getStudyParamValueDao().findParameterByHandle("subjectIdPrefixSuffix").getDefaultValue());
    // newStudy.getStudyParameterConfig().setPersonIdShownOnCRF(getStudyParamValueDao().findParameterByHandle("personIdShownOnCRF").getDefaultValue());
    // newStudy.getStudyParameterConfig().setSecondaryLabelViewable(getStudyParamValueDao().findParameterByHandle("secondaryLabelViewable").getDefaultValue());
    // newStudy.getStudyParameterConfig().setAdminForcedReasonForChange(
    // getStudyParamValueDao().findParameterByHandle("adminForcedReasonForChange").getDefaultValue());
    // return newStudy;
    // }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        System.out.println("Request text create study ");
        StudyBean studyBean = new StudyBean();
        studyBean.setIdentifier("null");
        try {
            NodeList nlist = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studyProtocol");
            // this.logNodeList(nlist);
            for (int i = 0; i < nlist.getLength(); i++) {

                Node study = nlist.item(i);
                studyBean = studyService.generateStudyBean(getUserAccount(), study);
                // note: this returns null if there is nothing in the db. not that cool. tbh
                StudyBean testStudyBean = getStudyDao().findByUniqueIdentifier(studyBean.getIdentifier());
                boolean updateMe = false;
                int testStudyBeanId = testStudyBean != null ? testStudyBean.getId() : 0;
                // create all sites, moved up here to help with updates, tbh
                ArrayList<StudyBean> sites = studyService.generateSites(getUserAccount(), studyBean, study);
                // have to check site identifiers to protect uniqueness here, tbh
                ArrayList<StudyBean> testSites = new ArrayList<StudyBean>();
                for (StudyBean site : sites) {
                    StudyBean testSite = getStudyDao().findByUniqueIdentifier(site.getIdentifier());
                    if ((testSite != null) && (testSite.getParentStudyId() != testStudyBeanId)) {
                        throw new CCBusinessFaultException("The site with the identifier " + site.getIdentifier()
                            + " already exists and has a different relationship in the database.", "CC10110");

                    } else {
                        site.setId(testSite.getId());
                        // add it to a new list
                        testSites.add(site);
                    }
                }
                if (testStudyBean != null) {
                    // we need to check if it's identical. if it is, refresh all the info
                    // ArrayList<StudyBean> testSites = (ArrayList<StudyBean>) getStudyDao().findAllByParent(studyBean.getId());
                    // for (StudyBean site : sites) {
                    // StudyBean testSite = getStudyDao().findByUniqueIdentifier(site.getIdentifier());
                    //                        
                    // }
                    if (!studyBean.getIdentifier().equals(testStudyBean.getIdentifier())) {
                        // if not. AND if SITES are not the same, throw an error
                        throw new CCBusinessFaultException("The study with the identifier " + studyBean.getIdentifier()
                            + " already exists in the database.  Please use another identfier.", "CC10110");
                    } else {
                        updateMe = true;
                        studyBean.setUpdater(getUserAccount());
                        studyBean.setUpdatedDate(new Date(System.currentTimeMillis()));
                        studyBean.setId(testStudyBean.getId());
                        // set status here, as in the register subject?
                        sites = testSites;
                        // set the sites above, since we are updating and not creating
                    }
                }
                // but what if we rolled back? and what if we are updating?
                StudyConfigService configService = new StudyConfigService(dataSource);
                // studyBean = this.generateStudyParameters(studyBean);
                studyBean = configService.setParametersForStudy(studyBean);
                studyBean.getStudyParameterConfig().setSubjectIdGeneration("auto non-editable");
                if (!updateMe) {
                    studyBean = (StudyBean) getStudyDao().create(studyBean);
                } else {
                    studyBean = (StudyBean) getStudyDao().update(studyBean);
                }
                studyBean = this.createStudyParameters(studyBean);

                if (!updateMe) {
                    for (StudyBean site : sites) {
                        // what about site params?
                        site = configService.setParametersForSite(site);
                        site.getStudyParameterConfig().setSubjectIdGeneration("auto non-editable");
                        // what about site updates?
                        site.setParentStudyId(studyBean.getId());
                        System.out.println("set parent study: " + studyBean.getId());
                        site = (StudyBean) getStudyDao().create(site);
                        site = this.createStudyParameters(site);
                    }
                } else {
                    // do we update sites?
                    for (StudyBean site : sites) {
                        site.setStatus(Status.AVAILABLE);
                        site.setUpdatedDate(new Date(System.currentTimeMillis()));
                        site.setUpdater(getUserAccount());
                        site = (StudyBean) getStudyDao().update(site);
                    }
                }

            }
            return mapCreateStudyConfirmation(studyBean.getIdentifier());
        } catch (Exception npe) {
            npe.printStackTrace();
            // TODO figure out exception and send response
            if (npe.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + npe.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) npe;
                return mapStudyErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + npe.getClass().getName());
                return mapStudyErrorConfirmation(npe.getMessage());
            }
        }
    }

}
