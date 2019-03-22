package uk.ac.babraham.SeqMonk.Vistory;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import uk.ac.babraham.SeqMonk.Utilities.EscapeHTML;

public class VistoryTable extends VistoryBlock implements TableModel {

	String [][] tableData;
	
	public VistoryTable (Date date, String data) {
		super(date);
		String [] sections = data.split("%%t%%");
		
		int rows = Integer.parseInt(sections[0]);
		int cols = Integer.parseInt(sections[1]);
		
		tableData = new String[rows+1][cols];
		
		int index = 2;
		
		for (int h=0;h<cols;h++) {
			tableData[0][h] = sections[index];
			index++;
		}
		
		for (int r=0;r<rows;r++) {
			for (int c=0;c<cols;c++) {
				tableData[r+1][c] = sections[index];
				index++;
			}
		}
		
		doInitialLayout();
		
	}
	
	
	public VistoryTable (TableModel model) {
		
		tableData = new String [model.getRowCount()+1][model.getColumnCount()];
		
		// First row is headers
		for (int i=0;i<tableData[0].length;i++) {
			tableData[0][i] = model.getColumnName(i);
		}
		
		// Now the rest of the data
		for (int i=0;i<tableData.length-1;i++) {
			for (int j=0;j<tableData[0].length;j++) {
				Object value = model.getValueAt(i, j);
				if (value == null) {
					tableData[i+1][j] = "";					
				}
				else {
					tableData[i+1][j] = value.toString();
				}
			}
		}
		doInitialLayout();
	}
	
	private void doInitialLayout() {
		
		JTable table = new JTable(this);
		table.addMouseListener(this);
		add(table.getTableHeader(),BorderLayout.NORTH);
		add(table,BorderLayout.CENTER);
		
		// We need to add this call otherwise an optimisation in the 
		// scroll pane implementation means that non-visible portions
		// of the table won't render until we click on it.
		table.setVisible(true);
	}
	
	public void setData (String[][] tableData) {
		this.tableData = tableData;
	}
	
	@Override
	public String getHTML() {

		StringBuffer sb = new StringBuffer();
		sb.append("<table>");
		
		for (int i=0;i<tableData.length;i++) {
			sb.append("<tr>");
			for (int j=0;j<tableData[i].length;j++) {
				if (i==0) {
					sb.append("<th>");
					sb.append(EscapeHTML.escapeHTML(tableData[i][j]));
					sb.append("</th>");
				}
				else {
					sb.append("<td>");
					sb.append(EscapeHTML.escapeHTML(tableData[i][j]));
					sb.append("</td>");
				}
			}
			sb.append("</tr>");
		}
		
		sb.append("</table>");
	
		return sb.toString();
	}


	@Override
	public void addTableModelListener(TableModelListener l) {}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return tableData[0].length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return tableData[0][columnIndex];
	}

	@Override
	public int getRowCount() {
		return tableData.length-1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
//		System.err.println("Getting r="+rowIndex+" c="+columnIndex+" rows="+tableData.length+" cols="+tableData[0].length);
		return tableData[rowIndex+1][columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

	@Override
	public String getType() {
		return "TABLE";
	}

	@Override
	public String getData() {
		StringBuffer sb = new StringBuffer();
		// We start with rows and columns
		sb.append(getRowCount());
		sb.append("%%t%%");
		sb.append(getColumnCount());
		sb.append("%%t%%");
		
		// Now the headers
		for (int i=0;i<getColumnCount();i++) {
			sb.append(getColumnName(i));
			sb.append("%%t%%");
		}

		// Now the rows
		for (int r=0;r<getRowCount();r++) {
			for (int c=0;c<getColumnCount();c++) {
			sb.append(getValueAt(r, c));
			sb.append("%%t%%");
			}
		}
		
		return sb.toString();
		
	}


	@Override
	public boolean allowsRelativePosition() {
		return false;
	}


}