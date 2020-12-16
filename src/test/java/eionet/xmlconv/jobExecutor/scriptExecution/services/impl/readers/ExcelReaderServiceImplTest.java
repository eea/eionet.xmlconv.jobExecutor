package eionet.xmlconv.jobExecutor.scriptExecution.services.impl.readers;

import eionet.xmlconv.jobExecutor.Properties;
import eionet.xmlconv.jobExecutor.TestConstants;
import eionet.xmlconv.jobExecutor.exceptions.ConversionException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = { Properties.class })
@RunWith(SpringRunner.class)
public class ExcelReaderServiceImplTest {

    @Test
    public void testTrimIntegerValues() throws Exception {
        File inFile = new File(this.getClass().getClassLoader().getResource(TestConstants.SEED_RIVERS_XLS)
                .getFile());

        ExcelReaderServiceImpl excel = new ExcelReaderServiceImpl(false);
        excel.initReader(inFile);

        Workbook workbook = excel.getWorkbook();

        //test value in Mean column
        Cell cell = workbook.getSheetAt(0).getRow(1).getCell(13);
        String value = excel.cellValueToString(cell, "xs:integer");
        assertEquals("54.675000", value);
    }
    @Test
    public void testTrimStringValues() throws Exception {
        File inFile = new File(this.getClass().getClassLoader().getResource(TestConstants.SEED_RIVERS_XLS)
                .getFile());

        ExcelReaderServiceImpl excel = new ExcelReaderServiceImpl(false);
        excel.initReader(inFile);

        Workbook workbook = excel.getWorkbook();

        //test string value trimming. PeriodLength column
        Cell cell = workbook.getSheetAt(0).getRow(1).getCell(4);
        String value = excel.cellValueToString(cell, "xs:string");
        assertEquals("Trim this string", value);
    }

    @Test
    public void testThousantSeparator() throws ConversionException {
        File inFile = new File(getClass().getClassLoader().getResource(TestConstants.SEED_READER_XLS)
                .getFile());

        ExcelReaderServiceImpl excel = new ExcelReaderServiceImpl(false);
        excel.initReader(inFile);

        Workbook workbook = excel.getWorkbook();

        //test thousant separator
        Cell cell = workbook.getSheetAt(0).getRow(1).getCell(1);
        String value = excel.cellValueToString(cell, "xs:string");
        assertEquals("123123.21", value);
    }

    @Test
    public void testIntegerParsing() throws ConversionException {
        File inFile = new File(getClass().getClassLoader().getResource(TestConstants.SEED_READER_XLS)
                .getFile());

        ExcelReaderServiceImpl excel = new ExcelReaderServiceImpl(false);
        excel.initReader(inFile);

        Workbook workbook = excel.getWorkbook();

        //test thousant separator
        Cell cell = workbook.getSheetAt(0).getRow(2).getCell(1);
        String value = excel.cellValueToString(cell, "xs:string");
        assertEquals("1", value);
    }

    @Test
    public void testDecimalValue() throws ConversionException {
        File inFile = new File(getClass().getClassLoader().getResource(TestConstants.SEED_READER_XLS)
                .getFile());

        ExcelReaderServiceImpl excel = new ExcelReaderServiceImpl(false);
        excel.initReader(inFile);

        Workbook workbook = excel.getWorkbook();

        //test thousant separator
        Cell cell = workbook.getSheetAt(0).getRow(3).getCell(1);
        String value = excel.cellValueToString(cell, "xs:string");
        assertEquals("0.00001", value);
    }

    @Test
    public void testGreekLocale() throws ConversionException {
        File inFile = new File(getClass().getClassLoader().getResource(TestConstants.SEED_READER_XLS)
                .getFile());

        ExcelReaderServiceImpl excel = new ExcelReaderServiceImpl(false);
        excel.initReader(inFile);

        Workbook workbook = excel.getWorkbook();

        //test thousant separator
        Cell cell = workbook.getSheetAt(0).getRow(4).getCell(1);
        String value = excel.cellValueToString(cell, "xs:string");
        assertEquals("123123.123", value);
    }
}
