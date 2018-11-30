drop table ETTERKONTROLL_LOGG cascade constraints purge;
drop table YF_FORDELING_PERIODE cascade constraints purge;
drop table YF_DOKUMENTASJON_PERIODE cascade constraints  purge;
drop table YF_AVKLART_DATO cascade constraints purge;
drop table YF_DOKUMENTASJON_PERIODER cascade constraints purge;
drop table YF_FORDELING cascade constraints purge;
drop table GR_YTELSES_FORDELING cascade constraints purge;
drop table STOENADSKONTO cascade constraints purge;
drop table STOENADSKONTOBEREGNING cascade constraints purge;
drop table SO_DEKNINGSGRAD cascade constraints purge;
drop table SO_RETTIGHET cascade constraints purge;
drop table UTGAAENDE_HENDELSE cascade constraints purge;
drop table IAY_UTSETTELSE_PERIODE cascade constraints purge;

drop sequence SEQ_ETTERKONTROLL_LOGG;
drop sequence SEQ_GR_YTELSES_FORDELING;
drop sequence SEQ_YF_FORDELING;
drop sequence SEQ_YF_FORDELING_PERIODE;
drop sequence SEQ_YF_AVKLART_DATO;
drop sequence SEQ_YF_DOKUMENTASJON_PERIODE;
drop sequence SEQ_YF_DOKUMENTASJON_PERIODER;
drop sequence SEQ_PERIODE_ALENEOMSORG;
drop sequence SEQ_PERIODER_ALENEOMSORG;
drop sequence SEQ_STOENADSKONTO;
drop sequence SEQ_STOENADSKONTOBEREGNING;
drop sequence SEQ_UTGAAENDE_HENDELSE_ID;

alter table UTTAK_RESULTAT_PERIODE_SOKNAD drop constraint FK_UTTAK_RES_PER_SOKNAD_2;
alter table UTTAK_RESULTAT_PERIODE_SOKNAD drop column KL_MORS_AKTIVITET;
alter table UTTAK_RESULTAT_PERIODE_SOKNAD drop column MORS_AKTIVITET;
alter table TOTRINNRESULTATGRUNNLAG drop column YTELSES_FORDELING_GRUNNLAG_ID;
alter table UTTAK_RESULTAT_PERIODE_AKT drop constraint FK_UTTAK_RES_PERIODE_AKT_03;
alter table UTTAK_RESULTAT_PERIODE_AKT drop column KL_TREKKONTO;
alter table UTTAK_RESULTAT_PERIODE_AKT drop column TREKKONTO;

delete from kodeliste_navn_i18n where kl_kodeverk='UTSETTELSE_AARSAK_TYPE';
delete from kodeliste where kodeverk='UTSETTELSE_AARSAK_TYPE';
delete from kodeverk where kode = 'UTSETTELSE_AARSAK_TYPE';

delete from kodeliste_navn_i18n where kl_kodeverk='MORS_AKTIVITET';
delete from kodeliste where kodeverk='MORS_AKTIVITET';
delete from kodeverk where kode='MORS_AKTIVITET';

delete from kodeliste_navn_i18n where kl_kodeverk='UTTAK_DOKUMENTASJON_KLASSE';
delete from kodeliste where kodeverk='UTTAK_DOKUMENTASJON_KLASSE';
delete from kodeverk where kode='UTTAK_DOKUMENTASJON_KLASSE';

delete from kodeliste_navn_i18n where kl_kodeverk='UTTAK_DOKUMENTASJON_TYPE';
delete from kodeliste where kodeverk='UTTAK_DOKUMENTASJON_TYPE';
delete from kodeverk where kode='UTTAK_DOKUMENTASJON_TYPE';

delete from kodeliste_navn_i18n where kl_kodeverk in ('OVERFOERING_AARSAK_TYPE', 'AVKORTING_AARSAK_TYPE', 'OPPHOLD_AARSAK_TYPE', 'STOENADSKONTOTYPE');
delete from kodeliste where kodeverk in ('OVERFOERING_AARSAK_TYPE', 'AVKORTING_AARSAK_TYPE', 'OPPHOLD_AARSAK_TYPE', 'STOENADSKONTOTYPE');
delete from kodeverk where kode in ('OVERFOERING_AARSAK_TYPE', 'AVKORTING_AARSAK_TYPE', 'OPPHOLD_AARSAK_TYPE', 'STOENADSKONTOTYPE');


delete from kodeliste_navn_i18n where kl_kodeverk='AARSAK_TYPE';
delete from kodeliste where kodeverk='AARSAK_TYPE';
delete from kodeverk where kode = 'AARSAK_TYPE';

delete from kodeliste_navn_i18n where kl_kodeverk='FORDELING_PERIODE_KILDE';
delete from kodeliste where kodeverk='FORDELING_PERIODE_KILDE';
delete from kodeverk where kode = 'FORDELING_PERIODE_KILDE';

delete from kodeliste_navn_i18n where kl_kodeverk='OMSORGSOVERTAKELSE_VILKAR';
delete from kodeliste where kodeverk='OMSORGSOVERTAKELSE_VILKAR';
delete from kodeverk where kode = 'OMSORGSOVERTAKELSE_VILKAR';

delete from behandling_type_steg_sekv where behandling_steg_type='VURDERSFV';





