package com.ccma.Modals;

public class SelectedItem {
    String text;
    boolean isSelected;

    public SelectedItem(String text, boolean isSelected) {
        this.text = text;
        this.isSelected = isSelected;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
