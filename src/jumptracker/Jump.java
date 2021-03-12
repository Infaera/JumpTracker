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
 *
 * Name, Points, Options, Supplements Options > Name, Descriptions, Cost, -Rules
 *
 */
public class Jump {

    String name;
    int points, selectedOption = 0, stepValue;
    boolean supplement = false;
    private ArrayList<Option> options;

    public Jump() {
        this("");
    }

    public Jump(String title) {
        this(title, 1000);
    }

    public Jump(String title, int amount) {
        name = title;
        points = amount;
        options = new ArrayList();
        stepValue = 1;
    }

    public int calcCosts() {
        int cost = points;
        for (Option o : options) {
            if (o.isActive()) {
                cost += o.getPoints();
            }
        }
        return cost;
    }

    public void setSupplement(boolean is) {
        supplement = is;
    }

    public boolean isSupplement() {
        return supplement;
    }

    public List<Option> getOptionList() {
        return options;
    }

    public Option getOption(int i) {
        return options.get(i);
    }

    public void setOptions(ArrayList<Option> options) {
        this.options = options;
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public void deleteOption(Option option) {
        options.remove(option);
    }

    public void isSupplement(boolean supplemental) {
        supplement = supplemental;
    }

    public void setPoints(int val) {
        this.points = val;
    }

    public int getPoints() {
        return points;
    }

    public void setName(String str) {
        name = str;
    }

    public String getName() {
        return name;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int index) {
        selectedOption = index;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setStepValue(int val) {
        stepValue = val;
    }

    public int getStepValue() {
        return stepValue;
    }

}
