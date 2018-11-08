ALTER TABLE VERGE MODIFY MANDAT_TEKST NULL;

-- ------------------------------------ --
-- Aksjonspunkt for å registrere verge  --
-- ------------------------------------ --
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE)
VALUES ('5030', 'Avklar verge', 'VURDERBV.INN', 'Saksbehandler registrerer verge', 'N', '-');
