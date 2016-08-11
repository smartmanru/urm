package org.urm.server.dist;

import org.urm.server.meta.Metadata;

public class ProductVersion {

	public int majorFirstNumber;
	public int majorSecondNumber;
	public int majorNextFirstNumber;
	public int majorNextSecondNumber;
	public int lastProdTag;
	public int nextProdTag;
	
	public ProductVersion( Metadata meta ) {
		majorFirstNumber = 0;
		majorSecondNumber = 0;
		majorNextFirstNumber = 0;
		majorNextSecondNumber = 0;
		lastProdTag = 0;
		nextProdTag = 0;
	}
	
	public void createInitial() {
		majorFirstNumber = 1;
		majorSecondNumber = 0;
		majorNextFirstNumber = 1;
		majorNextSecondNumber = 1;
		lastProdTag = 0;
		nextProdTag = 1;
	}
	
}
