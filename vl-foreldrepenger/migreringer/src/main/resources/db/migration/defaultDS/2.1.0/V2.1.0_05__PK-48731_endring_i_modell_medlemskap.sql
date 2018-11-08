-- rename column opphold_hjemland
alter table medlemskap_oppg_land drop constraint fk_utlandsopphold_1;
alter table medlemskap_oppg_land rename column opphold_hjemland to medlemskap_oppg_tilknyt_id;
alter table medlemskap_oppg_land add constraint fk_medlemskap_oppg_land_1 FOREIGN KEY (medlemskap_oppg_tilknyt_id) REFERENCES medlemskap_oppg_tilknyt;

-- rename constraint fk_utlandsopphold_81
alter table medlemskap_oppg_land rename constraint fk_utlandsopphold_81 to fk_medlemskap_oppg_land_2;

-- rename primary key medlemskap_oppg_land samt tilhørende index
alter table medlemskap_oppg_land rename constraint pk_utlandsopphold to pk_medlemskap_oppg_land;
alter index pk_utlandsopphold rename to pk_medlemskap_oppg_land;

-- rename primary key medlemskap_oppg_tilknyt
alter table medlemskap_oppg_tilknyt rename constraint pk_tilknytning_hjemland to pk_medlemskap_oppg_tilknyt;
alter index pk_tilknytning_norge rename to pk_medlemskap_oppg_tilknyt;

-- oppgitt_dato not nullable
merge into medlemskap_oppg_tilknyt
using soeknad
on (medlemskap_oppg_tilknyt.id = soeknad.TILKNYTNING_HJEMLAND_ID)
when matched then update set medlemskap_oppg_tilknyt.oppgitt_dato = soeknad.mottatt_dato;
alter table medlemskap_oppg_tilknyt modify oppgitt_dato not null;

-- rename column og constraing tilknytning_hjemland i soeknad
alter table soeknad rename constraint fk_soeknad_tilknytning_hjeml to fk_medlemskap_oppg_tilknyt;
alter table soeknad rename column tilknytning_hjemland_id to medlemskap_oppg_tilknyt_id;
comment on column soeknad.medlemskap_oppg_tilknyt_id IS 'FK: MEDLEMSKAP_OPPG_TILKNYT';

-- rename sekvenser
rename seq_tilknytning_hjemland to seq_medlemskap_oppg_tilknyt;
rename seq_utlandsopphold to seq_medlemskap_oppg_land;
