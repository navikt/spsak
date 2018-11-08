-- Relasjon Behandlingsresultat <-> BehandlingVedtak er 1-til-1
CREATE UNIQUE INDEX UIDX_BEHANDLING_VEDTAK_1 ON BEHANDLING_VEDTAK (behandling_resultat_id);
