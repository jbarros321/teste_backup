CREATE OR REPLACE PROCEDURE STP_EMAILVENDDESLIGADO_SATIS (
       P_CODUSU NUMBER,        -- Código do usuário logado
       P_IDSESSAO VARCHAR2,    -- Identificador da execução. Serve para buscar informações dos parâmetros/campos da execução.
       P_QTDLINHAS NUMBER,     -- Informa a quantidade de registros selecionados no momento da execução.
       P_MENSAGEM OUT VARCHAR2 -- Caso seja passada uma mensagem aqui, ela será exibida como uma informação ao usuário.
) AS
       FIELD_NUFECH         NUMBER;
       FIELD_SEQUENCIA      NUMBER;
       P_EMAIL              VARCHAR2(100);
       P_VALOR              NUMBER;
       P_REMFIXA            NUMBER;
       P_REEMBOLSO          NUMBER;
       P_ADIANTCOMISSAO     NUMBER;
       P_EXTRAS             NUMBER;
       P_DESLIGADO          VARCHAR2(100);
       P_CODFILA            NUMBER;
       P_APELIDO            VARCHAR2(500);
       P_CODVEND            NUMBER;
       P_ASSUNTO            VARCHAR2(4000);
       P_CORPO              VARCHAR2(4000);
       P_DIAS               NUMBER;
       P_VLRFUT             NUMBER;
    
