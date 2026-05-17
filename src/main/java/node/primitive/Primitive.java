package node.primitive;

import node.Node;
import java.util.List;

public abstract class  Primitive implements Node {
    private String result;


	public abstract String execute(List<Node> inputs);
    public abstract int getArity();
    
    @Override
  	public void setResult(String str) {
  	  this.result =str;		
  	}
      

    @Override
  	public String getResult() {
  	  return this.result;		
  	}

    public String toString() {
    	return this.getClass().getName();
    }
    
}