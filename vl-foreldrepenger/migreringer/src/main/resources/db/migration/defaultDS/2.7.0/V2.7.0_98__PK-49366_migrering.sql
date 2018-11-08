/*
* Formål : migrering av gammel struktur for personopplysninger til ny aggregert struktur med støtte for
*          historikk
* Ref.   : https://jira.adeo.no/browse/PK-49366
*/

DECLARE

V_FOM_DATO CONSTANT     DATE := to_date('2010-01-01', 'YYYY-MM-DD');
V_TIDENES_ENDE CONSTANT DATE := to_date('31.12.9999', 'dd.mm.yyyy');

personopplysning_row    PERSONOPPLYSNING%ROWTYPE;

CURSOR c_hent_alle_po_grunnlag IS
    SELECT id FROM GR_PERSONOPPLYSNING;

CURSOR c_hent_personopplysning (po_id NUMBER) IS
    SELECT * FROM PERSONOPPLYSNING WHERE id = po_id AND AKTOER_ID IS NOT NULL;

-----------------------------------------------------
-- henter alle til-personer for angitt fra-person
-----------------------------------------------------
CURSOR c_hent_po_tilpersoner(fra_person_id NUMBER) IS
    SELECT *
    FROM PERSONOPPLYSNING
    WHERE id IN (
        SELECT tilperson
        FROM FAMILIERELASJON
        WHERE
            fraperson = fra_person_id
    ) AND aktoer_id IS NOT NULL;

CURSOR c_hent_adresser(po_opplysning_id NUMBER) IS
    SELECT * FROM OPPLYSNING_ADRESSE WHERE PERSONOPPLYSNING_ID =  po_opplysning_id;

CURSOR c_hent_familierelasjon(fra_po_id NUMBER, til_po_id NUMBER) IS
    SELECT * FROM FAMILIERELASJON WHERE FRAPERSON = fra_po_id AND TILPERSON = til_po_id;

-----------------------------------------------------
-- Sjekker om angitt po-id har overstyrt data
-----------------------------------------------------
FUNCTION f_har_overstyrt_data(po_id NUMBER)
    RETURN BOOLEAN IS
    v_tmp_count INTEGER := 0;
    BEGIN
        SELECT count(*)
        INTO v_tmp_count
        FROM PERSONOPPLYSNING
        WHERE id = po_id AND VALGT_OPPLYSNING_ID IS NOT NULL;
        IF v_tmp_count > 0
        THEN
            RETURN TRUE;
        ELSE
            RETURN FALSE;
        END IF;
    END;

-----------------------------------------------------
-- Vi skal ignoerere alle personopplysning, inkl. deres
-- relasjoner hvor aktoer_id er null
-----------------------------------------------------
FUNCTION f_skal_kopiere_po(po_id NUMBER)
    RETURN BOOLEAN IS
    BEGIN
        FOR po IN c_hent_personopplysning(po_id) LOOP
            RETURN TRUE;
        END LOOP;
        RETURN FALSE;
    END;

------------------------------------------------------------
-- Sjekker om personopplysning-grunnlaget har personopplysninger
------------------------------------------------------------
FUNCTION f_har_personopplysninger(po_grunnlag_id NUMBER) RETURN BOOLEAN IS
    v_tmp_count INTEGER := 0;
    BEGIN
        SELECT count(*) into v_tmp_count FROM GR_PERSONOPPLYSNING
        WHERE id = po_grunnlag_id and soeker_personoppl_id is null;
        IF  v_tmp_count > 0 THEN
            RETURN FALSE;
        ELSE
            RETURN TRUE;
        END IF;
    END;

-----------------------------------------------------
-- Oppretter nytt aggregat (PO_INFORMASJON) og
-- returnerer id. For overstyring av data brukes
-- GR_PERSONOPPLYSNING.overstyrt_informasjon_id
-----------------------------------------------------
PROCEDURE p_opprett_po_aggregat(po_grunnlag_id IN NUMBER, overstyring IN BOOLEAN, next_id OUT NUMBER) IS
    BEGIN
        SELECT SEQ_PO_INFORMASJON.nextval INTO next_id FROM dual;
        INSERT INTO PO_INFORMASJON (id) VALUES (next_id);
        IF (overstyring)
        THEN
            UPDATE GR_PERSONOPPLYSNING
            SET overstyrt_informasjon_id = next_id
            WHERE id = po_grunnlag_id;
        ELSE
            UPDATE GR_PERSONOPPLYSNING
            SET registrert_informasjon_id = next_id
            WHERE id = po_grunnlag_id;
        END IF;
    END;

