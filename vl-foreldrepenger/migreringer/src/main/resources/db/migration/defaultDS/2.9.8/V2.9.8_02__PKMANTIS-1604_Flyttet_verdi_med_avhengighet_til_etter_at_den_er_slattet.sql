-- Flyttet fra 2.9.0 siden avhengighet ikke ble slettet før i 2.9.6
DELETE FROM KODELISTE WHERE KODEVERK='RELATERTE_YTELSER_STATUS';
DELETE FROM KODEVERK WHERE KODE='RELATERTE_YTELSER_STATUS';
