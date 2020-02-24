/* This class will eventually hold the largest
 * code from the code generator that may then be
 * used to increment as needed for update content.
 * For now, we will leave the code generator in 
 * the CDCTables class
 */

package gov.nih.nlm.mor.db;

import java.text.DecimalFormat;

public class CodeGenerator {
	private Integer index = null;
	private DecimalFormat df = null;

	public CodeGenerator() {
		index = (int) 0;
		df = new DecimalFormat("#");		
	}
	
	public String plus() {
		return String.valueOf(Integer.valueOf(df.format(++index)));
	}
	
	public String get() {
		return String.valueOf(Integer.valueOf(df.format(index)));
	}


}
