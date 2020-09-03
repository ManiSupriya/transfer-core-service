package com.mashreq.transfercoreservice.common;

import java.util.Objects;

import org.springframework.web.util.HtmlUtils;

public class HtmlEscapeCache {
	public static String htmlEscape(Object object) {
		if (Objects.nonNull(object)) {
			return HtmlUtils.htmlEscape(object.toString());
		}
		return null;
	}

}
