package ru.egov.urm.conf;

public class ConfFileDiffItem {

	public int relFrom;
	public int relTo;
	public int prodFrom;
	public int prodTo;
	
	public ConfFileDiffItem( int relFrom , int relTo , int prodFrom , int prodTo ) {
		this.relFrom = relFrom;
		this.relTo = relTo;
		this.prodFrom = prodFrom;
		this.prodTo = prodTo;
	}

}
