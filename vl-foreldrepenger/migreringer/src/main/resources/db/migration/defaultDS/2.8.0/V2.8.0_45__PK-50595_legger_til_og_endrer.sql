alter table GR_ARBEID_INNTEKT RENAME COLUMN inntekt_arbeid_ytelser_id TO  iay_register_før_id;
alter table GR_ARBEID_INNTEKT RENAME COLUMN overstyrt_id TO iay_saksbehandlet_før_id;

ALTER TABLE GR_ARBEID_INNTEKT add iay_register_etter_id NUMBER(19);
ALTER TABLE GR_ARBEID_INNTEKT add iay_saksbehandlet_etter_id NUMBER(19);

COMMENT ON COLUMN GR_ARBEID_INNTEKT.iay_register_etter_id is 'Arbeid inntekt register etter skjæringstidspunkt';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.iay_saksbehandlet_etter_id is 'Arbeid inntekt saksbehandlet etter skjæringstidspunkt';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.iay_register_før_id is 'Arbeid inntekt register før skjæringstidspunkt';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.iay_saksbehandlet_før_id is 'Arbeid inntekt saksbehandlet før skjæringstidspunkt';
