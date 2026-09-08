-- =====================================================================
-- Teste de isolamento entre igrejas.
--
-- Rode no SQL Editor do Supabase DEPOIS das migrations. Ele cria dois
-- usuários e duas igrejas, verifica que nenhum dos dois enxerga o
-- conteúdo do outro, e desfaz tudo no final (a transação é revertida).
--
-- APROVADO  = a última linha devolve resultado = APROVADO.
-- REPROVADO = o script aborta com erro em vermelho, e a mensagem diz o que
--             vazou. Nesse caso NÃO suba para produção.
-- =====================================================================

begin;

do $$
declare
  ana   uuid := gen_random_uuid();
  bruno uuid := gen_random_uuid();
  igreja_a uuid;
  igreja_b uuid;
  visiveis integer;
begin
  -- Usuários. O gatilho on_auth_user_created cria perfil e preferências.
  insert into auth.users (id, instance_id, aud, role, email, encrypted_password, created_at, updated_at)
  values
    (ana,   '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'ana@teste.local',   '', now(), now()),
    (bruno, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 'bruno@teste.local', '', now(), now());

  -- Ana cria a igreja A e cadastra uma música.
  perform set_config('request.jwt.claims', json_build_object('sub', ana)::text, true);
  igreja_a := (public.create_church('Igreja A')).id;

  insert into public.songs (church_id, title, slug, chordpro_content, created_by)
  values (igreja_a, 'Música da Ana', 'musica-da-ana', '{title: Música da Ana}', ana);

  -- Bruno cria a igreja B.
  perform set_config('request.jwt.claims', json_build_object('sub', bruno)::text, true);
  igreja_b := (public.create_church('Igreja B')).id;

  insert into public.songs (church_id, title, slug, chordpro_content, created_by)
  values (igreja_b, 'Música do Bruno', 'musica-do-bruno', '{title: Música do Bruno}', bruno);

  -- ---------------------------------------------------------------
  -- A partir daqui as consultas passam pelo RLS.
  -- ---------------------------------------------------------------
  set local role authenticated;

  -- Bruno enxerga só a música dele.
  perform set_config('request.jwt.claims', json_build_object('sub', bruno)::text, true);

  select count(*) into visiveis from public.songs;
  if visiveis <> 1 then
    raise exception 'Bruno deveria ver 1 música, viu %', visiveis;
  end if;

  select count(*) into visiveis from public.songs where church_id = igreja_a;
  if visiveis <> 0 then
    raise exception 'VAZAMENTO: Bruno enxergou % música(s) da igreja da Ana', visiveis;
  end if;

  select count(*) into visiveis from public.churches where id = igreja_a;
  if visiveis <> 0 then
    raise exception 'VAZAMENTO: Bruno enxergou a igreja da Ana';
  end if;

  -- Ana enxerga só a música dela.
  perform set_config('request.jwt.claims', json_build_object('sub', ana)::text, true);

  select count(*) into visiveis from public.songs;
  if visiveis <> 1 then
    raise exception 'Ana deveria ver 1 música, viu %', visiveis;
  end if;

  -- Bruno não consegue escrever na igreja da Ana.
  perform set_config('request.jwt.claims', json_build_object('sub', bruno)::text, true);
  begin
    insert into public.songs (church_id, title, slug, chordpro_content)
    values (igreja_a, 'Invasão', 'invasao', '');
    raise exception 'VAZAMENTO: Bruno conseguiu inserir música na igreja da Ana';
  exception
    when insufficient_privilege then null;  -- esperado
  end;

  -- Cada igreja recebeu as categorias padrão.
  perform set_config('request.jwt.claims', json_build_object('sub', ana)::text, true);
  select count(*) into visiveis from public.categories;
  if visiveis <> 10 then
    raise exception 'Ana deveria ver 10 categorias padrão, viu %', visiveis;
  end if;

  reset role;
end
$$;

-- Chegar até aqui significa que nenhuma verificação abortou.
-- (O SQL Editor não mostra NOTICE, por isso devolvemos uma linha de verdade.)
select
  'APROVADO' as resultado,
  'Nenhuma igreja enxerga ou escreve no conteúdo da outra' as detalhe;

rollback;
