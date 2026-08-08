CREATE OR REPLACE procedure proc_inclui_parceiro_ufs_nm(p_dtini        in date,
                                                        p_dtfin        in date,
                                                        p_codemp       in number,
                                                        p_codempmatriz in number,
                                                        p_nunota       in number,
                                                        p_codtipoper   in number,
                                                        p_apenasdifal  in varchar2,
                                                        p_seqtela      in number,
                                                        p_mensagem     out varchar2) is
  v_codparc    tgfpar.codparc%type;
  v_codend     int;
  v_coduf      number(5) := 0;
  v_conta_parc number;
  v_count      number;
  v_qtdreg     number := 0;
  v_qtdalt     number := 0;
  v_erro       varchar2(4000);
  v_uf         tsiufs.uf%type;
  v_qtd        number;
begin

  dbms_application_info.set_module('ALT_PARC',
                                   'ABR CURS' ||
                                   to_char(sysdate,
                                           ' - DD/MM/RRRR HH24:MI:SS'));

  for regc in (/*+ all_rows */ select c.nunota,
                      c.codemp,
                      c.codcencus,
                      c.numnota,
                      c.serienota,
                      c.dtneg,
                      c.dtfatur,
                      c.dtentsai,
                      c.dtval,
                      c.dtmov,
                      c.dtcontab,
                      c.hrmov,
                      c.codempnegoc,
                      c.codparc,
                      c.codcontato,
                      c.rateado,
                      c.codveiculo,
                      c.codtipoper,
                      c.dhtipoper,
                      c.tipmov,
                      c.codtipvenda,
                      c.dhtipvenda,
                      c.numcotacao,
                      c.codvend,
                      c.comissao,
                      c.codmoeda,
                      c.codobspadrao,
                      c.observacao,
                      c.vlrseg,
                      c.vlricmsseg,
                      c.vlrdestaque,
                      c.vlrjuro,
                      c.vlrvendor,
                      c.vlroutros,
                      c.vlremb,
                      c.vlricmsemb,
                      c.vlrdescserv,
                      c.ipiemb,
                      c.tipipiemb,
                      c.vlrdesctot,
                      c.vlrdesctotitem,
                      c.vlrfrete,
                      c.icmsfrete,
                      c.baseicmsfrete,
                      c.tipfrete,
                      c.cif_fob,
                      c.vencfrete,
                      c.vlrnota,
                      c.vencipi,
                      c.ordemcarga,
                      c.seqcarga,
                      c.kmveiculo,
                      c.codparctransp,
                      c.qtdvol,
                      c.pendente,
                      c.baseicms,
                      c.vlricms,
                      c.baseipi,
                      c.vlripi,
                      c.issretido,
                      c.baseiss,
                      c.vlriss,
                      c.aprovado,
                      c.statusnota,
                      c.codusu,
                      c.irfretido,
                      c.comger,
                      c.vlrirf,
                      c.dtalter,
                      c.volume,
                      c.codparcdest,
                      c.vlrsubst,
                      c.basesubstit,
                      c.codproj,
                      c.numcontrato,
                      c.baseinss,
                      c.vlrinss,
                      c.vlrrepredtot,
                      c.percdesc,
                      c.codparcremetente,
                      c.codparcconsignatario,
                      c.codparcredespacho,
                      c.localcoleta,
                      c.localentrega,
                      c.vlrmercadoria,
                      c.peso,
                      c.notascf,
                      c.codnat,
                      c.codmaq,
                      c.codfunc,
                      c.numos,
                      c.numcf,
                      c.vlrfretecpl,
                      c.troco,
                      c.nroredz,
                      c.vlrmoeda,
                      c.occn48,
                      c.codusuinc,
                      c.nutransf,
                      c.codsite,
                      c.totalcustoprod,
                      c.totalcustoserv,
                      c.codcid,
                      c.basesubstsemred,
                      c.codmotorista,
                      c.nurd8,
                      c.codusucomprador,
                      c.naturezaoperdes,
                      c.serienfdes,
                      c.modelonfdes,
                      c.dtprevent,
                      c.nunotapedfret,
                      c.baseirf,
                      c.aliqirf,
                      c.placa,
                      c.ufveiculo,
                      c.pesobruto,
                      c.ad_natvitrine,
                      c.vlr_entrada,
                      c.ad_pis,
                      c.ad_cofins,
                      c.ad_vlrcus,
                      c.hrentsai,
                      c.antt,
                      c.lacres,
                      c.danfe,
                      c.chavenfe,
                      c.numeracaovolumes,
                      c.dtenviopmb,
                      c.tipnotapmb,
                      c.numaleatorio,
                      c.numprotoc,
                      c.dhprotoc,
                      c.dtenvsuf,
                      c.nulotenfe,
                      c.statusnfe,
                      c.ad_nmnf,
                      c.ad_pisret,
                      c.ad_cofinsret,
                      c.ad_csslret,
                      c.ad_anoaidf,
                      c.ad_nraidf,
                      c.ad_nmdoc,
                      c.ad_codencusger,
                      c.ad_encargofin,
                      c.ad_custoger,
                      c.ad_codcencusger,
                      c.tpemisnfe,
                      c.dtadiam,
                      c.hradiam,
                      c.coddoca,
                      c.digital,
                      c.totdispdesc,
                      c.basepis,
                      c.vlrpis,
                      c.basepisst,
                      c.vlrpisst,
                      c.basecofins,
                      c.vlrcofins,
                      c.basecofinsst,
                      c.vlrcofinsst,
                      c.vlrroyalt,
                      c.nrocaixa,
                      c.numregdpec,
                      c.dhregdpec,
                      c.ad_tipo_receita,
                      c.nm_juros,
                      c.codempfunc,
                      c.vlrindeniz,
                      c.num_pedido,
                      c.ad_tpv11,
                      c.ad_tpv12,
                      c.ad_tpv13,
                      c.ad_tpv16,
                      c.ad_tpv33,
                      c.ctb_cartao,
                      c.ad_ccorrente,
                      c.ad_nutef,
                      c.tpligacao,
                      c.codgrupotensao,
                      c.tpassinante,
                      c.nunotabkp,
                      c.flag,
                      c.marca,
                      c.tipoptagjnfe,
                      c.tpemisnfse,
                      c.nulotenfse,
                      c.numnfse,
                      c.statusnfse,
                      c.ufembarq,
                      c.locembarq,
                      c.nurem,
                      c.ad_vlripifrete,
                      c.dtneg_01,
                      c.retdatacritica,
                      c.qtdbatidas,
                      c.percpureza,
                      c.percgermin,
                      c.fretevlrbruto,
                      c.fretevlrdesc,
                      c.fretepercdesc,
                      c.fretevlrimp,
                      c.fretevlrjur,
                      c.fretevlrpago,
                      c.codvendtec,
                      c.numpedido,
                      c.vlrindenizdist,
                      c.numpedido2,
                      c.nomeadquirente,
                      c.cpfcnpjadquirente,
                      c.ad_guiast,
                      c.nm_raiz_cnpj,
                      c.dtref,
                      c.fretevlrnegoc,
                      c.ad_vlrcusto,
                      c.codagenda,
                      c.agrupbol,
                      c.codprodpermuta,
                      c.nrogar,
                      c.md5paf,
                      c.codmoddocnota,
                      c.vlrsacadolar,
                      c.numcoo,
                      c.ordemcargaant,
                      c.tpambnfe,
                      c.przmed,
                      c.codresiduo,
                      c.ad_ramoativ,
                      c.dtref2,
                      c.ad_dhalternm,
                      c.ad_baseantecip,
                      c.ad_vlrantecip,
                      c.ad_codusunm,
                      c.ad_vlricmstrans,
                      c.ad_nunotaca,
                      c.vlrliqitemnfe,
                      c.clascons,
                      c.numform,
                      c.dtref3,
                      c.ad_codparcserv,
                      c.codcc,
                      c.ad_vlrcusg,
                      c.vlrstextranotatot,
                      c.ad_vlricmscomp,
                      c.ad_idvtrine,
                      c.ad_vlrestornost,
                      c.produetloc,
                      c.nutranemp,
                      c.sitespecialresp,
                      c.libconf,
                      c.nuconfatual,
                      c.vlrjurodist,
                      c.ad_basepres,
                      c.ad_aliqpres,
                      c.ad_vlrpres,
                      c.ad_basestex,
                      c.ad_vlrstex,
                      c.ad_flag,
                      c.dtentsaiinfo,
                      c.nunota_inter,
                      c.exigeissqn,
                      c.regesptribut,
                      c.motnaoreterissqn,
                      c.dtremret,
                      c.statusxtrategie,
                      c.codsaf,
                      c.vlrfretecalc,
                      c.notaempenho,
                      c.pesobrutomanual,
                      c.pesoliquimanual,
                      c.codtpd,
                      c.codvtp,
                      c.cancelado,
                      c.nupca,
                      c.indpresnfce,
                      c.ad_tipend,
                      c.ad_nomeend,
                      c.ad_numend,
                      c.ad_complemento,
                      c.ad_uf,
                      c.ad_cep,
                      c.ad_codcid,
                      c.ad_codbai,
                      c.m3,
                      c.ad_basestest,
                      c.ad_vlrstest,
                      c.ad_vlrmont,
                      c.ad_baseicmsmont,
                      c.ad_vlricmsmont,
                      c.ad_numorcom,
                      c.id_documento,
                      c.vlrtotliqitemmoe,
                      c.vlrdesctotitemmoe,
                      c.chavecte,
                      c.prodpred,
                      c.tpemiscte,
                      c.tpambcte,
                      c.lotacao,
                      c.statuscte,
                      c.numaleatoriocte,
                      c.numprotoccte,
                      c.dhprotoccte,
                      c.nulotecte,
                      c.dtdeclara,
                      c.reboque1,
                      c.reboque2,
                      c.reboque3,
                      c.ad_iof,
                      c.ad_vlrstcompl,
                      c.ad_vlripicompl,
                      c.viatransp,
                      c.tipprocimp,
                      c.cnpjadquirente,
                      c.ufadquirente,
                      c.situacaocte,
                      c.ctelotacao,
                      c.codveitracao,
                      c.codobra,
                      c.codart,
                      c.idiproc,
                      c.nunotasub,
                      c.chavenfse,
                      c.modentrega,
                      c.dhemissepec,
                      c.vlricmsdifaldest,
                      c.vlricmsdifalrem,
                      c.ciot,
                      c.ad_vlrsegurorec,
                      c.ad_vlrseguroven,
                      p.identinscestad       as ad_inscestadnauf,
                      c.ad_nomecid,
                      c.ad_ddd,
                      c.ad_codreg,
                      c.ad_codmunfis,
                      c.ad_codparcorig,
                      c.ad_baseicmsfcp,
                      c.ad_vlricmsfcp,
                      c.vlricmsfcp,
                      c.vlrfretetotal,
                      c.codparctranspfinal,
                      c.fusoemissepec,
                      p.codcid               as codcid_par
                 from tgfcab c,
                      tgfpar p,
                      tgftop t
                where p.codparc = c.codparc
                  and t.codtipoper = c.codtipoper
                  and t.dhalter = c.dhtipoper
                  and c.codparc < 900000000
                  and (t.atuallivfis <> 'N' or t.codtipoper = 1005)
                  and exists
                (select 1
                         from tgfite i
                        where i.nunota = c.nunota
                          and (i.codcfo > 5000 or
                              i.codcfo in (1201,
                                            1202,
                                            1410,
                                            1411,
                                            2201,
                                            2202,
                                            2410,
                                            2411)))
                  and c.dtentsai between p_dtini and p_dtfin
                  and (c.codemp = p_codemp or nvl(p_codemp, 0) = 0)
                  and (func_filial_empresa_data_cisao(c.codemp, c.dtentsai) =
                      p_codempmatriz or nvl(p_codempmatriz, 0) = 0)
                  and (c.nunota = p_nunota or nvl(p_nunota, 0) = 0)
                  and (c.codtipoper = p_codtipoper or
                      nvl(p_codtipoper, 0) = 0)
               --   and ((nvl(p_apenasdifal, 'N') = 'S' and
               and ((nvl('N', 'N') = 'S' and
                      (nvl(c.vlricmsdifaldest, 0) <> 0 or
                      nvl(c.vlricmsdifalrem, 0) <> 0 or
                      nvl(c.vlricmsfcp, 0) <> 0)) or
                  --    (nvl(p_apenasdifal, 'N') = 'N'))
                   (nvl('N', 'N') = 'N'))
                  and not exists (select 1
                         from tsiemp e
                        where e.cgc = p.cgc_cpf
                          and e.cgc * 1 <> 0)
               
               )
  loop
  
    begin
    
      /*
      inicio rotina: Incluir parceiro com novo endereço/inscrição estadual
      autor: Rodrigo Coutinho
      data: 06/01/2016
      */
      v_codparc := regc.codparc;
    
      if regc.ad_uf is not null then
      
        begin
          select u.uf
            into v_uf
            from tsicid c,
                 tsiufs u
           where u.coduf = c.uf
             and c.codcid = regc.ad_codcid;
        exception
          when no_data_found then
            v_uf := null;
        end;
      
        select count(1)
          into v_conta_parc
          from tgfpar p,
               tsicid c,
               tsiufs u
         where c.codcid = p.codcid
           and u.coduf = c.uf
           and p.codparc = regc.codparc
           and u.uf = nvl(v_uf, regc.ad_uf)
           and nvl(trim(p.identinscestad), '0') =
               nvl(trim(regc.ad_inscestadnauf), '0');
      
        if v_conta_parc = 0 then
        
          begin
            select u.codparcvinc
              into v_codparc
              from ad_tgfparuf u,
                   tsiufs      ufs
             where ufs.coduf = u.coduf
               and u.codparc = regc.codparc
               and ufs.uf = nvl(v_uf, regc.ad_uf)
               and nvl(trim(u.inscestadnauf), '0') =
                   nvl(trim(regc.ad_inscestadnauf), '0');
          exception
            when no_data_found then
              v_codparc := 0;
          end;
        
          if v_codparc = 0 then
          
            select count(1)
              into v_count
              from tsicid c
             where c.codcid = nvl(regc.ad_codcid, 0);
          
            begin
              select u.uf
                into v_coduf
                from tsicid u
               where u.codcid = nvl(regc.ad_codcid, 0);
            exception
              when no_data_found then
                v_coduf := null;
            end;
          
            if v_count = 0 then
            
              v_coduf := 0;
              select u.coduf
                into v_coduf
                from tsiufs u
               where u.uf = regc.ad_uf;
            
              insert into tsicid
                (codcid,
                 uf,
                 nomecid,
                 ddd,
                 codreg,
                 distancia,
                 dtalter,
                 descricaocorreio,
                 seqentrega,
                 populacao,
                 codmunfis,
                 vlrfretemin,
                 vlrfreteton,
                 tipofrete,
                 ad_qtdkgperc,
                 vlrfretekm,
                 vlrtaxaent,
                 ad_codcid,
                 codmunsiafi)
              values
                (regc.ad_codcid,
                 v_coduf,
                 regc.ad_nomecid,
                 regc.ad_ddd,
                 nvl(regc.ad_codreg, 0),
                 null, ---distancia,
                 sysdate, ---dtalter,
                 regc.ad_nomecid, ---descricaocorreio,
                 null, ---seqentrega,
                 null, ---populacao,
                 regc.ad_codmunfis,
                 null, ---vlrfretemin,
                 null, ---vlrfreteton,
                 'C', ---tipofrete,
                 null, ---ad_qtdkgperc,
                 null, ---vlrfretekm,
                 null, ---vlrtaxaent,
                 regc.ad_codcid, ---ad_codcid,
                 null); ---codmunsiafi
            end if;
          
            -- VERIFICAR SE ENDERECO EXISTE
            select count(1)
              into v_count
              from tsiend
             where nomeend =
                   substr(nvl(regc.ad_nomeend, 'SEM NOME'), 1, 40)
               and tipo = nvl(regc.ad_tipend, 'RUA');
          
            if v_count <> 0 then
              select codend
                into v_codend
                from tsiend
               where nomeend =
                     substr(nvl(regc.ad_nomeend, 'SEM NOME'), 1, 40)
                 and tipo = nvl(regc.ad_tipend, 'RUA');
            else
              select ultcod + 1
                into v_codend
                from tgfnum t
               where t.arquivo = 'TSIEND';
            
              update tgfnum
                 set ultcod = ultcod + 1
               where arquivo = 'TSIEND';
              insert into tsiend
                (codend,
                 nomeend,
                 tipo,
                 dtalter,
                 descricaocorreio)
              values
                (v_codend,
                 substr(nvl(regc.ad_nomeend, 'SEM NOME'), 1, 40),
                 nvl(regc.ad_tipend, 'RUA'),
                 sysdate,
                 nvl(substr(regc.ad_nomeend, 1, 60), 'SEM NOME'));
            end if;
          
            select nvl(max(codparc), 900000000) + 1
              into v_codparc
              from tgfpar
             where codparc between 900000000 and 999000000;
          
            insert into tgfpar
              (codparc,
               cgc_cpf,
               codvend,
               nomeparc,
               razaosocial,
               identinscestad,
               tippessoa,
               codparcmatriz,
               codreg,
               cep,
               dtcad,
               dtalter,
               cliente,
               fornecedor,
               transportadora,
               tare,
               ipiincicms,
               retemiss,
               reteminss,
               ativo,
               calcinss,
               retempis,
               retemcofins,
               retemcsl,
               temipi,
               nm_raiz_cnpj,
               ad_vpc,
               codcid,
               complemento,
               numend,
               codend,
               codbai,
               ad_cnae,
               classificms)
              select /*+ all_rows */ v_codparc as codparc,
                     cgc_cpf,
                     codvend,
                     nomeparc,
                     razaosocial,
                     regc.ad_inscestadnauf as identinscestad,
                     tippessoa,
                     codparcmatriz,
                     nvl(regc.ad_codreg, 0) as codreg,
                     regc.ad_cep as cep,
                     dtcad,
                     sysdate as dtalter,
                     cliente,
                     fornecedor,
                     transportadora,
                     tare,
                     ipiincicms,
                     retemiss,
                     reteminss,
                     ativo,
                     calcinss,
                     retempis,
                     retemcofins,
                     retemcsl,
                     temipi,
                     nm_raiz_cnpj,
                     ad_vpc,
                     regc.ad_codcid as codcid,
                     regc.ad_complemento as complemento,
                     regc.ad_numend as numend,
                     v_codend as codend,
                     codbai,
                     ad_cnae,
                     classificms
                from tgfpar p
               where p.codparc = regc.codparc;
          
/*            raise_application_error(-20101,
                                    chr(13) || chr(13) || v_codparc || '-' || v_uf || '-' ||
                                    v_conta_parc || '-' || v_codparc ||
                                    '- qtdcid' || v_count || '-' ||
                                    v_codparc ||' qtd linhas'||sql%rowcount|| chr(13) || chr(13));
*/          
            insert into ad_tgfparuf
              (codparc,
               coduf,
               inscestadnauf,
               codparcvinc)
            values
              (regc.codparc,
               v_coduf,
               nvl(trim(regc.ad_inscestadnauf), '0'),
               v_codparc);
          
          end if;
        
          if nvl(v_codparc, 0) <> 0 then
          
            if regc.codparc < 900000000 then
            
              if nvl(p_seqtela, 0) <> 0 then
              
                select count(1)
                  into v_qtd
                  from ad_tgfparendorig o
                 where o.sequencia = p_seqtela
                   and o.codparc = regc.codparc;
              
                if v_qtd = 0 then
                
                  insert into ad_tgfparendorig
                    (sequencia,
                     codparc,
                     uforig,
                     inscestadnauforig,
                     codcidorig,
                     ufnova,
                     inscestadnaufnova,
                     codcidnova,
                     codparcnovo)
                  values
                    (p_seqtela, --SEQUENCIA,
                     regc.codparc, --CODPARC,
                     (select u.coduf
                        from tsiufs u
                       where u.uf = fc_uf_codparc(regc.codparc)), --UFORIG,
                     regc.ad_inscestadnauf, --INSCESTADNAUFORIG,
                     regc.codcid_par, --CODCIDORIG,
                     (select u.coduf
                        from tsiufs u
                       where u.uf = fc_uf_codparc(v_codparc)), --UFNOVA,
                     regc.ad_inscestadnauf, --INSCESTADNAUFNOVA,
                     (select p.codcid
                        from tgfpar p
                       where p.codparc = v_codparc), --CODCIDNOVA,
                     v_codparc --CODPARCNOVO
                     );
                
                end if;
              
              end if;
            
              update tgfcab c
                 set c.ad_codparcorig = regc.codparc
               where c.nunota = regc.nunota;
            
              update tgfcab c
                 set c.codparc = v_codparc
               where c.nunota = regc.nunota;
            
              update tgfliv l
                 set l.codparc   = v_codparc,
                     l.uforigem = (case
                                    when l.codcfo < 5000 then
                                     fc_uf_codparc(v_codparc)
                                    else
                                     fc_uf_codemp(l.codemp)
                                  end),
                     l.ufdestino = (case
                                     when l.codcfo > 5000 then
                                      fc_uf_codparc(v_codparc)
                                     else
                                      fc_uf_codemp(l.codemp)
                                   end)
               where l.nunota = regc.nunota
                 and l.origem = 'E';
            
              v_qtdalt := v_qtdalt + 1;
            
            end if;
          
          end if;
        
        end if;
      
      end if;
    
      v_qtdreg := v_qtdreg + 1;
    
      dbms_application_info.set_module('ALT_PARC',
                                       'R:' || v_qtdreg || '-A:' ||
                                       v_qtdalt);
    
    exception
      when others then
      
        v_erro := sqlerrm;
      
        dbms_output.put_line('N. Único: ' || regc.nunota || ', Parceiro: ' ||
                             regc.codparc || chr(13) || chr(13) || v_erro);
      
        raise_application_error(-20101,
                                'N. Único: ' || regc.nunota ||
                                ', Parceiro: ' || regc.codparc || chr(13) ||
                                chr(13) || v_erro);
      
    end;
  
  --    COMMIT;
  
  end loop;
  /*
  final rotina: Incluir parceiro com novo endereço/inscrição estadual
  */

  p_mensagem := v_qtdreg || ' registros processados' || chr(13) || v_qtdalt ||
                ' registros alterados';

end;

/