/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jumptracker;

/**
 *
 * @author Infaera
 */
public class Option {

    String type = "Perk";
    String name, description, notes, jumpName;
    boolean active = true, chain = false;
    int points, scrollPoint = 0;

    public Option() {
        this("", "", "", 0, false);
    }

    public Option(String title) {
        this(title, "", "", 0, false);
    }

    public Option(String title, String desc) {
        this(title, "", "", 0, false);
    }

    public Option(String title, String desc, String note) {
        this(title, desc, note, 0, false);
    }

    public Option(String title, int val) {
        this(title, "", "", val, false);
    }

    public Option(String title, String desc, int val) {
        this(title, desc, "", val, false);
    }

    public Option(String title, String desc, String note, int amount, boolean isChain) {
        name = title;
        description = desc;
        notes = note;
        points = amount;
        chain = isChain;
    }

    public Option(Option o) {
        name = o.getType();
        description = o.getDescription();
        notes = o.getNotes();
        chain = o.isChain();
        points = o.getPoints();
        type = o.getType();
        active = o.isActive();
    }

    public int getScrollPoint() {
        return scrollPoint;
    }

    public void setScrollPoint(int scrollPoint) {
        this.scrollPoint = scrollPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String str) {
        name = str;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String str) {
        description = str;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int amount) {
        this.points = amount;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isChain() {
        return chain;
    }

    public void setChain(boolean chain) {
        this.chain = chain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public void setJumpName(String name) {
        jumpName = name;
    }
    
    public String getJumpName() {
        return jumpName;
    }

    public void setActive(boolean selected) {
        active = selected;
    }
    
    public boolean isActive() {
        return active;
    }

//    public enum OptionType {
//        All, Origin, Perk, Item, Drawback, Scenario, Other;
//    }

}