-----------------------------------------------------
-- Kopiering til PO_PERSONSTATUS for angitt po-id,
-- med eller uten overstyring
-----------------------------------------------------
PROCEDURE p_kopier_personstatus(person_po_id IN NUMBER, po_informasjon_id IN NUMBER, overstyring IN BOOLEAN) IS
    v_personstatus VARCHAR2(20 CHAR);
    BEGIN

        OPEN c_hent_personopplysning(person_po_id);
        FETCH c_hent_personopplysning INTO personopplysning_row;

        IF (overstyring)
        THEN
            SELECT personstatus_type
            INTO v_personstatus
            FROM VALGT_OPPLYSNING
            WHERE id IN (
                SELECT valgt_opplysning_id
                FROM PERSONOPPLYSNING
                WHERE id = person_po_id
            );
        ELSE
            v_personstatus := personopplysning_row.personstatus_type;
        END IF;

        INSERT INTO PO_PERSONSTATUS (id, aktoer_id, fom, tom, po_informasjon_id, personstatus,
                                     opprettet_av, opprettet_tid, endret_av, endret_tid)
        VALUES (
            SEQ_PO_PERSONSTATUS.nextval,
            personopplysning_row.aktoer_id,
            V_FOM_DATO,
            V_TIDENES_ENDE,
            po_informasjon_id,
            v_personstatus,
            personopplysning_row.opprettet_av,
            personopplysning_row.opprettet_tid,
            personopplysning_row.endret_av,
            personopplysning_row.endret_tid);

        CLOSE c_hent_personopplysning;
    END;

-----------------------------------------------------
-- Kopier til PO_STATSBORGERSKAP for angitt po-id,
-- med eller uten overstyring
-----------------------------------------------------
PROCEDURE p_kopier_statsborgerskap(person_po_id IN NUMBER, po_informasjon_id IN NUMBER, overstyring IN BOOLEAN) IS
    v_statsborgerskap VARCHAR2(20 CHAR);
    BEGIN

        OPEN c_hent_personopplysning(person_po_id);
        FETCH c_hent_personopplysning INTO personopplysning_row;

        IF (overstyring)
        THEN
            SELECT statsborgerskap INTO v_statsborgerskap FROM VALGT_OPPLYSNING
            WHERE id IN (
                SELECT valgt_opplysning_id
                FROM PERSONOPPLYSNING
                WHERE id = person_po_id
            );
        ELSE
            v_statsborgerskap := personopplysning_row.statsborgerskap;
        END IF;

        INSERT INTO PO_STATSBORGERSKAP (id, aktoer_id, fom, tom, po_informasjon_id,
                                        statsborgerskap, opprettet_av, opprettet_tid, endret_av, endret_tid)
        VALUES (
            SEQ_PO_STATSBORGERSKAP.nextval,
            personopplysning_row.aktoer_id,
            V_FOM_DATO,
            V_TIDENES_ENDE,
            po_informasjon_id,
            v_statsborgerskap,
            personopplysning_row.opprettet_av,
            personopplysning_row.opprettet_tid,
            personopplysning_row.endret_av,
            personopplysning_row.endret_tid);

        CLOSE c_hent_personopplysning;
    END;

-----------------------------------------------------
-- Kopier til PO_ADRESSE
-----------------------------------------------------
PROCEDURE p_kopier_adresse(person_po_id IN NUMBER, po_informasjon_id IN NUMBER) IS
    BEGIN

        OPEN c_hent_personopplysning(person_po_id);
        FETCH c_hent_personopplysning INTO personopplysning_row;

        FOR adr IN c_hent_adresser(person_po_id)
        LOOP
            INSERT INTO PO_ADRESSE
            (id, aktoer_id, fom, tom, po_informasjon_id, adresse_type,
             adresselinje1, adresselinje2, adresselinje3, postnummer, poststed, land,
             opprettet_av, opprettet_tid, endret_av, endret_tid)
            VALUES (
                SEQ_PO_ADRESSE.nextval,
                personopplysning_row.aktoer_id,
                V_FOM_DATO,
                V_TIDENES_ENDE,
                po_informasjon_id,
                adr.adresse_type,
                adr.adresselinje1,
                adr.adresselinje2,
                adr.adresselinje3,
                adr.postnummer,
                adr.poststed,
                adr.land,
                adr.opprettet_av,
                adr.opprettet_tid,
                adr.endret_av,
                adr.endret_tid);
        END LOOP;

        CLOSE c_hent_personopplysning;
    END;

