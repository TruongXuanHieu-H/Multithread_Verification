package com.mtv.output;

import com.mtv.debug.DebugHelper;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.Sheet;
import jxl.format.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ExportToExcell {

    private static String filePath = "MTV_Report.xls";
    private static String sheetName = "Report sheet";

    private static File file = null;

    private static int labelRow = 4;


    public static void Export(ArrayList<ExcellReporter> reporters) throws IOException, BiffException, WriteException {
        if (file == null) {
            file = new File(filePath);
        }
        int currentReportCases = GetCurrentReportCases(file);

        Workbook workbook = Workbook.getWorkbook(file);
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(file, workbook);
        WritableSheet writableSheet = writableWorkbook.getSheet(sheetName);
        for (ExcellReporter reporter : reporters ) {
            WriteToRow(currentReportCases + labelRow + 1, currentReportCases + 1, reporter, writableSheet);
            currentReportCases += 1;
        }

        Label newReportCasesNumber = new Label(0, 3, currentReportCases + "");
        writableSheet.addCell(newReportCasesNumber);

        writableWorkbook.write();
        writableWorkbook.close();
        workbook.close();
    }

    private static int GetCurrentReportCases(File file) throws WriteException, IOException, BiffException {
        Workbook w;
        try {
            w = Workbook.getWorkbook(file);
        } catch (BiffException | IOException e) {
            CreateNewWorkBook(file);
            return GetCurrentReportCases(file);
        }
        Sheet sheet = w.getSheet(sheetName);
        int currentReports = Integer.parseInt(sheet.getCell(0, 3).getContents());
        w.close();
        return currentReports;
    }

    private static void CreateNewWorkBook(File file) throws IOException, WriteException {
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);
        WritableSheet sheet = writableWorkbook.createSheet(sheetName, 0);

        // Create label
        Label title = new Label(0, 0, "Multi threading verification tool report");
        sheet.addCell(title);

        Label curReportsNumberTitle = new Label(0, 2, "Reported cases: ");
        sheet.addCell(curReportsNumberTitle);
        Label curReportsNumber = new Label(0, 3, "0");
        sheet.addCell(curReportsNumber);

        Label index = new Label(0, labelRow, "Index");
        sheet.addCell(index);

        Label programName = new Label(1, labelRow, "Program");
        sheet.addCell(programName);

        Label time = new Label(2, labelRow, "Verify date");
        sheet.addCell(time);

        Label source = new Label(3, labelRow, "Source");
        sheet.addCell(source);

        Label result = new Label(4, labelRow, "Verification result");
        sheet.addCell(result);

        Label constraints = new Label(5, labelRow, "Number of generated constraints");
        sheet.addCell(constraints);

        Label consGenTime = new Label(6, labelRow, "Constraints generation time (milliseconds)");
        sheet.addCell(consGenTime);

        Label solveTime = new Label(7, labelRow, "Solve time (milliseconds)");
        sheet.addCell(solveTime);

        writableWorkbook.write();
        writableWorkbook.close();
    }

    private static void WriteToRow(int row, int index, ExcellReporter reporter, WritableSheet sheet) throws WriteException {
        Label indexLabel = new Label(0, row, index + "");
        sheet.addCell(indexLabel);

        Label programName = new Label(1, row, reporter.programName);
        sheet.addCell(programName);

        Label verifyDate = new Label(2, row, reporter.dateTime.toString());
        sheet.addCell(verifyDate);

        String sourceCodeContent = "";
        for (String line : reporter.sourceCode) {
            sourceCodeContent += line + "\n";
        }
        Label sourceCode = new Label(3, row, sourceCodeContent);
        sheet.addCell(sourceCode);

        Label verificationResult = new Label(4, row, reporter.verificationResult);
        WritableCellFormat resultFormat = new WritableCellFormat();
        if (reporter.verificationResult.equals("SATISFIABLE")) {
            resultFormat.setBackground(Colour.RED);
        } else if (reporter.verificationResult.equals("UNSATISFIABLE")) {
            resultFormat.setBackground(Colour.BRIGHT_GREEN);
        } else {
            resultFormat.setBackground(Colour.YELLOW);
        }
        verificationResult.setCellFormat(resultFormat);
        sheet.addCell(verificationResult);

        Label constraints = new Label(5, row, reporter.constraintsGenerated + "");
        sheet.addCell(constraints);

        Label consGenTime = new Label(6, row, reporter.consGenTime + "");
        sheet.addCell(consGenTime);

        Label solveTime = new Label(7, row, reporter.solveTime + "");
        sheet.addCell(solveTime);
    }
}
