package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Hint;
import org.akaza.openclinica.web.pform.dto.Input;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class InputWidget extends BaseWidget {
	private ItemBean item = null;
	private CRFVersionBean version = null;
	private String appearance = null;
	private ItemGroupBean itemGroupBean =null;
	private ItemFormMetadataBean itemFormMetadataBean=null;
	private Integer itemGroupRepeatNumber;
	private boolean isItemRequired;
	private boolean isGroupRepeating;
	public InputWidget(CRFVersionBean version, ItemBean item, String appearance ,ItemGroupBean itemGroupBean, ItemFormMetadataBean itemFormMetadataBean , Integer itemGroupRepeatNumber , boolean isItemRequired,boolean isGroupRepeating)
	{
		this.item = item;
		this.version = version;
		this.appearance = appearance;
		this.itemGroupBean=itemGroupBean;
		this.itemFormMetadataBean=itemFormMetadataBean;
		this.itemGroupRepeatNumber=itemGroupRepeatNumber;
        this.isItemRequired=isItemRequired;
	    this.isGroupRepeating=isGroupRepeating;
	}
	
	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		if (isGroupRepeating) {
			label.setLabel(itemFormMetadataBean.getLeftItemText() + " (" + itemGroupRepeatNumber + ")");
		} else {
			label.setLabel(itemFormMetadataBean.getLeftItemText());
		}

		input.setLabel(label);
		//Hint hint = new Hint();
		//hint.setHint(item.getItemMeta().getLeftItemText());
		//input.setHint(hint);
		if (appearance != null) input.setAppearance(appearance);
	input.setRef("/" + version.getOid() +"/" + item.getOid() );
		return input;
	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		binding.setNodeSet("/" + version.getOid() +"/" + item.getOid());
		binding.setType(getDataType(item));
		if (isItemRequired) binding.setRequired("true()");
		return binding;
	}

}