-----------------------------------------------------
-- Kopier en rad til PO_PERSONOPPLYSNING for angitt po-id,
-- med eller uten overstyring
-----------------------------------------------------
PROCEDURE p_kopier_personopplysning(person_po_id IN NUMBER, po_informasjon_id IN NUMBER) IS
    BEGIN

        OPEN c_hent_personopplysning(person_po_id);
        FETCH c_hent_personopplysning INTO personopplysning_row;

        INSERT INTO PO_PERSONOPPLYSNING (id, aktoer_id, po_informasjon_id,
                                         navn, foedselsdato, doedsdato, bruker_kjoenn, sivilstand_type,
                                         region, opprettet_av, opprettet_tid, endret_av, endret_tid)
        VALUES (
            SEQ_PO_PERSONOPPLYSNING.nextval,
            personopplysning_row.aktoer_id,
            po_informasjon_id,
            personopplysning_row.navn,
            personopplysning_row.foedselsdato,
            personopplysning_row.doedsdato,
            personopplysning_row.bruker_kjoenn,
            personopplysning_row.sivilstand_type,
            personopplysning_row.region,
            personopplysning_row.opprettet_av,
            personopplysning_row.opprettet_tid,
            personopplysning_row.endret_av,
            personopplysning_row.endret_tid
        );

        CLOSE c_hent_personopplysning;
    END;

-----------------------------------------------------
-- Kopier til PO_RELASJON
-----------------------------------------------------
PROCEDURE p_kopier_relasjon(fra_po_id IN NUMBER, til_po_id IN NUMBER, po_informasjon_id IN NUMBER) IS
    fra_aktoer_id       INTEGER := -1;
    til_aktoer_id       INTEGER := -1;
    familierelasjon_row FAMILIERELASJON%ROWTYPE;
    BEGIN
        SELECT AKTOER_ID INTO fra_aktoer_id FROM PERSONOPPLYSNING WHERE id = fra_po_id;
        SELECT AKTOER_ID INTO til_aktoer_id FROM PERSONOPPLYSNING WHERE id = til_po_id;

        OPEN c_hent_familierelasjon(fra_po_id, til_po_id);
        FETCH c_hent_familierelasjon INTO familierelasjon_row;

        INSERT INTO PO_RELASJON (
            id, fra_aktoer_id, til_aktoer_id,
            po_informasjon_id, relasjonsrolle, har_samme_bosted,
            opprettet_av, opprettet_tid, endret_av, endret_tid)
        VALUES (
            SEQ_PO_RELASJON.nextval,
            fra_aktoer_id,
            til_aktoer_id,
            po_informasjon_id,
            familierelasjon_row.relasjonsrolle,
            familierelasjon_row.har_samme_bosted,
            familierelasjon_row.opprettet_av,
            familierelasjon_row.opprettet_tid,
            familierelasjon_row.endret_av,
            familierelasjon_row.endret_tid
        );

        CLOSE c_hent_familierelasjon;
    END;