BEGIN

       FOR I IN 1..P_QTDLINHAS -- Este loop permite obter o valor de campos dos registros envolvidos na execução.
       LOOP                    -- A variável "I" representa o registro corrente.

           FIELD_NUFECH := ACT_INT_FIELD(P_IDSESSAO, I, 'NUFECH');
           FIELD_SEQUENCIA := ACT_INT_FIELD(P_IDSESSAO, I, 'SEQUENCIA');


    SELECT 
        VEN.AD_EMAILPART,
        NVL(DIASTRAB,0),
        ROUND(NVL(FIN.VALOR, 0) - NVL(FIN.VLRADTCOMP, 0),2) AS VALOR,
        NVL(FIN.REMFIXA, 0) AS REMFIXA,
        INITCAP(VEN.APELIDO) AS APELIDO,
        VEN.CODVEND,
        NVL(FIN.REEMBOLSO, 0) AS REEMBOLSO,
        NVL(FIN.ADIANTAMENTO, 0)+(NVL(FIN.ADIANTFIXO,0)) AS ADIANTCOMISSAO,
        NVL(FIN.EXTRA, 0) AS EXTRAS,
        DESLIGADO

    INTO P_EMAIL,P_DIAS, P_VALOR, P_REMFIXA, P_APELIDO, P_CODVEND, P_REEMBOLSO, P_ADIANTCOMISSAO, P_EXTRAS,P_DESLIGADO

    FROM 
        AD_DBFECHCOMFIN FIN 
    INNER JOIN 
        TGFVEN VEN ON FIN.CODVEND = VEN.CODVEND
    WHERE 
        FIN.NUFECH = FIELD_NUFECH AND
        FIN.SEQUENCIA = FIELD_SEQUENCIA;


    SELECT NVL(SUM(VLRCOM), 0) INTO P_VLRFUT FROM AD_DBFECHCOMNOTASA WHERE NUFECH = FIELD_NUFECH AND CODVEND = P_CODVEND;

    P_LIQUIDO := NVL(P_REMFIXA, 0) + NVL(P_VALOR, 0) + NVL(P_VLRFUT, 0) + NVL(P_EXTRAS, 0) + NVL(P_REEMBOLSO, 0) - NVL(P_ADIANTCOMISSAO, 0);

   IF P_DESLIGADO = 'NÃO' THEN 

         RAISE_APPLICATION_ERROR(-20101, FC_FORMATAHTML('E-mail não enviado para Vendedor: '||P_APELIDO,
                                               'Vendedor Não esta Desligado para que se gere o E-mail de Desligamento',
                                               'Selecione um vendedor com o Desligado marcado como SIM "'));       


   END IF;

   IF P_DESLIGADO = 'SIM' AND P_DIAS = 0 AND P_REMFIXA > 0 THEN 

         RAISE_APPLICATION_ERROR(-20101, FC_FORMATAHTML('E-mail não enviado para Vendedor: '||P_APELIDO,
                                               'Vendedor esta Desligado mas sem declaração de dias trabalhados',
                                               'Ajuste a quantidade de dias trabalhodos do vendedor "'));       


   END IF;


     IF P_DESLIGADO = 'SIM' AND P_DIAS > 0 THEN 


            P_ASSUNTO := 'Faturamento '||P_APELIDO||',';

            P_CORPO := 
                '<!DOCTYPE html><html><head><style>' ||
                'table { width: 100%; border-collapse: collapse; font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; margin-top: 20px; }' ||
                'th, td { padding: 12px; text-align: left; border: 1px solid #e0e0e0; }' ||
                'th { background-color: #f8f9fa; color: #333; font-weight: 600; }' ||
                '.total-row { background-color: #f1f8ff; font-weight: bold; color: #2c3e50; }' ||
                '.value-column { text-align: right; white-space: nowrap; }' ||
                '</style></head><body>' ||
                '<p>Prezado(a) <strong>' || P_APELIDO || '</strong>,</p>' ||
                '<p>Esperamos que esta mensagem lhe encontre bem!</p>' ||
                '<p>Conforme o encerramento do contrato de prestação de serviços firmado com a Satis, encaminhamos, com a devida consideração, o detalhamento dos valores referentes ao seu acerto.</p>' ||
                '<h3>Detalhamento dos valores:</h3>' ||
                '<table>' ||
                '<thead><tr><th>Descrição</th><th class="value-column">Valor (R$)</th></tr></thead>' ||
                '<tbody>' ||
                '<tr><td>Ajuda de custo</td><td class="value-column">' || TO_CHAR(P_REMFIXA, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '</td></tr>' ||
                '<tr><td>Comissão do mês</td><td class="value-column">' || TO_CHAR(P_VALOR, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '</td></tr>' ||
                '<tr><td>Comissão futura</td><td class="value-column">' || TO_CHAR(P_VLRFUT, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '</td></tr>' ||
                '<tr><td>Outros valores</td><td class="value-column">' || TO_CHAR(NVL(P_EXTRAS, 0) + NVL(P_REEMBOLSO, 0), 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '</td></tr>' ||
                '<tr><td>Valor a compensar</td><td class="value-column">' || TO_CHAR(P_ADIANTCOMISSAO, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '</td></tr>' ||
                '<tr class="total-row"><td>Valor líquido a receber</td><td class="value-column">' || TO_CHAR(P_LIQUIDO, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '</td></tr>' ||
                '</tbody></table>' ||
                '<p>Informamos que o pagamento será realizado no prazo de <strong>10 (dez) dias</strong>, contados a partir do recebimento da respectiva Nota Fiscal.</p>' ||
                '<p>Caso haja qualquer dúvida ou necessidade de esclarecimentos adicionais, permanecemos inteiramente à disposição para auxiliá-lo(a).</p>' ||
                '<p>Atenciosamente,<br /><strong>Equipe Satis</strong></p>' ||
                '</body></html>';

                SELECT NVL(MAX(CODFILA),0)INTO P_CODFILA FROM TMDFMG;

                INSERT INTO TMDFMG (CODFILA, ASSUNTO, CODMSG, DTENTRADA, STATUS, CODCON, TENTENVIO, MENSAGEM, TIPOENVIO, MAXTENTENVIO, EMAIL, NUANEXO, MIMETYPE, CODSMTP) 
                VALUES( P_CODFILA+1, P_ASSUNTO, NULL, SYSDATE, 'Pendente', 0, 0, P_CORPO, 'E', 3, P_EMAIL, NULL, NULL, 33 );
                COMMIT;

                UPDATE 
                    AD_DBFECHCOMFIN 
                SET 
                    EMAILVEND = 'S' 
                WHERE 
                NUFECH = FIELD_NUFECH AND
                SEQUENCIA = FIELD_SEQUENCIA;

                UPDATE 
                    AD_DBFECHCOMFIN 
                SET 
                    CODFILA = P_CODFILA 
                WHERE 
                NUFECH = FIELD_NUFECH AND
                SEQUENCIA = FIELD_SEQUENCIA;  

                -- Mensagem de Sucesso para dizer que o Vendedor Recebeu o E-mail    
                P_MENSAGEM := 'Email enviado para vendedor com Sucesso!';

   END IF; 

    END LOOP;


END;
/