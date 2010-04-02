package org.akaza.openclinica.domain.rule.action;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("3")
public class ShowActionBean extends RuleActionBean {

    private String message;
    private String OIDs;

    public ShowActionBean() {
        setActionType(ActionType.SHOW);
        setRuleActionRun(new RuleActionRunBean(true, true, true, false, false));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOIDs() {
        return OIDs;
    }

    @Column(name = "oids")
    public void setOIDs(String oIDs) {
        OIDs = oIDs;
    }

    @Override
    @Transient
    public String getSummary() {
        return this.message;
    }

    @Transient
    public String[] getOIDsAsArray() {
        String[] oids = getOIDs().split(",");
        return oids;
    }

    @Override
    public String toString() {
        return "ShowActionBean [OIDs=" + OIDs + ", message=" + message + "]";
    }

}
