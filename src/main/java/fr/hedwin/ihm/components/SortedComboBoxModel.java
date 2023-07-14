package fr.hedwin.ihm.components;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortedComboBoxModel<E> extends DefaultComboBoxModel<E> {
    private final Comparator<E> comparator;
    private final List<E> originalItems;
    private final List<E> filteredItems;

    public SortedComboBoxModel(List<E> originalItems, Comparator<E> comparator) {
        super();
        this.originalItems = originalItems;
        this.filteredItems = originalItems;
        this.comparator = comparator;
    }

    public void addAll(List<E> items) {
        originalItems.addAll(items);
        updateFilteredItems();
    }

    private void updateFilteredItems() {
        filteredItems.clear();
        filteredItems.addAll(originalItems);
        filteredItems.sort(comparator);
    }

    public void setFilter(String filter) {
        filteredItems.clear();

        for (E item : originalItems) {
            if (item.toString().toLowerCase().contains(filter)) {
                filteredItems.add(item);
            }
        }

        fireContentsChanged(this, 0, filteredItems.size() - 1);
    }

    public void resetFilter() {
        filteredItems.clear();
        filteredItems.addAll(originalItems);
        fireContentsChanged(this, 0, filteredItems.size() - 1);
    }

    @Override
    public E getElementAt(int index) {
        return filteredItems.get(index);
    }

    @Override
    public int getSize() {
        return filteredItems.size();
    }
}