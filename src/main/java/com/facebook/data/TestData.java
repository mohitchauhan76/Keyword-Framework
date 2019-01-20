package com.facebook.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.DataProvider;

import com.facebook.utils.ExcelObject;

public class TestData {
	@DataProvider(name = "getTest")
	public Object[][] getBookData() throws IOException {
		ExcelObject excelObj = new ExcelObject("Framework\\Scripts\\TestScripts.xlsx");
		HashMap<Integer, List<Object>> data = excelObj.getExcelData("TestCases", "Execute=Yes");

		Object[][] output = new Object[data.size()][];

		for (int i = 1; i <= data.size(); i++) {
			List<Object> listData = data.get(i);

			Object[] arrData = listData.toArray();
			output[i - 1] = arrData;
		}
		
		excelObj.closeWorkbook();
		
		return output;
	}
}
