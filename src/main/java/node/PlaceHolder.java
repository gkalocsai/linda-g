package node;

public abstract class PlaceHolder implements Node {
    protected final String typeId;
    
    private String value;
    
    public PlaceHolder(String typeId) { this.typeId = typeId; }
    public String getTypeId() { return typeId; }

    @Override
	public void setResult(String str) {
	  this.value =str;		
	}
    
    @Override
	public String getResult() {
	  return this.value;		
	}
    
    @Override
	public int getArity() {		
		return 1;
	}
    
}