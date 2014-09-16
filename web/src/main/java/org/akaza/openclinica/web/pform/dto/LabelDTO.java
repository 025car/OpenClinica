package org.akaza.openclinica.web.pform.dto;

public class LabelDTO{
	private String ref;
	private String label;
	
	public String getRef() {
	  return ref;
  }
	public void setRef(String ref) {
	  this.ref = ref;
  }
	public String getLabel() {
	  return label;
  }
	public void setLabel(String label) {
	  this.label = label;
  }
	
	@Override
	public String toString() {
			return "<label ref=" +ref+ ">" +label+ "</label>";
	}
}
