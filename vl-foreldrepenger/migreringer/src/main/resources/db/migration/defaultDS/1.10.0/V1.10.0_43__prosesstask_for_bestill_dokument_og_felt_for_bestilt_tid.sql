ALTER TABLE DOKUMENT_DATA ADD bestilt_tid  TIMESTAMP(3) NULL;

INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
values ('dokumentbestiller.bestillDokument', 'Bestill dokument', 3, 60, 'DEFAULT', 'Produserer nytt dokument og sender det til dokumentproduksjonsstjenesten.');
