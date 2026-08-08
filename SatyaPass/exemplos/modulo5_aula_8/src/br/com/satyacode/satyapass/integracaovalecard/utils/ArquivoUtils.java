package br.com.satyacode.satyapass.integracaovalecard.utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArquivoUtils {

    public static String formatarCelulaComoString(Cell cell, int... casasDecimais) {
        if (cell == null) {
            return "";
        }

        int cellType = cell.getCellType();
        int decimal = (casasDecimais.length > 0) ? casasDecimais[0] : 0;

        try {
            switch (cellType) {
                case Cell.CELL_TYPE_NUMERIC:

                    if (DateUtil.isCellDateFormatted(cell)) {
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = cell.getDateCellValue();
                        return df.format(date);
                    } else {
                        BigDecimal valorDecimal = BigDecimal.valueOf(cell.getNumericCellValue())
                                .setScale(decimal, RoundingMode.HALF_UP);
                        return valorDecimal.toPlainString();
                    }

                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();

                case Cell.CELL_TYPE_BOOLEAN:
                    return Boolean.toString(cell.getBooleanCellValue());

                case Cell.CELL_TYPE_FORMULA:
                    return cell.getCellFormula();

                case Cell.CELL_TYPE_ERROR:
                    return Byte.toString(cell.getErrorCellValue());

                case Cell.CELL_TYPE_BLANK:
                    return "";

                default:
                    return "Tipo de célula não reconhecido.";
            }
        } catch (Exception e) {
            return String.format("Erro ao formatar célula: %s", e.getMessage());
        }
    }

    public static String decodificarColunaPeloTipo(Cell cell) {
        String mensagem = "";
        switch (cell.getCellType()) {
            case 0:
                mensagem = "Númerico ou Data";
                break;
            case 1:
                mensagem = "Texto";
                break;
            case 2:
                mensagem = "Formula";
                break;
            case 3:
                mensagem = "Vazio";
                break;
            case 4:
                mensagem = "Booleano";
                break;
            case 5:
                mensagem = "Error";
                break;

        }
        return mensagem;
    }
}
