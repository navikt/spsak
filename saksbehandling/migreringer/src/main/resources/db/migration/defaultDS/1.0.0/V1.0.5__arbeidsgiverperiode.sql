  CREATE TABLE IAY_ARBEIDSGIVERPERIODE
   (	ID bigint,
	INNTEKTSMELDING_ID bigint,
	FOM DATE,
	TOM DATE,
	VERSJON bigint DEFAULT 0,
	OPPRETTET_AV VARCHAR(20) DEFAULT 'VL',
	OPPRETTET_TID TIMESTAMP (3) DEFAULT localtimestamp,
	ENDRET_AV VARCHAR(20),
	ENDRET_TID TIMESTAMP (3)
   ) ;

   COMMENT ON COLUMN IAY_ARBEIDSGIVERPERIODE.ID IS 'Primærnøkkel';
   COMMENT ON COLUMN IAY_ARBEIDSGIVERPERIODE.INNTEKTSMELDING_ID IS 'FK:';
   COMMENT ON TABLE IAY_ARBEIDSGIVERPERIODE  IS 'Arbeidsgivers informasjon om arbeidsgiverperiode';

  CREATE INDEX IDX_ARBEIDSGIVERPERIODE_1 ON IAY_ARBEIDSGIVERPERIODE (INNTEKTSMELDING_ID)
  ;

  /*CREATE UNIQUE INDEX PK_ARBEIDSGIVERPERIODE ON IAY_ARBEIDSGIVERPERIODE (ID)
  ;*/

  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ADD CONSTRAINT PK_ARBEIDSGIVERPERIODE PRIMARY KEY (ID);
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN OPPRETTET_TID SET NOT NULL;
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN OPPRETTET_AV SET NOT NULL;
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN VERSJON SET NOT NULL;
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN TOM SET NOT NULL;
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN FOM SET NOT NULL;
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN INNTEKTSMELDING_ID SET NOT NULL;
  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ALTER COLUMN ID SET NOT NULL;

  ALTER TABLE IAY_ARBEIDSGIVERPERIODE ADD CONSTRAINT FK_ARBEIDSGIVERPERIODE_1 FOREIGN KEY (INNTEKTSMELDING_ID)
	  REFERENCES IAY_INNTEKTSMELDING (ID);

   CREATE SEQUENCE  SEQ_ARBEIDSGIVERPERIODE  MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 50 START WITH 8751     NO CYCLE ;
