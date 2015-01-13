package com.github.aureliano.srvraml.code.meta;

import java.util.Map;

import com.github.aureliano.srvraml.helper.CodeBuilderHelper;

public class FieldMeta {

	private String name;
	private String type;
	private String genericType;
	private Visibility visibility;
	
	public FieldMeta() {
		super();
	}
	
	public static FieldMeta parse(Map<String, ?> data) {
		if (data == null) {
			return null;
		}
		
		FieldMeta f = new FieldMeta();
		
		f.setName(data.get("name").toString());
		f.setType(CodeBuilderHelper.getJavaType(data.get("type").toString()));
				
		if (data.get("items") != null) {
			Map<String, String> items = (Map<String, String>) data.get("items");
			f.setGenericType(CodeBuilderHelper.getJavaType(items.get("$ref")));
		}
		
		if (data.get("visibility") == null) {
			f.setVisibility(Visibility.PRIVATE);
		} else {
			f.setVisibility(Visibility.valueOf(data.get("visibility").toString().toUpperCase()));
		}
		
		return f;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGenericType() {
		return genericType;
	}

	public void setGenericType(String genericType) {
		this.genericType = genericType;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldMeta other = (FieldMeta) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format(
			"[ name => \"%s\", type => %s, genericType => %s, visibility => %s ]",
			this.name, this.type, this.genericType, this.visibility
		);
	}
}