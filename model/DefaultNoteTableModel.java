package notes.model;

import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import util.Contract;

public class DefaultNoteTableModel extends AbstractTableModel implements NoteTableModel {
	
	private static final long serialVersionUID = 1L;
	private List<Object[]> lines;
	
	public DefaultNoteTableModel() {
		lines = new LinkedList<Object[]>();
	}

	@Override
	public int getColumnCount() {
		return ColumnFeature.values().length;
	}

	@Override
	public int getRowCount() {
		return lines.size();
	}
	
	@Override
    public Class<?> getColumnClass(int column) {
		Contract.checkCondition(0 <=column && column <= ColumnFeature.values().length - 1);
		return ColumnFeature.values()[column].getClass();
	}

	@Override
	public Object getValueAt(int row, int column) {
		if(row<0 || row>=getRowCount() || column<0 || column>=getColumnCount()) {
			return null;
		} 
		if (column == ColumnFeature.POINTS.ordinal()) {
			return (Double) getValueAt(row,ColumnFeature.COEF.ordinal())
					* (Double) getValueAt(row, ColumnFeature.MARK.ordinal());
		}
		return lines.get(row)[column];
	}
	
	@Override
    public String getColumnName(int index) {
		return ColumnFeature.values()[index].headerName();
	}

	@Override
	public void addEmptyRow() {
		Object[] row = new Object[getColumnCount()];
		for (int i = 0; i< row.length; i++ ) {
			row[i] = ColumnFeature.values()[i].defaultValue();
		}
		lines.add(row);
		fireTableRowsInserted(lines.size() - 1, lines.size() - 1);
	}
	
	@Override
    public boolean isCellEditable(int row, int column) {
		return row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount() -1;
	}

	@Override
	public void addRow(Object[] row) {
		Contract.checkCondition(checkRow(row));
		lines.add(row.clone());
		fireTableRowsInserted(lines.size() -1 , lines.size() - 1);
	}

	@Override
	public void clearRows() {
		int i = lines.size();
		lines.clear();
		fireTableRowsDeleted(0, i);
	}

	@Override
	public void insertEmptyRow(int index) {
		Contract.checkCondition(index >=0 && index <= getRowCount());
		Object[] row = new Object[getColumnCount()-1];
		for (int i = 0; i< row.length; i++ ) {
			row[i] = ColumnFeature.values()[i].defaultValue();
		}
		lines.add(index, row);
		fireTableRowsInserted(index-1, index-1);
	}

	@Override
	public void insertRow(int index, Object[] row) {
		Contract.checkCondition(index >=0 && index <= getRowCount());
		Contract.checkCondition(checkRow(row));
		lines.add(index, row.clone());
		fireTableRowsInserted(index-1, index-1);
	}

	@Override
	public void removeRow(int index) {
		Contract.checkCondition(index >=0 && index < getRowCount());
		lines.remove(index);
		fireTableRowsDeleted(index-1,index-1);
	}
	
	
	@Override
    public void setValueAt(Object value, int row, int column) {
		if (!isCellEditable(row,column)) {
			return;
		}
		if (value != null) {
			if (column == ColumnFeature.COEF.ordinal() || column == ColumnFeature.MARK.ordinal()) {
				lines.get(row)[column] = Double.parseDouble((String) value);
				fireTableCellUpdated(row,ColumnFeature.POINTS.ordinal());
			} else {
				lines.get(row)[column] = value;
			}
		} else {
			lines.get(row)[column] = ColumnFeature.values()[column].defaultValue();
		}
		fireTableCellUpdated(row, column);
	}
	
	private boolean checkRow(Object[] row) {
		if(row == null || row.length != ColumnFeature.values().length - 1) {
			return false;
		}
		for (int i = 0; i<row.length; i++) {
			if (row[i] == null || row[i].getClass() != ColumnFeature.values()[i].cellType()) {
				return false;
			}
		}
		return true;
	}

}
