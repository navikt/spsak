 CREATE TABLE BG_FAKTA_BER_TILFELLE (
  id                 NUMBER(19, 0)                     NOT NULL,
  fakta_beregning_tilfelle VARCHAR2(100 char) NOT NULL,
  kl_fakta_beregning_tilfelle VARCHAR2(100 char) AS ('FAKTA_OM_BEREGNING_TILFELLE'),
  beregningsgrunnlag_id NUMBER(19, 0)                     NOT NULL,
  versjon            NUMBER(19, 0) DEFAULT 0           NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_BG_FAKTA_BER_TILFELLE PRIMARY KEY (id),
  CONSTRAINT FK_BG_FAKTA_BER_TILFELLE_1 FOREIGN KEY (beregningsgrunnlag_id) REFERENCES BEREGNINGSGRUNNLAG(id)
);

CREATE SEQUENCE SEQ_BG_FAKTA_BER_TILFELLE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

comment on table BG_FAKTA_BER_TILFELLE is 'Eit fakta om beregning tilfelle for eit beregningsgrunnlag';
comment on column BG_FAKTA_BER_TILFELLE.fakta_beregning_tilfelle is 'FK: FAKTA_OM_BEREGNING_TILFELLE';
comment on column BG_FAKTA_BER_TILFELLE.kl_fakta_beregning_tilfelle is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column BG_FAKTA_BER_TILFELLE.beregningsgrunnlag_id is 'FK: BEREGNINGSGRUNNLAG';

alter table BG_FAKTA_BER_TILFELLE add constraint fk_BG_FAKTA_BER_TILFELLE_01
foreign key (fakta_beregning_tilfelle, kl_fakta_beregning_tilfelle) references kodeliste (kode, kodeverk);
create index IDX_BG_FAKTA_BER_TILFELLE_1 ON BG_FAKTA_BER_TILFELLE(beregningsgrunnlag_id);
create index IDX_BG_FAKTA_BER_TILFELLE_2 ON BG_FAKTA_BER_TILFELLE(fakta_beregning_tilfelle, kl_fakta_beregning_tilfelle);

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'TILSTØTENDE_YTELSE','Avklar beregningsgrunnlag og inntektskategori for tilstøtende ytelse',
to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

insert into KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
values (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE',	'TILSTØTENDE_YTELSE',	'NB',
'Avklar beregningsgrunnlag og inntektskategori for tilstøtende ytelse');

INSERT INTO BG_FAKTA_BER_TILFELLE (id, fakta_beregning_tilfelle, beregningsgrunnlag_id)
select SEQ_BG_FAKTA_BER_TILFELLE.NEXTVAL, 'TILSTØTENDE_YTELSE', beregningsgrunnlag_id from behandling b
join aksjonspunkt ap ON ap.behandling_id = b.id and ap.aksjonspunkt_def = '5050'
join gr_beregningsgrunnlag gr ON gr.behandling_id = b.id
where aktiv = 'J';

DELETE FROM AKSJONSPUNKT a5050
where a5050.aksjonspunkt_def = '5050'
  and exists (
    select 1 from AKSJONSPUNKT a5058
  where a5050.behandling_id = a5058.behandling_id
    and a5058.aksjonspunkt_def = '5058'
    );



UPDATE AKSJONSPUNKT set AKSJONSPUNKT_DEF = '5058' where AKSJONSPUNKT_DEF = '5050';
UPDATE TOTRINNSVURDERING set AKSJONSPUNKT_DEF = '5058' where AKSJONSPUNKT_DEF = '5050';

DELETE from AKSJONSPUNKT_DEF where KODE = '5050';
