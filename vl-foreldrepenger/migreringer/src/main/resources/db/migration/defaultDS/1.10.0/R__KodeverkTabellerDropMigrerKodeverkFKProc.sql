-- DENNE KAN RYDDES NÅR VI HAR PRODSATT, BASELINER NYTT SCRIPT 
-- MEN NYTTIG Å HA PROC INNTIL DA SLIK AT DET ER ENKLERE Å MIGRERE KODEVERK
begin
   execute immediate 'drop procedure MIGRER_KODELISTE_FK';
exception when others then
   if sqlcode != -4043 then
      raise;
   end if;
end;
/




