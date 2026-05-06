package node;

public abstract class PlaceHolder extends Node {
    protected final String typeId;
    public PlaceHolder(String typeId) { this.typeId = typeId; }
    public String getTypeId() { return typeId; }
}