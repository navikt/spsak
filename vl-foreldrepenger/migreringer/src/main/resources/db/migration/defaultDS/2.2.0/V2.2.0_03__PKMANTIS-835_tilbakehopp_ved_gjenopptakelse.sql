alter table aksjonspunkt_def add TILBAKEHOPP_VED_GJENOPPTAKELSE char(1 byte) default 'N' not null;

update aksjonspunkt_def set tilbakehopp_ved_gjenopptakelse = 'J' where kode = 7002;
