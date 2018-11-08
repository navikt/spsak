--- lar denne kjøre som rerunnable inntil videre slik at alle migreringer ellers vil kjøres før denne. Så hvis ting feiler har vi ikke en delvis ryddet database.

begin
    execute immediate 'alter table personopplysning drop column behandling_grunnlag_id';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/


begin
    execute immediate 'alter table MEDLEMSKAP_REGISTRERT drop column behandling_grunnlag_id';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'alter table MEDLEMSKAP_VURDERING drop column behandling_grunnlag_id';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'alter table MEDLEMSKAP_PERIODER drop column behandling_grunnlag_id';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'ALTER TABLE ADOPSJON DROP COLUMN BEHANDLING_GRUNNLAG_ID';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'ALTER TABLE TERMINBEKREFTELSE DROP COLUMN BEHANDLING_GRUNNLAG_ID';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'ALTER TABLE UIDENTIFISERT_BARN DROP COLUMN SOEKNAD_ID';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'DROP TABLE FOEDSEL';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'DROP TABLE ADOPSJON_BARN';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/

begin
    execute immediate 'DROP TABLE SOEKNAD_ADOPSJON_BARN';
    exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/
