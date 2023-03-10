package fr.hedwin.objects;

public class Tiers {

    private int id;
    private String name;

    private String regex;

    public Tiers(int id, String name, String regex) {
        this.id = id;
        this.name = name;
        this.regex = regex;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return getName();
    }
}