------------------------------------------------------------------------------------------------
-- Delgerer kopi til nytt aggregat som utgjør følgende nye tabeller:
-- PO_ADRESSE, PO_PERSONOPPLYSNING, PO_PERSONSTATUS, PO_RELASJON, PO_STATSBORGERSKAP. Disse er
-- samlet under (aggregat) PO_INFORMASJON, her representert av po_informasjon_id.
--
-- Data kopieres fra PERSONOPPLYSNING, FAMILIERELASJON, VALGT_OPPLYSNING, og OPPLYSNING_ADRESSE
--
-- Hvis informasjon er overtyrt (PERSONOPPLYSNING.VALGT_OPPLYSNING_ID eksisterer), kopieres alt
-- med overstyring, og denne pekes til fra GR_PERSONOPPLYSNING.overstyrt_informasjon_id
------------------------------------------------------------------------------------------------
PROCEDURE p_migrer_til_ny_struktur(po_grunnlag_id IN NUMBER, po_informasjon_id IN NUMBER) IS
    fra_person_po_id  INTEGER := -1;
    overstyring       BOOLEAN := FALSE;
    overstyrt_info_id INTEGER := -1;
    BEGIN
        SELECT soeker_personoppl_id
        INTO fra_person_po_id
        FROM GR_PERSONOPPLYSNING
        WHERE id = po_grunnlag_id;

        IF (f_skal_kopiere_po(fra_person_po_id))
        THEN
            p_kopier_personstatus(fra_person_po_id, po_informasjon_id, FALSE);
            p_kopier_statsborgerskap(fra_person_po_id, po_informasjon_id, FALSE);
            p_kopier_adresse(fra_person_po_id, po_informasjon_id);
            p_kopier_personopplysning(fra_person_po_id, po_informasjon_id);

            -- Finnes overstyrt data?
            overstyring := f_har_overstyrt_data(fra_person_po_id);
            IF (overstyring)
            THEN
                p_opprett_po_aggregat(po_grunnlag_id, TRUE, overstyrt_info_id);

                -- Kopierer over til overstyrt kun det som har endret seg
                -- Skal ikke overstyre brukerkjønn
                p_kopier_personstatus(fra_person_po_id, overstyrt_info_id, TRUE);
                p_kopier_statsborgerskap(fra_person_po_id, overstyrt_info_id, TRUE);
            END IF;

            -- Migrering av relaterte til-personer for fra_person_po_id
            FOR tilPo IN c_hent_po_tilpersoner(fra_person_po_id)
            LOOP
                p_kopier_personstatus(tilPo.id, po_informasjon_id, FALSE);
                p_kopier_statsborgerskap(tilPo.id, po_informasjon_id, FALSE);
                p_kopier_adresse(tilPo.id, po_informasjon_id);
                p_kopier_personopplysning(tilPo.id, po_informasjon_id);
                p_kopier_relasjon(fra_person_po_id, tilPo.id, po_informasjon_id);

                -- Finnes overstyrt data?
                IF (overstyring)
                THEN
                    -- Kopierer over til overstyrt kun det som har endret seg
                    -- Skal ikke overstyre brukerkjønn
                    p_kopier_personstatus(tilPo.id, overstyrt_info_id, TRUE);
                    p_kopier_statsborgerskap(tilPo.id, overstyrt_info_id, TRUE);
                END IF;
            END LOOP;
        END IF;
    END;

-----------------------------------------------------------
-- Migrerer data til nytt aggregat for personopplysninger,
-- nå med støtte for historikk av opplysninger
-----------------------------------------------------------
PROCEDURE kjoer_migrering IS
    reg_info_id INTEGER := -1;
    BEGIN
        FOR grunnlag IN c_hent_alle_po_grunnlag
        LOOP
            IF (f_har_personopplysninger(grunnlag.id))
            THEN
                DBMS_OUTPUT.PUT_LINE('Starter migrering av po-grunnlag:' || grunnlag.id);

                p_opprett_po_aggregat(grunnlag.id, FALSE, reg_info_id);
                p_migrer_til_ny_struktur(grunnlag.id, reg_info_id);

                DBMS_OUTPUT.PUT_LINE('Migrering av po-grunnlag ferdig:' || grunnlag.id);
            END IF;
        END LOOP;
    END;

-------------------------------------------------------------------------------
-- Slett kolonnen GR_PERSONOPPLYSNING.soeker_personoppl_id
-- Slett PERSONOPPLYSNING
-- Slett andre migrerte tabeller (FAMILIERELASJON, OPPLYSNING_ADRESSE etc)
-------------------------------------------------------------------------------
PROCEDURE kjoer_opprydding IS
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Starter opprydding etter migrering');
        EXECUTE IMMEDIATE 'drop table FAMILIERELASJON';
        EXECUTE IMMEDIATE 'drop table OPPLYSNING_ADRESSE';
        EXECUTE IMMEDIATE 'drop table PERSONOPPLYSNING';
        EXECUTE IMMEDIATE 'drop table VALGT_OPPLYSNING';
        DBMS_OUTPUT.PUT_LINE('Ferdig med opprydding etter migrering');
    END;

--------------------------------------------------------------
-------------------------- M A I N ---------------------------
--------------------------------------------------------------
BEGIN
    kjoer_migrering;
    EXECUTE IMMEDIATE 'alter table GR_PERSONOPPLYSNING drop column soeker_personoppl_id';
    -- kjoer_opprydding; // TODO avklaring opprydding/sletting av tabeller
END;
/
