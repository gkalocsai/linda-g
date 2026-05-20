package node.primitive;

import main.KBAware;
import main.KnowledgeBase;
import node.Node;
import java.util.List;

public class Find extends Primitive implements KBAware{
    @Override public int getArity() { return 3; }

   

	@Override
	public String executeWithKB(List<Node> inputs, KnowledgeBase kb) {
		 if (inputs.size() < 3) return null;
	      
	        String s = inputs.get(0).getResult();
	        String p = inputs.get(1).getResult();
	        String o = inputs.get(2).getResult();
	        return kb.query(s, p, o);
		
	}

	@Override
	public String execute(List<Node> inputs) {
		throw new RuntimeException("Use executeWithKB!");		
	}
}