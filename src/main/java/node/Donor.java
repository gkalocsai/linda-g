package node;

public class Donor extends PlaceHolder {
    public Donor(String typeId) { super(typeId); }
    
    public String toString() {
		return "<"+typeId+">";
	}

    
}