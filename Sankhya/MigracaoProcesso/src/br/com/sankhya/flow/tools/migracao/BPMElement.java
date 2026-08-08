package br.com.sankhya.flow.tools.migracao;

import org.jdom.Element;

import com.sankhya.util.XMLUtils;

public class BPMElement{

	private String kind;
	private String name;
	private String id;

	public BPMElement(String kind, String name, String id){
		this.kind = kind;
		this.name = name;
		this.id = id;
	}

	public static BPMElement fromElement(Element e){
		return new BPMElement(e.getName(), XMLUtils.getAttributeAsString(e, "name"), XMLUtils.getAttributeAsString(e, "id"));
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
