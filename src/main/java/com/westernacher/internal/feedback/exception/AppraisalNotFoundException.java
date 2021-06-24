package com.westernacher.internal.feedback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AppraisalNotFoundException extends RuntimeException {

	public AppraisalNotFoundException(String exception) {
		super(exception);
	}

}
