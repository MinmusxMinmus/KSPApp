package other;

import javax.swing.*;
import java.awt.*;

public class MainSearchCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        // Check if the value has a different representation
        if (value instanceof KSPObject l) setText(l.getTextRepresentation());
        return this;
    }
}
