package cat.nyaa.saika.forge;

public class Element {
    private String name;
    private String displayName = "";

    public Element(String elementName){
        this.name = elementName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getDisplayName(){
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
