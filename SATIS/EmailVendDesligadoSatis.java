package br.com.satis.extensions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;

/**
 * Versão Java da Procedure STP_EMAILVENDDESLIGADO_SATIS
 * Realiza o envio de e-mail de detalhamento de acerto para vendedores desligados.
 */
public class EmailVendDesligadoSatis implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

        for (Registro reg : ctx.getLinhas()) {
            
            BigDecimal nuFech = (BigDecimal) reg.getCampo("NUFECH");
            BigDecimal sequencia = (BigDecimal) reg.getCampo("SEQUENCIA");
            
            if (nuFech == null || sequencia == null) continue;

            JdbcWrapper jdbc = null;
            NativeSql sql = null;
            ResultSet rs = null;

            try {
                jdbc = JapeFactory.getEntityFacade().getJdbcWrapper();
                sql = new NativeSql(jdbc);

                // 1. Busca dados do fechamento e vendedor
                sql.appendSql("SELECT VEN.AD_EMAILPART, NVL(FIN.DIASTRAB,0) AS DIASTRAB, ");
                sql.appendSql("ROUND(NVL(FIN.VALOR, 0) - NVL(FIN.VLRADTCOMP, 0),2) AS VALOR, ");
                sql.appendSql("NVL(FIN.REMFIXA, 0) AS REMFIXA, INITCAP(VEN.APELIDO) AS APELIDO, ");
                sql.appendSql("VEN.CODVEND, NVL(FIN.REEMBOLSO, 0) AS REEMBOLSO, ");
                sql.appendSql("NVL(FIN.ADIANTAMENTO, 0) + NVL(FIN.ADIANTFIXO,0) AS ADIANTCOMISSAO, ");
                sql.appendSql("NVL(FIN.EXTRA, 0) AS EXTRAS, FIN.DESLIGADO ");
                sql.appendSql("FROM AD_DBFECHCOMFIN FIN ");
                sql.appendSql("INNER JOIN TGFVEN VEN ON FIN.CODVEND = VEN.CODVEND ");
                sql.appendSql("WHERE FIN.NUFECH = :NUFECH AND FIN.SEQUENCIA = :SEQ");
                
                sql.setNamedParameter("NUFECH", nuFech);
                sql.setNamedParameter("SEQ", sequencia);
                
                rs = sql.executeQuery();

                if (rs.next()) {
                    String email = rs.getString("AD_EMAILPART");
                    BigDecimal dias = rs.getBigDecimal("DIASTRAB");
                    BigDecimal valorComis = rs.getBigDecimal("VALOR");
                    BigDecimal remFixa = rs.getBigDecimal("REMFIXA");
                    String apelido = rs.getString("APELIDO");
                    BigDecimal codVend = rs.getBigDecimal("CODVEND");
                    BigDecimal reembolso = rs.getBigDecimal("REEMBOLSO");
                    BigDecimal adiant = rs.getBigDecimal("ADIANTCOMISSAO");
                    BigDecimal extras = rs.getBigDecimal("EXTRAS");
                    String desligado = rs.getString("DESLIGADO");

                    // Validações
                    if (!"SIM".equalsIgnoreCase(desligado)) {
                        ctx.setMensagemRetorno("E-mail não enviado para " + apelido + ". Vendedor não está marcado como Desligado.");
                        continue;
                    }
                    if (dias.compareTo(BigDecimal.ZERO) == 0 && remFixa.compareTo(BigDecimal.ZERO) > 0) {
                        ctx.setMensagemRetorno("Ajuste a quantidade de dias trabalhados para " + apelido + ".");
                        continue;
                    }

                    // 2. Busca Comissão Futura
                    sql.clean();
                    sql.appendSql("SELECT NVL(SUM(VLRCOM), 0) AS VLRFUT FROM AD_DBFECHCOMNOTASA WHERE NUFECH = :NUFECH AND CODVEND = :CODVEND");
                    sql.setNamedParameter("NUFECH", nuFech);
                    sql.setNamedParameter("CODVEND", codVend);
                    ResultSet rsFut = sql.executeQuery();
                    BigDecimal vlrFut = BigDecimal.ZERO;
                    if (rsFut.next()) vlrFut = rsFut.getBigDecimal("VLRFUT");
                    rsFut.close();

                    // 3. Calcula Valor Líquido
                    BigDecimal outros = extras.add(reembolso);
                    BigDecimal liquido = remFixa.add(valorComis).add(vlrFut).add(outros).subtract(adiant);

                    // 4. Monta HTML do Corpo
                    String corpo = buildHtmlBody(apelido, remFixa, valorComis, vlrFut, outros, adiant, liquido, df);

                    // 5. Insere na Fila de E-mail (TMDFMG)
                    sql.clean();
                    sql.appendSql("SELECT NVL(MAX(CODFILA),0) + 1 AS PROXFILA FROM TMDFMG");
                    ResultSet rsFila = sql.executeQuery();
                    BigDecimal codFila = BigDecimal.ONE;
                    if (rsFila.next()) codFila = rsFila.getBigDecimal("PROXFILA");
                    rsFila.close();

                    sql.clean();
                    sql.appendSql("INSERT INTO TMDFMG (CODFILA, ASSUNTO, DTENTRADA, STATUS, CODCON, TENTENVIO, MENSAGEM, TIPOENVIO, MAXTENTENVIO, EMAIL, CODSMTP) ");
                    sql.appendSql("VALUES (:CODFILA, :ASSUNTO, SYSDATE, 'Pendente', 0, 0, :CORPO, 'E', 3, :EMAIL, 33)");
                    sql.setNamedParameter("CODFILA", codFila);
                    sql.setNamedParameter("ASSUNTO", "Faturamento " + apelido + ",");
                    sql.setNamedParameter("CORPO", corpo);
                    sql.setNamedParameter("EMAIL", email);
                    sql.executeUpdate();

                    // 6. Atualiza Fechamento
                    sql.clean();
                    sql.appendSql("UPDATE AD_DBFECHCOMFIN SET EMAILVEND = 'S', CODFILA = :CODFILA WHERE NUFECH = :NUFECH AND SEQUENCIA = :SEQ");
                    sql.setNamedParameter("CODFILA", codFila);
                    sql.setNamedParameter("NUFECH", nuFech);
                    sql.setNamedParameter("SEQ", sequencia);
                    sql.executeUpdate();
                }
            } finally {
                if (rs != null) rs.close();
                if (sql != null) NativeSql.releaseResources(sql);
            }
        }
        ctx.setMensagemRetorno("Emails gerados com sucesso!");
    }

    private String buildHtmlBody(String apelido, BigDecimal ajuda, BigDecimal comis, BigDecimal fut, BigDecimal outros, BigDecimal compensar, BigDecimal liquido, DecimalFormat df) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("table { width: 100%; border-collapse: collapse; font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; margin-top: 20px; }");
        html.append("th, td { padding: 12px; text-align: left; border: 1px solid #e0e0e0; }");
        html.append("th { background-color: #f8f9fa; color: #333; font-weight: 600; }");
        html.append(".total-row { background-color: #f1f8ff; font-weight: bold; color: #2c3e50; }");
        html.append(".value-column { text-align: right; white-space: nowrap; }");
        html.append("</style></head><body>");
        html.append("<p>Prezado(a) <strong>").append(apelido).append("</strong>,</p>");
        html.append("<p>Esperamos que esta mensagem lhe encontre bem!</p>");
        html.append("<p>Conforme o encerramento do contrato de prestação de serviços firmado com a Satis, encaminhamos, com a devida consideração, o detalhamento dos valores referentes ao seu acerto.</p>");
        html.append("<h3>Detalhamento dos valores:</h3>");
        html.append("<table><thead><tr><th>Descrição</th><th class=\"value-column\">Valor (R$)</th></tr></thead><tbody>");
        html.append("<tr><td>Ajuda de custo</td><td class=\"value-column\">").append(df.format(ajuda)).append("</td></tr>");
        html.append("<tr><td>Comissão do mês</td><td class=\"value-column\">").append(df.format(comis)).append("</td></tr>");
        html.append("<tr><td>Comissão futura</td><td class=\"value-column\">").append(df.format(fut)).append("</td></tr>");
        html.append("<tr><td>Outros valores</td><td class=\"value-column\">").append(df.format(outros)).append("</td></tr>");
        html.append("<tr><td>Valor a compensar</td><td class=\"value-column\">").append(df.format(compensar)).append("</td></tr>");
        html.append("<tr class=\"total-row\"><td>Valor líquido a receber</td><td class=\"value-column\">").append(df.format(liquido)).append("</td></tr>");
        html.append("</tbody></table>");
        html.append("<p>Informamos que o pagamento será realizado no prazo de <strong>10 (dez) dias</strong>, contados a partir do recebimento da respectiva Nota Fiscal.</p>");
        html.append("<p>Caso haja qualquer dúvida ou necessidade de esclarecimentos adicionais, permanecemos inteiramente à disposição para auxiliá-lo(a).</p>");
        html.append("<p>Atenciosamente,<br /><strong>Equipe Satis</strong></p>");
        html.append("</body></html>");
        return html.toString();
    }
}
