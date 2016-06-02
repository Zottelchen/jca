package de.zeropoly.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ColoringCellRenderer extends DefaultTableCellRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = 6013495342835602767L;
    private final Map<Point, Color> cellColors = new HashMap<Point, Color>();

    void setCellColor(int r, int c, Color color)
    {
        if (color == null)
        {
            cellColors.remove(new Point(r,c));
        }
        else
        {
            cellColors.put(new Point(r,c), color);
        }
    }

    public Color getCellColor(int r, int c)
    {
        Color color = cellColors.get(new Point(r,c));
        if (color == null)
        {
            return Color.WHITE;
        }
        return color;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = getCellColor(row, column);
        setBackground(color);
        return this;
    }

}