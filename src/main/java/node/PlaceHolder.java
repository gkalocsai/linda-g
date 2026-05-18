package node;

public abstract class PlaceHolder implements Node {
    protected final String typeId;
    private String value;
    private Node input; // Explicit reference to the input node

    public PlaceHolder(String typeId) { this.typeId = typeId; }
    public String getTypeId() { return typeId; }

    public Node getInput() { return input; }
    public void setInput(Node input) { this.input = input; }

    @Override
    public void setResult(String str) { this.value = str; }

    @Override
    public String getResult() { return this.value; }

    @Override
    public int getArity() { return 1; }
}