DELETE FROM FAGSAK_PROSESS_TASK WHERE prosess_task_id IN (SELECT id FROM PROSESS_TASK WHERE task_type = 'IverksetteVedtak.VarsleOmVedtak');
DELETE FROM PROSESS_TASK WHERE task_type = 'IverksetteVedtak.VarsleOmVedtak';
DELETE FROM PROSESS_TASK_TYPE WHERE kode = 'IverksetteVedtak.VarsleOmVedtak';
