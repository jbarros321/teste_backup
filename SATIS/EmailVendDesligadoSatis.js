function doAction(context) {
    var linhas = context.getLinhas();
    var formatter = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    for (var i = 0; i < linhas.length; i++) {
        var linha = linhas[i];
        var nuFech = linha.getCampo("NUFECH");
        var sequencia = linha.getCampo("SEQUENCIA");

        if (nuFech && sequencia) {
            var sql = context.getNativeSql();
            
            // 1. Busca dados principais
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
            
            var rs = sql.executeQuery();

            if (rs.next()) {
                var email = rs.getString("AD_EMAILPART");
                var apelido = rs.getString("APELIDO");
                var codVend = rs.getBigDecimal("CODVEND");
                var remFixa = rs.getBigDecimal("REMFIXA");
                var valorComis = rs.getBigDecimal("VALOR");
                var adiant = rs.getBigDecimal("ADIANTCOMISSAO");
                var extras = rs.getBigDecimal("EXTRAS");
                var reembolso = rs.getBigDecimal("REEMBOLSO");
                var desligado = rs.getString("DESLIGADO");

                if (desligado !== "SIM" || !email) {
                    rs.close();
                    continue;
                }

                // 2. Busca Comissão Futura
                sql.clean();
                sql.appendSql("SELECT NVL(SUM(VLRCOM), 0) AS VLRFUT FROM AD_DBFECHCOMNOTASA WHERE NUFECH = :NUFECH AND CODVEND = :CODVEND");
                sql.setNamedParameter("NUFECH", nuFech);
                sql.setNamedParameter("CODVEND", codVend);
                var rsFut = sql.executeQuery();
                var vlrFut = rsFut.next() ? rsFut.getBigDecimal("VLRFUT") : 0;
                rsFut.close();

                // 3. Calcula total
                var outros = Number(extras) + Number(reembolso);
                var liquido = Number(remFixa) + Number(valorComis) + Number(vlrFut) + outros - Number(adiant);

                // 4. Monta Corpo HTML
                var corpo = buildHtmlBody(apelido, remFixa, valorComis, vlrFut, outros, adiant, liquido, formatter);

                // 5. Envia E-mail via Serviço (JSON/Objeto)
                try {
                    context.callService("MailService.sendEmail", {
                        email: {
                            assunto: "Faturamento " + apelido + ",",
                            corpo: corpo,
                            destinatario: email,
                            codSmtp: 33 
                        }
                    });

                    // 6. Atualiza registro apenas se enviou com sucesso
                    sql.clean();
                    sql.appendSql("UPDATE AD_DBFECHCOMFIN SET EMAILVEND = 'S' WHERE NUFECH = :NUFECH AND SEQUENCIA = :SEQ");
                    sql.setNamedParameter("NUFECH", nuFech);
                    sql.setNamedParameter("SEQ", sequencia);
                    sql.executeUpdate();
                } catch (e) {
                    // Erro ao enviar e-mail
                }
            }
            rs.close();
            sql.releaseResources();
        }
    }
    context.setMensagemRetorno("Processamento finalizado. E-mails enviados via serviço JSON.");
}

function buildHtmlBody(apelido, remFixa, valorComis, vlrFut, outros, adiant, liquido, formatter) {
    return "<!DOCTYPE html><html><head><style>" +
    "table { width: 100%; border-collapse: collapse; font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; margin-top: 20px; }" +
    "th, td { padding: 12px; text-align: left; border: 1px solid #e0e0e0; }" +
    "th { background-color: #f8f9fa; color: #333; font-weight: 600; }" +
    ".total-row { background-color: #f1f8ff; font-weight: bold; color: #2c3e50; }" +
    ".value-column { text-align: right; white-space: nowrap; }" +
    "</style></head><body>" +
    "<p>Prezado(a) <strong>" + apelido + "</strong>,</p>" +
    "<p>Esperamos que esta mensagem lhe encontre bem!</p>" +
    "<p>Conforme o encerramento do contrato de prestação de serviços firmado com a Satis, encaminhamos o detalhamento dos valores referentes ao seu acerto.</p>" +
    "<h3>Detalhamento dos valores:</h3>" +
    "<table><thead><tr><th>Descrição</th><th class='value-column'>Valor (R$)</th></tr></thead><tbody>" +
    "<tr><td>Remuneração Fixa</td><td class='value-column'>" + formatter.format(remFixa) + "</td></tr>" +
    "<tr><td>Comissão do mês</td><td class='value-column'>" + formatter.format(valorComis) + "</td></tr>" +
    "<tr><td>Comissão futura</td><td class='value-column'>" + formatter.format(vlrFut) + "</td></tr>" +
    "<tr><td>Outros valores</td><td class='value-column'>" + formatter.format(outros) + "</td></tr>" +
    "<tr><td>Valor a compensar</td><td class='value-column'>" + formatter.format(adiant) + "</td></tr>" +
    "<tr class='total-row'><td>Valor líquido a receber</td><td class='value-column'>" + formatter.format(liquido) + "</td></tr>" +
    "</tbody></table>" +
    "<p>Informamos que o pagamento será realizado no prazo de <strong>10 (dez) dias</strong>, contados a partir do recebimento da respectiva Nota Fiscal.</p>" +
    "<p>Atenciosamente,<br /><strong>Equipe Satis</strong></p>" +
    "</body></html>";
}
