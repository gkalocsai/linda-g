package node;

// The aim of this class to handle unique identifiers of entities of the world!
public class Thing implements Node {
    private String id;

    public Thing(String id) {
      
        this.id = id.replace("@", "");
    }

    @Override
    public String getResult() {
        return "@" + id + "@";
    }

    @Override
    public void setResult(String str) {
        this.id = str.replace("@", "");
    }

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public String toString() {
        return "@" + id + "@";
    }
}