package com.facebook.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelObject {
	private String excelPath;
	private Workbook workbook;
	private Sheet activeSheet;

	// Constructor
	public ExcelObject(String excelPath) throws IOException {
		this.excelPath = excelPath;

		File excelFile = new File(excelPath);
		FileInputStream fis = new FileInputStream(excelFile);

		if (excelPath.toLowerCase().endsWith(".xlsx")) {
			workbook = new XSSFWorkbook(fis);
		} else {
			workbook = new HSSFWorkbook(fis);
		}

		fis.close();

		activeSheet = workbook.getSheetAt(0);
	}

	public ExcelObject(String excelPath, String sheetName) throws IOException {
		this(excelPath);
		activeSheet = workbook.getSheet(sheetName);
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public Sheet getActiveSheet() {
		return activeSheet;
	}

	private boolean isSheetExist(String sheetName) {
		return workbook.getSheetIndex(sheetName) != -1;
	}

	public void closeWorkbook() throws IOException {
		this.workbook.close();
	}

	public void createSheets(String... sheetNames) throws IOException {
		File excelFile = new File(this.excelPath);
		FileOutputStream fos = null;

		if (sheetNames != null && sheetNames.length >= 1) {
			for (String sheet : sheetNames) {
				if (!isSheetExist(sheet)) {
					workbook.createSheet(sheet);
				}
			}
		}

		fos = new FileOutputStream(excelFile);
		workbook.write(fos);
		fos.close();
	}

	private int getColumnIndex(Sheet sheet, String columnName) {
		Row row = sheet.getRow(0);
		for (Cell cell : row) {
			if (columnName.equals(getCellValue(cell))) {
				return cell.getColumnIndex();
			}
		}
		return -1;
	}

	public Object getCellValue(Cell cell) {
		Object value = null;
		CellValue cellValue = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator().evaluate(cell);

		if (cellValue != null) {
			switch (cellValue.getCellTypeEnum()) {
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					value = cell.getDateCellValue();
				} else {
					value = cell.getNumericCellValue();
				}
				break;
			case BLANK:
			case _NONE:
				value = " ";
				break;
			case BOOLEAN:
				value = cell.getBooleanCellValue();
				break;
			case ERROR:
			default:
				break;
			}
		} else {
			value = " ";
		}
		return value;
	}

	public Object getCellValue(int rowPosition, int colPosition) {
		Object value = null;
		Cell cell;

		try {
			cell = activeSheet.getRow(rowPosition).getCell(colPosition, MissingCellPolicy.CREATE_NULL_AS_BLANK);
		} catch (NullPointerException e) {
			return null;
		}

		value = getCellValue(cell);
		return value;
	}

	public Object getCellValue(Sheet sheet, int rowPosition, int colPosition) {
		Object value = null;
		Cell cell;

		try {
			cell = sheet.getRow(rowPosition).getCell(colPosition, MissingCellPolicy.CREATE_NULL_AS_BLANK);
		} catch (NullPointerException e) {
			return null;
		}

		value = getCellValue(cell);
		return value;
	}

	public Object getCellValue(String sheetName, int rowPosition, int colPosition) {
		return getCellValue(workbook.getSheet(sheetName), rowPosition, colPosition);
	}

	public Object getCellValue(String sheetName, int rowPosition, String columnName) {
		return getCellValue(rowPosition, getColumnIndex(workbook.getSheet(sheetName), columnName));
	}

	public Object getCellValue(String sheetName, String columnName, String filterCondition,
			boolean... strictCompareFlag) {
		boolean strictCompare = (strictCompareFlag != null && strictCompareFlag.length >= 1) ? strictCompareFlag[0]
				: false;

		Object value = getCellValue(workbook.getSheet(sheetName),
				getRowIndex(sheetName, filterCondition, strictCompare),
				getColumnIndex(workbook.getSheet(sheetName), columnName));
		return value;
	}

	public int getRowIndex(String sheetName, String filterCondition, boolean strictCompare) {
		String[] conditions = filterCondition.split(Constants.CONDITION_SEPARATOR);
		LinkedHashMap<String, String> fullConditions = new LinkedHashMap<String, String>();

		for (String condition : conditions) {
			fullConditions.put(condition.split(Constants.CONDITIONVALUE_SEPARATOR)[0],
					condition.split(Constants.CONDITIONVALUE_SEPARATOR)[1]);
		}

		int[] columnIndices = new int[fullConditions.size()];
		Set<String> columnNames = fullConditions.keySet();
		Sheet sheet = workbook.getSheet(sheetName);

		for (String columnName : columnNames) {
			columnIndices = ArrayUtils.add(columnIndices, getColumnIndex(sheet, columnName));
			columnIndices = ArrayUtils.remove(columnIndices, 0);
		}

		for (Row row : sheet) {
			LinkedHashMap<String, String> newHashMap = new LinkedHashMap<>();

			for (int index : columnIndices) {
				newHashMap.put(String.valueOf(getCellValue(sheet.getRow(0).getCell(index))),
						String.valueOf(getCellValue(row.getCell(index))));
			}

			if (strictCompare) {
				if (newHashMap.equals(fullConditions))
					return row.getRowNum();
			} else {
				if (compareHashMapLoosely(newHashMap, fullConditions))
					return row.getRowNum();
			}
		}
		return -1;
	}

	private boolean compareHashMapLoosely(LinkedHashMap<String, String> one, LinkedHashMap<String, String> two) {
		Set<String> keySetOne = one.keySet();
		Set<String> keySetTwo = two.keySet();

		if (!CollectionUtils.isEqualCollection(keySetOne, keySetTwo))
			return false;
		for (String col1 : keySetOne) {
			for (String col2 : keySetTwo) {
				if (col1.equalsIgnoreCase(col2)) {
					// Numeric Logic
					if (StringUtils.isNumeric(one.get(col1)) && StringUtils.isNumeric(two.get(col2))) {
						if (Double.valueOf(one.get(col1)).doubleValue() != Double.valueOf(two.get(col2)).doubleValue())
							return false;
					} // Ignore case and trim
					else {
						if (!one.get(col1).trim().equalsIgnoreCase(two.get(col2).trim())) {
							return false;
						}
					}
					// TODO - Logic to be added for dateformat comparisons
				}
			}
		}
		return true;
	}

	public void setCellValue(Sheet sheet, int rowNum, int columnNum, Object valueToSet) throws IOException {
		Cell cell = null;

		try {
			if (sheet.getRow(rowNum) == null)
				cell = sheet.createRow(rowNum).createCell(columnNum);
			else if (sheet.getRow(rowNum).getCell(columnNum) == null) {
				cell = sheet.getRow(rowNum).createCell(columnNum);
			} else {
				cell = sheet.getRow(rowNum).getCell(columnNum);
			}
		} catch (NullPointerException e) {
			return;
		}

		if (valueToSet != null) {
			switch (valueToSet.getClass().getSimpleName().toUpperCase()) {
			case "INTEGER":
			case "DOUBLE":
			case "FLOAT":
			case "SHORT":
			case "BYTE":
			case "LONG":
				cell.setCellType(CellType.NUMERIC);
				cell.setCellValue(Double.parseDouble(String.valueOf(valueToSet)));
				break;
			case "DATE":
				CellStyle cellStyle = workbook.createCellStyle();
				CreationHelper createHelper = workbook.getCreationHelper();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MMM/yyyy HH:mm:ss.ms"));
				cell.setCellValue((Date) valueToSet);
				cell.setCellStyle(cellStyle);
				break;
			case "BOOLEAN":
				cell.setCellValue(Boolean.parseBoolean(String.valueOf(valueToSet)));
				cell.setCellType(CellType.BOOLEAN);
				break;
			case "STRING":
			case "OBJECT":
			default:
				cell.setCellValue(String.valueOf(valueToSet));

				if (valueToSet.toString().toLowerCase().startsWith("=")) {
					cell.setCellType(CellType.FORMULA);
				} else {
					cell.setCellType(CellType.STRING);
				}
				break;
			}
		}

		FileOutputStream fos = new FileOutputStream(excelPath);
		workbook.write(fos);
		fos.close();
	}

	public void setCellValue(String sheetName, int rowNum, int columnNum, Object valueToSet) throws IOException {
		setCellValue(workbook.getSheet(sheetName), rowNum, columnNum, valueToSet);
	}

	public HashMap<Integer, List<Object>> getExcelData(Sheet sheet, boolean... columnHeaderPresent) {
		HashMap<Integer, List<Object>> data = new HashMap<>();
		boolean headersPresent = (columnHeaderPresent != null && columnHeaderPresent.length >= 1)
				? (columnHeaderPresent[0]) : false;
		ArrayList<String> columnNames = new ArrayList<>();
		List<Object> values;

		for (Row row : sheet) {
			values = new ArrayList<Object>();
			for (Cell cell : row) {
				if (row.getRowNum() == 0) {
					values.add(getCellValue(cell));

					if (headersPresent) {
						columnNames.add(String.valueOf(getCellValue(cell)));
					}
				} else {
					String columnPrefix = "";

					if (headersPresent) {
						columnPrefix = columnNames.get(cell.getColumnIndex()) + Constants.COLUMN_SEPARATOR;
					}
					values.add(columnPrefix + getCellValue(cell));
				}
			}
			data.put(row.getRowNum(), values);
		}

		if (headersPresent) {
			data.remove(0);
		} else {
			HashMap<Integer, List<Object>> newData = new HashMap<>();
			for (int x = 0; x < data.size(); x++) {
				newData.put((x + 1), data.get(x));
			}
			data = newData;
		}
		return data;
	}

	public HashMap<Integer, List<Object>> getExcelData(String sheetName, boolean... columnHeaderPresent) {
		return getExcelData(workbook.getSheet(sheetName), columnHeaderPresent);
	}

	public HashMap<Integer, List<Object>> getExcelData(String sheetName, String filterConditions,
			boolean... strictCompareFlag) {
		HashMap<Integer, List<Object>> data = new HashMap<>();
		boolean strictCompare = (strictCompareFlag != null && strictCompareFlag.length >= 1) ? strictCompareFlag[0]
				: false;

		Sheet sheet = workbook.getSheet(sheetName);
		int cnt = 1;
		int[] targetRowIndices = getRowIndices(sheet, filterConditions, strictCompare);

		if (targetRowIndices.length == 1 && targetRowIndices[0] == -1) {
			return data;
		} else {
			HashMap<Integer, List<Object>> newData = getExcelData(sheet, false);

			for (int i : targetRowIndices) {
				data.put(cnt++, newData.get(i + 1));
			}
		}
		return data;
	}

	public HashMap<Integer, List<Object>> getExcelData(String sheetName, String filterConditions, String columnNames,
			boolean... strictCompareFlag) {
		HashMap<Integer, List<Object>> data = new HashMap<>();
		boolean strictCompare = (strictCompareFlag != null && strictCompareFlag.length >= 1) ? strictCompareFlag[0]
				: false;
		Sheet sheet = workbook.getSheet(sheetName);
		int cnt = 1;
		int[] targetRowIndices = getRowIndices(sheet, filterConditions, strictCompare);
		if (targetRowIndices.length == 1 && targetRowIndices[0] == -1) {
			return data;
		} else {

			HashMap<Integer, List<Object>> newData = getExcelColumnsData(sheetName, columnNames);

			for (int i : targetRowIndices) {
				data.put(cnt++, newData.get(i));
			}
		}
		return data;
	}

	public List<HashMap<Integer, List<Object>>> getExcelData() {
		List<HashMap<Integer, List<Object>>> data = new ArrayList<HashMap<Integer, List<Object>>>();

		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			data.add(getExcelData(workbook.getSheetAt(i)));
		}
		return data;
	}

	public HashMap<Integer, List<Object>> getExcelColumnsData(String sheetName, String columnNames) {
		HashMap<Integer, List<Object>> data = new HashMap<>();
		Sheet sheet = workbook.getSheet(sheetName);

		String[] arrColumns = columnNames.split(Constants.CONDITION_SEPARATOR);
		List<Object> values;

		for (Row row : sheet) {
			values = new ArrayList<>();

			if (row.getRowNum() != 0) {
				for (String string : arrColumns) {
					int colIndex = getColumnIndex(sheet, string);
					Cell cell = row.getCell(colIndex);
					values.add(getCellValue(cell));
				}
				data.put(row.getRowNum(), values);
			}
		}
		return data;
	}

	private int[] getRowIndices(Sheet sheet, String filterCondition, boolean strictCompare) {
		ArrayList<Integer> list = new ArrayList<>();
		String[] conditions = filterCondition.split(Constants.CONDITION_SEPARATOR);
		LinkedHashMap<String, String> fullConditions = new LinkedHashMap<String, String>();

		for (String condition : conditions) {
			if (!condition.isEmpty()) // Added this condition to avoid exception
										// when blank/no condition is given
			{
				fullConditions.put(condition.split(Constants.CONDITIONVALUE_SEPARATOR)[0],
						condition.split(Constants.CONDITIONVALUE_SEPARATOR)[1]);
			}
		}

		int[] columnIndices = new int[fullConditions.size()];
		Set<String> columnNames = fullConditions.keySet();

		for (String columnName : columnNames) {
			columnIndices = ArrayUtils.add(columnIndices, getColumnIndex(sheet, columnName));
			columnIndices = ArrayUtils.remove(columnIndices, 0);
		}

		for (Row row : sheet) {
			LinkedHashMap<String, String> newHashMap = new LinkedHashMap<>();

			for (int index : columnIndices) {
				newHashMap.put(String.valueOf(getCellValue(sheet.getRow(0).getCell(index))),
						String.valueOf(getCellValue(row.getCell(index))));
			}

			if (strictCompare) {
				if (newHashMap.equals(fullConditions))
					list.add(row.getRowNum());
			} else {
				if (compareHashMapLoosely(newHashMap, fullConditions))
					list.add(row.getRowNum());
			}
		}

		if (list.size() == 0)
			list.add(-1);

		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}

	public ArrayList<Object> getEntireColumnData(Sheet sheet, int columnIndex) {
		ArrayList<Object> data = new ArrayList<>();
		for (Row row : sheet) {
			data.add(getCellValue(row.getCell(columnIndex)));
		}
		return data;
	}

	public ArrayList<Object> getEntireColumnData(String sheetName, int columnIndex) {
		return getEntireColumnData(workbook.getSheet(sheetName), columnIndex);
	}

	public ArrayList<Object> getEntireColumnData(String sheetName, String columnName) {
		ArrayList<Object> data = new ArrayList<>();
		data.addAll(getEntireColumnData(sheetName, getColumnIndex(workbook.getSheet(sheetName), columnName)));
		data.remove(0);
		return data;
	}
}