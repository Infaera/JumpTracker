/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jumptracker;

import java.util.*;

/**
 *
 * @author Infaera
 */
public class Jumper {

    String name, link;
    ArrayList<Jump> jumpList;
    ArrayList<String> typeList;
    boolean pointsLinked;
    public static String[] basicTypes = new String[]{"All", "Origin", "Perk",
        "Item", "Companion", "Drawback", "Scenario", "Other"};
    //ArrayList<Option> chainOptions;

    public Jumper() {
        jumpList = new ArrayList();
        typeList = new ArrayList(Arrays.asList(basicTypes));
    }

    public Jumper(String jumper) {
        this();
        name = jumper;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void saveLink(String str) {
        link = str;
    }

    public String getLink() {
        return link;
    }

    public void addJump(Jump jump) {
        if (!jumpList.contains(jump)) {
            jumpList.add(jump);
        }
    }

    public List<Jump> getJumpList() {
        return jumpList;
    }

    public Jump getJump(int index) {
        return jumpList.get(index);
    }

    public void deleteJump(Jump jump) {
        jumpList.remove(jump);
    }

    public void deleteJump(int toDeleteIndex) {
        jumpList.remove(toDeleteIndex);
    }

    public void setJumpList(ArrayList<Jump> list) {
        jumpList = list;
    }

    public void setTypes(ArrayList<String> list) {
        typeList = list;
    }

    public void setTypes(String[] list) {
        typeList = new ArrayList<>();
        typeList.addAll(Arrays.asList(list));
    }

    public String[] getTypesArray() {
        return typeList.toArray(new String[typeList.size()]);
    }

    public ArrayList<String> getTypeList() {
        return typeList;
    }

    public ArrayList<String> getActiveTypes() {
        ArrayList<String> activeTypes = new ArrayList();
        jumpList.forEach((j) -> {
            j.getOptionList().forEach((o) -> {
                if (!activeTypes.contains(o.getType())) {
                    activeTypes.add(o.getType());
                }
            });
        });
        return activeTypes;
    }

    public int calcCosts() {
        int points = 0;
        for (Jump jump : jumpList) {
            points += jump.calcCosts();
        }
        return points;
    }

    public void setPointsLinked(boolean selected) {
        pointsLinked = selected;
    }

    public boolean isPointsLinked() {
        return pointsLinked;
    }
}
//    public List<Option> getChainOptions() {
//        return chainOptions;
//    }
//
//    public void setChainOptions(ArrayList<Option> list) {
//        chainOptions = list;
//    }
//
//    public void addChainOption(Option option) {
//        if (!chainOptions.contains(option)) {
//            chainOptions.add(option);
//        }
//    }
//
//    public void deleteChainOption(Option option) {
//        chainOptions.remove(option);
//    }
//
//    public Option getChainOption(int index) {
//        return chainOptions.get(index);
//    }

