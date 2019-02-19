import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JCA {

    private JFrame mainFrame;
    private JTable table;
    private JTextField txt_filepath;
    private File file;
    private JFileChooser fc;
    private DefaultTableModel tblm = new DefaultTableModel() {
        @Override
        public Class getColumnClass(int column) {
            if (column == 2) {
                return Integer.class;
            }
            return String.class;
        }


    };
    private JCheckBox top5only;
    private JCheckBox complementary;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                JCA window = new JCA();
                window.mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     */
    private JCA() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mainFrame = new JFrame();
        mainFrame.setTitle("Java Color Analyzer");
        mainFrame.setResizable(false);
        mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(JCA.class.getResource("/spyglass.png")));
        mainFrame.setBounds(100, 100, 600, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MigLayout layout = new MigLayout("wrap 3", "[49.00][303.00][]", "[][][][][][]");
        mainFrame.getContentPane().setLayout(layout);

        //final ColoringCellRenderer cellRenderer = new ColoringCellRenderer();

        JLabel lbl_filepath = new JLabel("Path:");
        mainFrame.getContentPane().add(lbl_filepath, "cell 0 0,alignx center,growy");

        txt_filepath = new JTextField();
        txt_filepath.setEditable(false);
        mainFrame.getContentPane().add(txt_filepath, "cell 1 0,alignx center,growy");
        txt_filepath.setColumns(36);

        top5only = new JCheckBox("Show only Top5 values");
        mainFrame.getContentPane().add(top5only, "cell 1 1,alignx center");
        complementary = new JCheckBox("Show complementary colors");
        mainFrame.getContentPane().add(complementary, "cell 1 2,alignx center");


        String[] headers = {"Color (RGB)", "Color (Hex)", "Pixelcount", "Complementary (RGB)", "Complementary (Hex)"};
        tblm.setColumnIdentifiers(headers);
        table = new JTable(tblm) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(getBackground());
                    int modelRow = convertRowIndexToModel(row);
                    Color color = Color.decode((String) getModel().getValueAt(modelRow, 1));
                    c.setBackground(color);
                }


                return c;
            }
        };
        //table.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
        table.setColumnSelectionAllowed(false);

        JButton btn_choosefilepath = new JButton("Choose ...");
        btn_choosefilepath.addActionListener(arg0 -> {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(new FileFilter() {
                private final FileNameExtensionFilter filter =
                        new FileNameExtensionFilter("Images",
                                "tiff", "tif", "gif", "jpeg", "jpg", "png");

                @Override
                public boolean accept(File f) {
                    return filter.accept(f);
                }

                @Override
                public String getDescription() {
                    return "Images";
                }
            });

            int returnVal = fc.showOpenDialog(fc);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fc.setCurrentDirectory(fc.getCurrentDirectory());
                file = fc.getSelectedFile();
                txt_filepath.setText(file.toString());
            }
        });
        mainFrame.getContentPane().add(btn_choosefilepath, "cell 2 0,alignx center,growy");

        JButton btn_analyze = new JButton("Analyze");
        btn_analyze.addActionListener(arg0 -> {
            table.setAutoCreateRowSorter(false);
            table.setRowSorter(new TableRowSorter<>(table.getModel()));
            tblm.setRowCount(0);
            if (file == null) {
                JOptionPane.showMessageDialog(null, "Please choose a file first.");
                return;
            }

            HashMap<Color, Integer> map = readImage(file);
            if (map == null) {
                return;
            }

            if (top5only.isSelected()) {
                map =
                        map.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .limit(5)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            }


            fillTable(map, complementary.isSelected());


            table.setAutoCreateRowSorter(true);
            map = null;
        });
        mainFrame.getContentPane().add(btn_analyze, "cell 2 1,alignx center");


        JLabel lbl_sorthint = new JLabel("Columns can be sorted by clicking on their headers AFTER ANALYZING.");
        mainFrame.getContentPane().add(lbl_sorthint, "cell 0 3 3 1,alignx center");

        JScrollPane scrollPane = new JScrollPane();
        mainFrame.getContentPane().add(scrollPane, "cell 0 4 3 1,alignx center");


        scrollPane.setViewportView(table);
    }

    private void fillTable(HashMap<Color, Integer> map, boolean calc_complementary) {
        if (calc_complementary) {
            for (Color color : map.keySet()) {
                HSLColor compl = new HSLColor(color);
                Color complementary = compl.getComplementary();
                Object[] row = {color.getRed() + " - " + color.getGreen() + " - " + color.getBlue(), String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), map.get(color), complementary.getRed() + " - " + complementary.getGreen() + " - " + complementary.getBlue(), String.format("#%02X%02X%02X", complementary.getRed(), complementary.getGreen(), complementary.getBlue())};
                tblm.addRow(row);
                //cellRenderer.setCellColor(tblm.getRowCount(), 3, color);
            }
        } else {
            for (Color color : map.keySet()) {
                Object[] row = {color.getRed() + " - " + color.getGreen() + " - " + color.getBlue(), String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), map.get(color), "0", "0"};
                tblm.addRow(row);
                //cellRenderer.setCellColor(tblm.getRowCount(), 3, color);
            }
        }
    }

    private HashMap<Color, Integer> readImage(File file) {
        BufferedImage image;

        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "An error happened:\r\n" + e.toString());
            return null;
        }

        HashMap<Color, Integer> map = new HashMap<>();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));

                if (map.containsKey(color)) {
                    map.put(color, map.get(color) + 1);
                } else {
                    map.put(color, 1);
                }
            }
        }
        return map;

    }


}
