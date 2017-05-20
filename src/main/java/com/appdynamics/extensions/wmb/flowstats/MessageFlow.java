package com.appdynamics.extensions.wmb.flowstats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.namespace.QName;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class MessageFlow {

	@XmlAnyAttribute
	private Map<QName, String> attributes;

	public Map<QName, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<QName, String> attributes) {
		this.attributes = attributes;
	}
}