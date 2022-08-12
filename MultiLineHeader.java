
/**
 * @author Matthew Kelly work
 * This class simply allows a table row/header to me multi-lined 
 * It is adapted from the SWING docs.
 */

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MultiLineHeader extends JList implements TableCellRenderer {
  public MultiLineHeader() {
    setOpaque(true);
    setForeground(UIManager.getColor("TableHeader.foreground"));
    setBackground(UIManager.getColor("TableHeader.background"));
    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    ListCellRenderer renderer = getCellRenderer();
    ((JLabel)renderer).setHorizontalAlignment(JLabel.CENTER);
    setCellRenderer(renderer);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                   boolean isSelected, boolean hasFocus, int row, int column) {
    setFont(table.getFont());
    String str = (value == null) ? "" : value.toString();
    BufferedReader br = new BufferedReader(new StringReader(str));
    String line;
    Vector v = new Vector();
    try {
      while ((line = br.readLine()) != null) {
        v.addElement(line);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    setListData(v);
    return this;
  }
}