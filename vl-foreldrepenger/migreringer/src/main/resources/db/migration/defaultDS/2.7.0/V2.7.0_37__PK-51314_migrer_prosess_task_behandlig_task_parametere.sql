-- gjør om properties i task_parametere til inserts i FAGSAK_PROSESS_TASK. tar samtidig hensyn til at disse eksisterer
INSERT INTO FAGSAK_PROSESS_TASK
  (id,fagsak_id,behandling_id, prosess_task_id
  )
SELECT seq_fagsak_prosess_task.nextval AS id,
  t.fagsak_id,
  t.behandling_id,
  t.prosess_task_id
FROM
  ( WITH tbl AS
  (SELECT regexp_substr(regexp_substr(task_parametere, 'fagsakId=(.+)'), '[0-9]+') AS fagsak_Id,
    regexp_substr(regexp_substr(task_parametere, 'behandlingId=(.+)'), '[0-9]+')   AS behandling_Id,
    pt.id                                                                             AS prosess_task_id
  FROM prosess_task pt
  WHERE task_parametere LIKE '%fagsakId=%behandlingId=%'
  )
SELECT tbl.*
FROM tbl
WHERE EXISTS
  (SELECT 1 FROM behandling b WHERE b.id      =tbl.behandling_id AND b.fagsak_id =tbl.fagsak_id)
AND EXISTS
  (SELECT 1 FROM fagsak f WHERE f.id=tbl.fagsak_id)
AND NOT EXISTS (select 1 from fagsak_prosess_task p where p.fagsak_id = tbl.fagsak_id and P.prosess_task_id = tbl.prosess_task_id)  
  ) t ;