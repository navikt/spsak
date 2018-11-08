begin
    execute immediate 'drop index UIDX_OPPTJENING_AKTIVITET_01';
exception when others then 
    if sqlcode != -4043 then
      raise;
    end if;
end;
/

begin
    execute immediate 'alter table OPPTJENING_AKTIVITET drop constraint FK_OPPTJENING_AKT_REF_TYPE';
exception when others then 
    if sqlcode != -4043 then
      raise;
    end if;
end;
/

begin
    execute immediate 'alter table OPPTJENING_AKTIVITET drop column KL_REFERANSE_TYPE';
exception when others then 
    if sqlcode != -4043 then
      raise;
    end if;
end;
/

begin
	execute immediate 'alter table OPPTJENING_AKTIVITET drop column REFERANSE_TYPE';
exception when others then 
    if sqlcode != -4043 then
      raise;
    end if;
end;
/

delete from KODELISTE where KODEVERK ='OPPTJENING_AKTIVITET_REFERANSE_TYPE';
delete from KODEVERK where KODE ='OPPTJENING_AKTIVITET_REFERANSE_TYPE';

-- NYE

alter table OPPTJENING_AKTIVITET add REFERANSE_TYPE varchar2(100 char);
alter table OPPTJENING_AKTIVITET add KL_REFERANSE_TYPE varchar2(100 char) AS ('REFERANSE_TYPE');

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, NAVN, BESKRIVELSE) VALUES ('REFERANSE_TYPE', 'VL', 'Type referanse, for å referer til aktør, organisasjon', 'Type aktivitetReferanse (eks. Orgnummer, aktørid)');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'REFERANSE_TYPE', 'ORG_NR', 'Orgnr', 'Orgnr registrert for virksomhet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'REFERANSE_TYPE', 'AKTØR_ID', 'Aktør Id', 'Aktørid for person (registrert arbeidsgiver)', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

CREATE INDEX IDX_OPPTJENING_AKTIVITET_04
  ON OPPTJENING_AKTIVITET (REFERANSE_TYPE, KL_REFERANSE_TYPE);

  
alter table OPPTJENING_AKTIVITET add constraint FK_OPPTJENING_AKT_REF_TYPE foreign key (REFERANSE_TYPE, KL_REFERANSE_TYPE) references KODELISTE(kode, kodeverk);

-- er dessverre ingen enkel måte å forhindre overlappende fom/tom intervaller i Oracle uten å gå veien om triggere eller materialized views.  
-- Enforcer derfor det i Applikasjonslaget og legger kun på et basic unique constraint her.
CREATE UNIQUE INDEX UIDX_OPPTJENING_AKTIVITET_01
  ON OPPTJENING_AKTIVITET (opptjeningsperiode_id, fom, aktivitet_type, aktivitet_referanse, referanse_type);

