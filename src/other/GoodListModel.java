package other;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.*;

public class GoodListModel<T> implements ListModel<T> {

    private final List<T> items;
    private final List<ListDataListener> listDataListeners = new LinkedList<>();

    @SafeVarargs
    public GoodListModel(T... items) {
        this.items = new LinkedList<>(Arrays.asList(items));
    }

    public void add(T d) {
        items.add(d);
        for (ListDataListener ldl : listDataListeners)
            ldl.contentsChanged(new ListDataEvent(this,
                    ListDataEvent.CONTENTS_CHANGED,
                    items.size() - 1,
                    items.size()));
    }

    public void sort(Comparator<? super T> c) {
        items.sort(c);
        for (ListDataListener ldl : listDataListeners)
            ldl.contentsChanged(new ListDataEvent(this,
                    ListDataEvent.CONTENTS_CHANGED,
                    items.size() - 1,
                    items.size()));

    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int index) {
        return items.get(index);
    }

    public T pop(int index) {
        T d = getElementAt(index);
        items.remove(d);
        for (ListDataListener ldl : listDataListeners)
            ldl.contentsChanged(new ListDataEvent(this,
                    ListDataEvent.CONTENTS_CHANGED,
                    index - 1,
                    index + 1));
        return d;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listDataListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listDataListeners.remove(l);
    }
}
