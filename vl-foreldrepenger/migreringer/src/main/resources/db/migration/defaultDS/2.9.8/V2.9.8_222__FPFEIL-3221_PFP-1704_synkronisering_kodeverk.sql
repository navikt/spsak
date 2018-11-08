update kodeverk set KODEVERK_SYNK_EKSISTERENDE='J' , KODEVERK_SYNK_NYE='J' where KODEVERK_EIER='Kodeverkforvaltning';
update kodeverk set KODEVERK_SYNK_EKSISTERENDE='N' , KODEVERK_SYNK_NYE='N' where KODE='BEHANDLING_TYPE';

update KODELISTE set offisiell_kode='ae0034' where KODEVERK='BEHANDLING_TYPE' and KODE='BT-002';
update KODELISTE set offisiell_kode='ae0043' where KODEVERK='BEHANDLING_TYPE' and KODE='BT-005';

update KODELISTE_NAVN_I18N set navn='Førstegangsbehandling' where KL_KODEVERK='BEHANDLING_TYPE' and KL_KODE='BT-002';
update KODELISTE_NAVN_I18N set navn='Tilbakebetaling endring' where KL_KODEVERK='BEHANDLING_TYPE' and KL_KODE='BT-005';
