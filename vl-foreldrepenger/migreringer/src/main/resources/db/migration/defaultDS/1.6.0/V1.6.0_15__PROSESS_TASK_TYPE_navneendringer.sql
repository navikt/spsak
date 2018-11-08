UPDATE PROSESS_TASK_TYPE SET kode = 'iverksetteVedtak.varsleOmVedtak'
,beskrivelse = 'Varsler andre løpende vedtak om vedtaket'
WHERE kode = 'IverksetteVedtak.VarsleOmVedtak';

UPDATE PROSESS_TASK_TYPE SET kode = 'iverksetteVedtak.sendVedtaksbrev'
,beskrivelse = 'Sender vedtaksbrev til brukeren via DokumentBestilleren, venter på kvittering'
WHERE kode = 'IverksetteVedtak.SendVedtaksbrev';

UPDATE PROSESS_TASK_TYPE SET kode = 'iverksetteVedtak.avsluttBehandling'
,beskrivelse = 'Avslutter behandlingen når brev er sendt og økonomioppdrag overført'
WHERE kode = 'IverksetteVedtak.AvsluttBehandling';

UPDATE PROSESS_TASK_TYPE SET kode = 'iverksetteVedtak.oppdragTilØkonomi'
,beskrivelse = 'Dersom vedtaket medfører økonomioppdrag, sender dette oppdragsløsningen og venter på kvittering'
WHERE kode = 'IverksetteVedtak.Utbetale';

