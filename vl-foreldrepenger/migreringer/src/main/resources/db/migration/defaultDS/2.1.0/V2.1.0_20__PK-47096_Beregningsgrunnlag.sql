ALTER TABLE SAMMENLIGNINGSGRUNNLAG ADD avvik_prosent NUMBER(19) NULL;

UPDATE SAMMENLIGNINGSGRUNNLAG SET avvik_prosent = 0;

ALTER TABLE SAMMENLIGNINGSGRUNNLAG modify (avvik_prosent NOT NULL);

ALTER TABLE BG_PR_STATUS_OG_ANDEL RENAME column naturalytelse_bortfalt_mnd TO naturalytelse_bortfalt_pr_aar;

ALTER TABLE BG_PR_STATUS_OG_ANDEL RENAME column manuelt_fastsatt_pr_aar TO overstyrt_pr_aar;

ALTER TABLE BG_PR_STATUS_OG_ANDEL RENAME column brukers_andel_pr_aar TO maksimal_brukers_andel_pr_aar;

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD maksimal_refusjon_pr_aar DOUBLE PRECISION NULL;

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD avkortet_refusjon_pr_aar DOUBLE PRECISION NULL;

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD redusert_refusjon_pr_aar DOUBLE PRECISION NULL;

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD avkortet_brukers_andel_pr_aar DOUBLE PRECISION NULL;

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD redusert_brukers_andel_pr_aar DOUBLE PRECISION NULL;

ALTER TABLE BEREGNINGSGRUNNLAG_PERIODE RENAME column tilkjent_pr_dag TO dagsats;

ALTER TABLE BEREGNINGSGRUNNLAG_PERIODE DROP column beregnet_pr_aar;

ALTER TABLE BEREGNINGSGRUNNLAG_PERIODE DROP column overstyrt_pr_aar;

-- Oppdater kodeliste_relasjon tabell med data fra VILKAR_TYPE_AVSLAGSARSAK_KOBLE.
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2)
VALUES (seq_kodeliste_relasjon.nextval, 'VILKAR_TYPE', 'FP_VK_41', 'AVSLAGSARSAK', '1041');

-- sporing av regelresultater
alter table BEREGNINGSGRUNNLAG_PERIODE add (regel_evaluering_fastsett CLOB);
