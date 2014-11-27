package org.akaza.openclinica.service;

import java.text.MessageFormat;

import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PformValidator implements Validator {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Override
	public boolean supports(Class<?> clazz) {
		return ItemItemDataContainer.class.equals(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors e) {
		ItemItemDataContainer container = (ItemItemDataContainer) target;
		String origValue = container.getItemDataBean().getValue();
		Integer responseTypeId = container.getResponseTypeId();
		Integer itemDataTypeId = container.getItemBean().getItemDataTypeId();
		logger.info("*** Data type id:  ***" + itemDataTypeId);

		if (responseTypeId == 3 || responseTypeId == 7) {
			String[] values = origValue.split(",");
			for (String value : values) {
				subValidator(itemDataTypeId, value.trim(), e);
			}
		} else {
			subValidator(itemDataTypeId, origValue, e);

		}
	}

	public void subValidator(Integer itemDataTypeId, String value, Errors e) {
		if (value != null && value != "") {

			switch (itemDataTypeId) {
			case 6: { // ItemDataType.INTEGER
				try {
					Integer.valueOf(value);
				} catch (NumberFormatException nfe) {
					e.reject("value.invalid.Integer");
				}
				break;
			}
			case 7: { // ItemDataType.REAL
				try {
					Float.valueOf(value);
				} catch (NumberFormatException nfe) {
					e.reject("value.invalid.float");
				}
				break;
			}
			case 9: { // ItemDataType.DATE
				if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value)) {
					System.out.print(" Error");
					e.reject("value.invalid.date");
				}
				break;
			}
			case 10: { // ItemDataType.PDATE
				if (!ExpressionTreeHelper.isDateyyyyMMddDashes(value) && !ExpressionTreeHelper.isDateyyyyMMDashes(value)
						&& !ExpressionTreeHelper.isDateyyyyDashes(value)) {
					e.reject("value.invalid.pdate");
				}
				break;
			}
			case 11: { // ItemDataType.FILE
				e.reject("value.notSupported.file");
				break;
			}

			default:
				break;
			}

		}
	}

}
