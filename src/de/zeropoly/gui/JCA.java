package de.zeropoly.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;


import net.miginfocom.swing.MigLayout;

public class JCA {

	private JFrame mainFrame;
	private JTable table;
	private JTextField txt_filepath;
	private File file;
	private JFileChooser fc;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JCA window = new JCA();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JCA() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainFrame = new JFrame();
		mainFrame.setTitle("Java Color Analyzer");
		mainFrame.setResizable(false);
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(JCA.class.getResource("/de/zeropoly/res/spyglass.png")));
		mainFrame.setBounds(100, 100, 450, 500);
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

		DefaultTableModel tblm = new DefaultTableModel(){
			@Override
			public Class getColumnClass(int column){
				switch (column) {
				case 2:
					return Integer.class;
				default:
					return String.class;
				}
			}


		};
		String[] headers = {"Color (RGB)", "Color (Hex)", "Pixelcount"};
		tblm.setColumnIdentifiers(headers);
		table = new JTable(tblm){
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
				Component c = super.prepareRenderer(renderer, row, column);
				if (!isRowSelected(row))
				{
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
		btn_choosefilepath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (fc == null){
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
			}
		});
		mainFrame.getContentPane().add(btn_choosefilepath, "cell 2 0,alignx center,growy");

		JButton btn_analyze = new JButton("Analyze");
		btn_analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				table.setAutoCreateRowSorter(false);
				table.setRowSorter(new TableRowSorter(table.getModel()));
				tblm.setRowCount(0);
				if (file == null){
					JOptionPane.showMessageDialog(null, "Please choose a file first.");
					return;
				}
				BufferedImage image;
				try {
					image = ImageIO.read(file);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "An error happended:\r\n" + e.toString());
					return;
				}

				HashMap<Color, Integer> map = new HashMap<Color, Integer>();
				for (int i = 0; i < image.getWidth(); i++){
					for (int j = 0; j < image.getHeight(); j++){
						Color color = new Color(image.getRGB(i, j));

						if(map.containsKey(color)){
							map.put(color, map.get(color)+1);
						}
						else{
							map.put(color, 1);
						}
					}
				}



				for (Color color: map.keySet()){
					Object[] row = {color.getRed() + " - " + color.getGreen() + " - " + color.getBlue(), String.format("#%02X%02X%02X", color.getRed(), color.getBlue(), color.getGreen()), map.get(color).intValue()};
					tblm.addRow(row);
					//cellRenderer.setCellColor(tblm.getRowCount(), 3, color);
				}


				table.setAutoCreateRowSorter(true);
			}
		});
		mainFrame.getContentPane().add(btn_analyze, "cell 1 1,alignx center");

		JLabel lbl_sorthint = new JLabel("Columns can be sorted by clicking on their headers AFTER ANALYZING.");
		mainFrame.getContentPane().add(lbl_sorthint, "cell 0 3 3 1,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		mainFrame.getContentPane().add(scrollPane, "cell 0 4 3 1,alignx center");


		scrollPane.setViewportView(table);
	}

}
