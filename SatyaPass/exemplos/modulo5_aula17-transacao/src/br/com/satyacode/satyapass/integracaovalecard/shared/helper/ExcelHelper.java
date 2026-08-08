package br.com.satyacode.satyapass.integracaovalecard.shared.helper;

import br.com.satyacode.satyapass.integracaovalecard.contants.ExcelConstants;
import br.com.satyacode.satyapass.integracaovalecard.shared.model.ItemSankhya;
import br.com.satyacode.satyapass.integracaovalecard.shared.model.ModelExcelDTO;
import br.com.satyacode.satyapass.integracaovalecard.shared.model.ModelExcelDTO.ValidaColuna;
import br.com.satyacode.satyapass.integracaovalecard.shared.utils.ArquivoUtils;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {

    public static void main(String[] args) {
        String diretorioDoArquivo = "/Users/danielaraujo/Desktop/SatyaPass/integracao_valecard/resources/modelo_excel.xls";
        gerenciarArquivo(diretorioDoArquivo);
    }

    public static ArrayList<ItemSankhya> processarArquivo(InputStream inputStream) throws Exception {
        ArrayList<ItemSankhya> itensSankhya;
        try{
            Workbook workbook;
            byte[] dados = IOUtils.toByteArray(inputStream);
            InputStream inputStreamParaTika = new ByteArrayInputStream(dados);
            InputStream inputStreamParaPoi = new ByteArrayInputStream(dados);
            String tipoArquivo = ArquivoUtils.getFileTypeByTika(inputStreamParaTika);
            if (!ArquivoUtils.isExcelMime(tipoArquivo)) {
                throw new Exception("Formato de arquivo inválido. Anexe um arquivo .xls ou .xlsx.");
            }
            if (tipoArquivo.contains("openxml") || tipoArquivo.contains("ooxml")) {
                workbook = new XSSFWorkbook(inputStreamParaPoi);
            } else {
                workbook = new HSSFWorkbook(inputStreamParaPoi);
            }
            itensSankhya = decodificarPlanilha(workbook);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao processar o arquivo excel: " + e.getMessage());
        }
        return itensSankhya;
    }

    public static void gerenciarArquivo(String path){
        try{
            InputStream file = new FileInputStream(path);
            Workbook workbook;
            if(path.toLowerCase().endsWith(".xls")){
                System.out.println("Lendo arquvio XLS");
                workbook = new HSSFWorkbook(file);
            } else if (path.toLowerCase().endsWith(".xlsx")){
                System.out.println("Lendo arquvio XLSX");
                workbook = new XSSFWorkbook(file);
            }else {
                throw new Exception("O formato do arquivo não é um arquivo de excel");
            }
            ArrayList<ItemSankhya> itemSankhyas = decodificarPlanilha(workbook);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<ItemSankhya> decodificarPlanilha(Workbook workbook) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);

        ArrayList<ModelExcelDTO.ColunaExcel> listaDeColunas = popularExcel();

        ValidaColuna retornoDaValidacao = realizaValidacao(sheet, listaDeColunas);
        if (retornoDaValidacao.isAcionado()) {
            throw new Exception(String.format("Identificamos um problema! Esperavamos a coluna: <b>%s</b> - Recebemos a coluna:  <b>%s</b>. Atualize a planilha e importe novamente.", retornoDaValidacao.getColuna().getNomeColuna(), retornoDaValidacao.getColunaRecebida()));
        }

        sheet.removeRow(sheet.getRow(0));
        ArrayList<ItemSankhya> listaItemSankhya = new ArrayList<>();
        for (Row row :sheet) {
            System.out.println(row.getRowNum());
            ItemSankhya itemSankhya = new ItemSankhya();
            for (Cell cell : row) {
                ModelExcelDTO.ColunaExcel colunaExcel = buscarColunaPorIndice(listaDeColunas, cell.getColumnIndex());
                if(StringUtils.isNotEmpty(colunaExcel.getNomeColuna())){
                    Object object = converterDados(colunaExcel, cell);
                    colunaExcel.setConteudo(object);
                    System.out.println(colunaExcel);
                    itemSankhya = decodificarColunasDaPlanilhaParaOItemSankhya(colunaExcel, itemSankhya);
                }
            }
            listaItemSankhya.add(itemSankhya);
        }

        return listaItemSankhya;

    }

    private static ItemSankhya decodificarColunasDaPlanilhaParaOItemSankhya(ModelExcelDTO.ColunaExcel colunaExcel, ItemSankhya itemSankhya){

        switch (colunaExcel.getNomeColuna()){
            case ExcelConstants.PLACA:
                itemSankhya.setPlaca((String) colunaExcel.getConteudo());
                break;
            case ExcelConstants.DESCRICAO_VEICULO:
                itemSankhya.setDescricaoDoVeiculo((String) colunaExcel.getConteudo());
                break;
            case ExcelConstants.DATA_ABASTECIMENTO:
                itemSankhya.setData((Timestamp) colunaExcel.getConteudo());
                break;
            case ExcelConstants.NRO_CARTAO:
                itemSankhya.setNroCartao((String) colunaExcel.getConteudo());
                break;
            case ExcelConstants.MOTORISTA:
                itemSankhya.setMotorista((String) colunaExcel.getConteudo());
                break;
            case ExcelConstants.HORIMETRO:
                itemSankhya.setHorimetro((BigDecimal) colunaExcel.getConteudo());
                break;
            case ExcelConstants.PRODUTO:
                itemSankhya.setProduto((String) colunaExcel.getConteudo());
                break;
            case ExcelConstants.QUANTIDADE:
                itemSankhya.setQuantidade((BigDecimal) colunaExcel.getConteudo());
                break;
            case ExcelConstants.DISTANCIA:
                itemSankhya.setDistancia((BigDecimal) colunaExcel.getConteudo());
                break;

        }

        return itemSankhya;
    }

    private static Object converterDados(ModelExcelDTO.ColunaExcel colunaExcel, Cell cell) throws Exception {
        Object retorno = null;
        try {
            switch (colunaExcel.getTipoColuna()) {
                case DATA:
                    retorno = TimeUtils.toTimestamp(ArquivoUtils.formatarCelulaComoString(cell), "dd/MM/yyyy");
                    break;
                case NUMERO:
                    retorno = BigDecimalUtil.valueOf(ArquivoUtils.formatarCelulaComoString(cell, 0));
                    break;
                case STRING:
                    retorno = ArquivoUtils.formatarCelulaComoString(cell);
                    break;
                case DECIMAL:
                    retorno = BigDecimalUtil.valueOf(ArquivoUtils.formatarCelulaComoString(cell, 5));
                    break;
            }
            return retorno;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(String.format("Erro ao converter a coluna: %s - Tipo: %s -  Valor: %s. Mensagem: %s", colunaExcel.getNomeColuna(), ArquivoUtils.decodificarColunaPeloTipo(cell), ArquivoUtils.formatarCelulaComoString(cell), e.getMessage()));
        }
    }

    private static ModelExcelDTO.ColunaExcel buscarColunaPorIndice(ArrayList<ModelExcelDTO.ColunaExcel> colunas, int index) {
        for (ModelExcelDTO.ColunaExcel coluna : colunas) {
            if (coluna.getIndex() == index) {
                return coluna;
            }
        }
        return new ModelExcelDTO.ColunaExcel();
    }

    private static ArrayList<ModelExcelDTO.ColunaExcel> popularExcel(){
        ArrayList<ModelExcelDTO.ColunaExcel> arrayColunas = new ArrayList<>();
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.PLACA, ModelExcelDTO.EnumTipoColuna.STRING, ExcelConstants.PLACA_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.DESCRICAO_VEICULO, ModelExcelDTO.EnumTipoColuna.STRING, ExcelConstants.DESCRICAO_VEICULO_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.NRO_CARTAO, ModelExcelDTO.EnumTipoColuna.STRING, ExcelConstants.NRO_CARTAO_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.DATA_ABASTECIMENTO, ModelExcelDTO.EnumTipoColuna.DATA, ExcelConstants.DATA_ABASTECIMENTO_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.MOTORISTA, ModelExcelDTO.EnumTipoColuna.STRING, ExcelConstants.MOTORISTA_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.HORIMETRO, ModelExcelDTO.EnumTipoColuna.NUMERO, ExcelConstants.HORIMETRO_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.PRODUTO, ModelExcelDTO.EnumTipoColuna.STRING, ExcelConstants.PRODUTO_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.DISTANCIA, ModelExcelDTO.EnumTipoColuna.DECIMAL, ExcelConstants.DISTANCIA_INDEX ));
        arrayColunas.add(new ModelExcelDTO.ColunaExcel(ExcelConstants.QUANTIDADE, ModelExcelDTO.EnumTipoColuna.DECIMAL, ExcelConstants.QUANTIDADE_INDEX ));
        return arrayColunas;
    }

    private static ValidaColuna realizaValidacao(Sheet sheet, List<ModelExcelDTO.ColunaExcel> lista) {
        ValidaColuna valida = new ValidaColuna(false);
        for (ModelExcelDTO.ColunaExcel info : lista) {
            valida.setColuna(info);
            String coluna = ArquivoUtils.formatarCelulaComoString(sheet.getRow(0).getCell(info.getIndex()));
            if (!coluna.equals(info.getNomeColuna())) {
                valida.setColunaRecebida(coluna);
                valida.setAcionado(true);
                break;
            }
        }
        return valida;
    }

}
