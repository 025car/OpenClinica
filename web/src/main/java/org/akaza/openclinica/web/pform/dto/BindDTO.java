package org.akaza.openclinica.web.pform.dto;

public class BindDTO {
	private String nodeSet;
	private String type;
	private String readOnly;
	private String calculate;
	private String constraint;
	private String constraintMsg;
	private String required;
	private String jrPreload;
	
	public String getNodeSet() {
	  return nodeSet;
  }
	
	public void setNodeSet(String nodeSet) {
	  this.nodeSet = nodeSet;
  }
	
	public String getType() {
	  return type;
  }
	
	public void setType(String type) {
	  this.type = type;
  }
	
	public String getReadOnly() {
	  return readOnly;
  }
	
	public void setReadOnly(String readOnly) {
	  this.readOnly = readOnly;
  }
	
	public String getCalculate() {
	  return calculate;
  }
	
	public void setCalculate(String calculate) {
	  this.calculate = calculate;
  }
	
	public String getConstraint() {
	  return constraint;
  }
	
	public void setConstraint(String constraint) {
	  this.constraint = constraint;
  }
	
	public String getConstraintMsg() {
	  return constraintMsg;
  }
	
	public void setConstraintMsg(String constraintMsg) {
	  this.constraintMsg = constraintMsg;
  }
	
	public String getRequired() {
	  return required;
  }
	
	public void setRequired(String required) {
	  this.required = required;
  }

	public String getJrPreload() {
		return jrPreload;
	}

	public void setJrPreload(String jrPreload) {
		this.jrPreload = jrPreload;
	}

	@Override
	public String toString() {
	  return "<bind nodeset="+nodeSet+" type="+type+" readonly= "+readOnly+" calculate=" +calculate+ " constraint=" +constraint+ " constraintMsg=" +constraintMsg+ " required=" +required+ " jrPreload" +jrPreload+ " />";
	}
}