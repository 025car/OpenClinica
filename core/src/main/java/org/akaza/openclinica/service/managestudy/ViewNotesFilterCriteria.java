/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.service.managestudy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesFilterCriteria {

    private static final Map<String, String> FILTER_BY_TABLE_COLUMN = new HashMap<String, String>();

    private static final String[] NUMERIC_FILTERS = {
        "discrepancy_note_type_id","resolution_status_id","days","age"
    };

    private static final String[] DATE_FILTERS = {
        "date_created", "date_updated"
    };

    static {
        FILTER_BY_TABLE_COLUMN.put("studySubject.label", "label");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.disType", "discrepancy_note_type_id");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.resolutionStatus", "resolution_status_id");
        FILTER_BY_TABLE_COLUMN.put("siteId", "site_id");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.createdDate", "date_created");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.updatedDate", "date_updated");
        FILTER_BY_TABLE_COLUMN.put("days", "days");
        FILTER_BY_TABLE_COLUMN.put("age", "age");
        FILTER_BY_TABLE_COLUMN.put("eventName", "event_name");
        FILTER_BY_TABLE_COLUMN.put("crfName", "crf_name");
        FILTER_BY_TABLE_COLUMN.put("entityName", "entity_name");
        FILTER_BY_TABLE_COLUMN.put("entityValue", "value");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.entityType", "entity_type");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.description", "description");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.user", "user");
    }

    private final Map<String, Object> filters = new HashMap<String, Object>();

    public static ViewNotesFilterCriteria buildFilterCriteria(FilterSet filterSet, String datePattern) {
        DateFormat df = new SimpleDateFormat(datePattern);
        ViewNotesFilterCriteria criteria = new ViewNotesFilterCriteria();
        for (Filter filter : filterSet.getFilters()) {
            String columnName = filter.getProperty();
            String filterName = FILTER_BY_TABLE_COLUMN.get(columnName);
            if (filterName == null) {
                throw new IllegalArgumentException("No query fragment available for column '" + columnName + "'");
            }
            criteria.getFilters().put(filterName, processValue(filterName, filter.getValue(), df));
        }
        return criteria;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    protected static Object processValue(String filterName, String value, DateFormat df) {
        if (Arrays.asList(NUMERIC_FILTERS).contains(filterName)) {
            return Integer.parseInt(value);
        } else if (Arrays.asList(DATE_FILTERS).contains(filterName)) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                throw new IllegalArgumentException("The filter '" + filterName + "' doesn't contain a valid date: "
                        + value, e);
            }
        }
        return "%" + StringUtils.trim(value) + "%";
    }

}
