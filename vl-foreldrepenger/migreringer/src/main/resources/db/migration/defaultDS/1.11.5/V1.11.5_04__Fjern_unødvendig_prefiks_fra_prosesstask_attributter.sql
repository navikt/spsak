UPDATE PROSESS_TASK set TASK_PARAMETERE = replace(TASK_PARAMETERE, 'key.forsendelseMottatt', 'forsendelse.mottatt');
UPDATE PROSESS_TASK set TASK_PARAMETERE = replace(TASK_PARAMETERE, 'key.', '') ;
