UPDATE dokument_mal_type SET kode = doksys_id;

ALTER TABLE dokument_mal_type DROP COLUMN doksys_id;
ALTER TABLE dokument_mal_type ADD generisk VARCHAR2(1) DEFAULT('Y') NOT NULL;

INSERT INTO dokument_mal_type (kode, navn, generisk) VALUES ('000048', 'Positivt vedtaksbrev', 'N');

ALTER TABLE dokument_data ADD CONSTRAINT FK_DOKUMENT_DATA_3 FOREIGN KEY ( dokument_mal_navn ) REFERENCES dokument_mal_type ( kode );
