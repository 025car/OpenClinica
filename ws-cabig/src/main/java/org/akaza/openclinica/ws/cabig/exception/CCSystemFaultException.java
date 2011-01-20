package org.akaza.openclinica.ws.cabig.exception;

import org.akaza.openclinica.exception.OpenClinicaException;

public class CCSystemFaultException extends OpenClinicaException {
    
    public CCSystemFaultException(String message) {
        super(message, "CCSystemFault");
    }

}

