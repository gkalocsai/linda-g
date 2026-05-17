package node;

public class Literal implements Node{
	   private String str = null;
	   public String getResult() { return str; }
	   public void setResult(String str) { this.str = str; }
	   
	   
	   
	   public Literal(String str) {
		super();
		this.str = str;
	}
	   public int getArity() {
	    	return 0;
	   }
	   
	   public String toString() {
			return "\""+str+"\"";
		}

}
