/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.QueryStore;
import org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesDaoImpl extends NamedParameterJdbcDaoSupport implements ViewNotesDao {

    private static final Logger LOG = LoggerFactory.getLogger(ViewNotesDaoImpl.class);

    private static final String QUERYSTORE_FILE = "viewnotes";

    private QueryStore queryStore;

    private static final RowMapper<DiscrepancyNoteBean> DISCREPANCY_NOTE_ROW_MAPPER =
            new RowMapper<DiscrepancyNoteBean>() {

        public DiscrepancyNoteBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiscrepancyNoteBean b = new DiscrepancyNoteBean();
            b.setId(rs.getInt("discrepancy_note_id"));
            b.setStudyId(rs.getInt("study_id"));
            StudySubjectBean studySubjectBean = new StudySubjectBean();
            studySubjectBean.setId(b.getStudyId());
            studySubjectBean.setLabel(rs.getString("label"));
            b.setStudySub(studySubjectBean);
            b.setDiscrepancyNoteTypeId(rs.getInt("discrepancy_note_type_id"));
            b.setDisType(DiscrepancyNoteType.get(b.getDiscrepancyNoteTypeId()));
            b.setResolutionStatusId(rs.getInt("resolution_status_id"));
            b.setResStatus(ResolutionStatus.get(b.getResolutionStatusId()));
            b.setSiteId(rs.getString("site_id"));
            b.setCreatedDate(rs.getDate("date_created"));
            b.setUpdatedDate(rs.getDate("date_updated"));
            b.setDays(rs.getInt("days"));
            b.setAge(rs.getInt("age"));
            b.setEventName(rs.getString("event_name"));
            b.setEventStart(rs.getDate("date_start"));
            b.setCrfName(rs.getString("crf_name"));
            int statusId = rs.getInt("status_id");
            if (statusId != 0) {
                b.setCrfStatus(DataEntryStage.get(statusId).getName());
            }
            b.setEntityName(rs.getString("entity_name"));
            b.setEntityValue(rs.getString("value"));
            b.setEntityType(rs.getString("entity_type"));
            b.setDescription(rs.getString("description"));
            b.setDetailedNotes(rs.getString("detailed_notes"));
            b.setNumChildren(rs.getInt("total_notes"));

            String userName = rs.getString("user_name");
            if (!StringUtils.isEmpty(userName)) {
                UserAccountBean userBean = new UserAccountBean();
                userBean.setName(userName);
                userBean.setFirstName(rs.getString("first_name"));
                userBean.setLastName(rs.getString("last_name"));
                b.setAssignedUser(userBean);
            }
            String ownerUserName = rs.getString("owner_user_name");
            if (!StringUtils.isEmpty(ownerUserName)) {
                UserAccountBean userBean = new UserAccountBean();
                userBean.setName(ownerUserName);
                userBean.setFirstName(rs.getString("owner_first_name"));
                userBean.setLastName(rs.getString("owner_last_name"));
                b.setOwner(userBean);
            }
            return b;
        }
    };

    public List<DiscrepancyNoteBean> findAllDiscrepancyNotes(StudyBean currentStudy, ViewNotesFilterCriteria filter,
            ViewNotesSortCriteria sort) {
        Map<String, Object> arguments = listNotesArguments(currentStudy);
        List<DiscrepancyNoteBean> result = getNamedParameterJdbcTemplate().query(listNotesSql(filter, arguments),
                arguments,
                DISCREPANCY_NOTE_ROW_MAPPER);
        return result;
    }

    protected String listNotesSql(ViewNotesFilterCriteria filter, Map<String, Object> arguments) {
        List<String> terms = new ArrayList<String>();
        terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.main"));

        // Append query filters
        for (String filterKey : filter.getFilters().keySet()) {
            String filterQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter." + filterKey);
            terms.add(filterQuery);
            arguments.put(filterKey, filter.getFilters().get(filterKey));
        }

        // Limit number of results (pagination)
        terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.limit"));

        String result = StringUtils.join(terms, ' ');
        LOG.debug("SQL: " + result);
        return result;
    }

    protected Map<String, Object> listNotesArguments(StudyBean currentStudy) {
        Map<String,Object> arguments = new HashMap<String, Object>();
        arguments.put("studyId", currentStudy.getId());
        arguments.put("parentStudyId", currentStudy.getParentStudyId());
        arguments.put("limit", 50);
        return arguments;
    }

    public QueryStore getQueryStore() {
        return queryStore;
    }

    public void setQueryStore(QueryStore queryStore) {
        this.queryStore = queryStore;
    }

}
