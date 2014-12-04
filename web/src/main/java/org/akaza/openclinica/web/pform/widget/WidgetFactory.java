package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WidgetFactory {

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_TEXTAREA = 2;
	public static final int TYPE_CHECKBOX = 3;
	public static final int TYPE_FILE = 4;
	public static final int TYPE_RADIO = 5;
	public static final int TYPE_SINGLE_SELECT = 6;
	public static final int TYPE_MULTI_SELECT = 7;
	public static final int TYPE_CALCULATION = 8;
	public static final int TYPE_GROUP_CALCULATION = 9;
	public static final int TYPE_INSTANT_CALCULATION = 10;
	public static final String SECTION_TEXT_TYPE_SUBTITLE = "SUBTITLE";
	public static final String SECTION_TEXT_TYPE_INSTRUCTIONS = "INSTRUCTIONS";

    protected final Logger log = LoggerFactory.getLogger(WidgetFactory.class);

	private CRFVersionBean version = null;
	
	public WidgetFactory(CRFVersionBean version)
	{
		this.version = version;
	}
	public Widget getWidget(ItemBean item , Integer widgetType,ItemGroupBean itemGroupBean,ItemFormMetadataBean itemFormMetaDataBean , Integer itemGrouprepeatNumber, boolean isItemRequired,boolean isGroupRepeating)
	{		
		switch (widgetType)
		{
			case TYPE_TEXT: return new InputWidget(version, item, null,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_SINGLE_SELECT: return new Select1Widget(version, item,Widget.APPEARANCE_MINIMAL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_RADIO:  return new Select1Widget(version,item,Widget.APPEARANCE_FULL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_MULTI_SELECT: return new SelectWidget(version,item,Widget.APPEARANCE_MINIMAL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_CHECKBOX: return new SelectWidget(version,item,Widget.APPEARANCE_FULL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_TEXTAREA: return new InputWidget(version, item, Widget.APPEARANCE_MULTILINE,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			default: 
				log.debug("Unsupported form widget: " + widgetType + "  Skipping.");
				return null;
		}
	}
	public Widget getHeaderWidget(ItemBean item, ItemFormMetadataBean itemMetaData, ItemGroupBean itemGroup)
	{		
		int widgetType = itemMetaData.getResponseSet().getResponseType().getId();                                                                                    
		
		switch (widgetType)
		{
			case TYPE_TEXT: 
			case TYPE_SINGLE_SELECT: 
			case TYPE_RADIO:  
			case TYPE_MULTI_SELECT: 
			case TYPE_CHECKBOX: 
			case TYPE_TEXTAREA: 
				if (itemMetaData.getHeader() != null && !itemMetaData.getHeader().equals("")) return new HeaderWidget(version, item, itemMetaData, itemGroup, null);
				else
				{
					log.debug("No header found for widget: " + widgetType + ". Skipping.");
					return null;
				}
			default: 
				log.debug("Unsupported form widget: " + widgetType + ".  Skipping.");
				return null;
		}
	}
	public Widget getSectionTextWidget(String versionOid, String field, SectionBean section)
	{		
		switch (field)
		{
			case SECTION_TEXT_TYPE_SUBTITLE: 
				if (section.getSubtitle() != null && !section.getSubtitle().equals("")) return new SectionTextWidget(versionOid, section.getSubtitle(),section.getId(),SECTION_TEXT_TYPE_SUBTITLE);
				else
				{
					log.debug("No Subtitle found for Section. Skipping.");
					return null;
				}
			case SECTION_TEXT_TYPE_INSTRUCTIONS: 
				if (section.getInstructions() != null && !section.getSubtitle().equals("")) return new SectionTextWidget(versionOid, section.getInstructions(),section.getId(),SECTION_TEXT_TYPE_INSTRUCTIONS);
				else
				{
					log.debug("No Instructions found for Section. Skipping.");
					return null;
				}
			default: 
				log.debug("Unsupported Section Text widget: " + field + ".  Skipping.");
				return null;
		}
	}
	public Widget getSubHeaderWidget(ItemBean item, ItemFormMetadataBean itemMetaData, ItemGroupBean itemGroup)
	{		
		int widgetType = itemMetaData.getResponseSet().getResponseType().getId();                                                                                    
		
		switch (widgetType)
		{
			case TYPE_TEXT: 
			case TYPE_SINGLE_SELECT: 
			case TYPE_RADIO:  
			case TYPE_MULTI_SELECT: 
			case TYPE_CHECKBOX: 
			case TYPE_TEXTAREA: 
				if (itemMetaData.getSubHeader() != null && !itemMetaData.getSubHeader().equals("")) return new SubHeaderWidget(version, item, itemMetaData, itemGroup, null);
				else
				{
					log.debug("No SubHeader found for widget: " + widgetType + ". Skipping.");
					return null;
				}
			default: 
				log.debug("Unsupported form widget: " + widgetType + ".  Skipping.");
				return null;
		}
	}
}
