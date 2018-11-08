-- legger til disse her. Kommer fra SF3, men brukes også i B2 Gå tilbake
alter table Vilkar add overstyrt char(1) default 'N' not null check (overstyrt IN ('J', 'N'));
alter table inngangsvilkar_resultat add overstyrt char(1) default 'N' not null check (overstyrt IN ('J', 'N'));
alter table beregning add overstyrt char(1) default 'N' not null check (overstyrt IN ('J', 'N'));
alter table beregning_resultat add overstyrt char(1) default 'N' not null check (overstyrt IN ('J', 'N'));

update Aksjonspunkt set aksjonspunkt_status = 'OPPR' where aksjonspunkt_status IN ('REAP', 'FORD');
update Aksjonspunkt set aksjonspunkt_status = 'UTFO' where aksjonspunkt_status IN ('LUKK');

delete from kodeliste where kode in ('REAP', 'FORD') and kodeverk = 'AKSJONSPUNKT_STATUS';
delete from kodeliste where kode in ('LUKK') and kodeverk = 'AKSJONSPUNKT_STATUS';
