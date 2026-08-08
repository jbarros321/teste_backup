package br.com.satyacode.satyapass.integracaovalecard;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;

public class Main {

    public static void main(String[] args) throws Exception {

        String diretorioDoArquivo = "/Users/danielaraujo/Desktop/SatyaPass/integracao_valecard/resources/modelo_excel.xls";
        try{
            InputStream file = new FileInputStream(diretorioDoArquivo);

            Workbook workbook;
            if(diretorioDoArquivo.toLowerCase().endsWith(".xls")){
                System.out.println("Lendo arquvio XLS");
                workbook = new HSSFWorkbook(file);
            } else if (diretorioDoArquivo.toLowerCase().endsWith(".xlsx")){
                System.out.println("Lendo arquvio XLSX");
                workbook = new XSSFWorkbook(file);
            }else {
                throw new Exception("O formato do arquivo não é um arquivo de excel");
            }

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row :sheet){

                for (Cell cell : row){
                    switch (cell.getCellType()){
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.println(cell.getNumericCellValue()+ "\t");
                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.println(cell.getStringCellValue()+ "\t");
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            System.out.println(cell.getBooleanCellValue()+ "\t");
                            break;
                        default:
                            throw new Exception("Não tratado");
                    }
                }
            }
            file.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Hello");
    }
}
