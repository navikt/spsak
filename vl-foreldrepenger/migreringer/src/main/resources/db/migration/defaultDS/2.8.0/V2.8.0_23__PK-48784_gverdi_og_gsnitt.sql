-- Nyt kodeverk for grunnbeløp og gsnitt
INSERT INTO KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'GRUNNBELØP', 'Grunnbeløp', 'Grunnbeløp', 'SATS_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) VALUES (seq_kodeliste.nextval, 'GSNITT', 'Grunnbeløp årsgjennomsnitt', 'Grunnbeløp årsgjennomsnitt', 'SATS_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Sats verdier for grunnbeløp og gsnitt fra 1967 til 2017
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2017-05-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 93634);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2017-01-01', 'YYYY-MM-DD'), to_date('2017-12-31', 'YYYY-MM-DD'), 93281);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2016-05-01', 'YYYY-MM-DD'), to_date('2017-04-30', 'YYYY-MM-DD'), 92576);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2016-01-01', 'YYYY-MM-DD'), to_date('2016-12-31', 'YYYY-MM-DD'), 91740);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2015-05-01', 'YYYY-MM-DD'), to_date('2016-04-30', 'YYYY-MM-DD'), 90068);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2015-01-01', 'YYYY-MM-DD'), to_date('2015-12-31', 'YYYY-MM-DD'), 89502);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2014-05-01', 'YYYY-MM-DD'), to_date('2015-04-30', 'YYYY-MM-DD'), 88370);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2014-01-01', 'YYYY-MM-DD'), to_date('2014-12-31', 'YYYY-MM-DD'), 87328);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2013-05-01', 'YYYY-MM-DD'), to_date('2014-04-30', 'YYYY-MM-DD'), 85245);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2013-01-01', 'YYYY-MM-DD'), to_date('2013-12-31', 'YYYY-MM-DD'), 84204);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2012-05-01', 'YYYY-MM-DD'), to_date('2013-04-30', 'YYYY-MM-DD'), 82122);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2012-01-01', 'YYYY-MM-DD'), to_date('2012-12-31', 'YYYY-MM-DD'), 81153);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2011-05-01', 'YYYY-MM-DD'), to_date('2012-04-30', 'YYYY-MM-DD'), 79216);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2011-01-01', 'YYYY-MM-DD'), to_date('2011-12-31', 'YYYY-MM-DD'), 78024);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2010-05-01', 'YYYY-MM-DD'), to_date('2011-04-30', 'YYYY-MM-DD'), 75641);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2010-01-01', 'YYYY-MM-DD'), to_date('2010-12-31', 'YYYY-MM-DD'), 74721);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2009-05-01', 'YYYY-MM-DD'), to_date('2010-04-30', 'YYYY-MM-DD'), 72881);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2009-01-01', 'YYYY-MM-DD'), to_date('2009-12-31', 'YYYY-MM-DD'), 72006);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2008-05-01', 'YYYY-MM-DD'), to_date('2009-04-30', 'YYYY-MM-DD'), 70256);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2008-01-01', 'YYYY-MM-DD'), to_date('2008-12-31', 'YYYY-MM-DD'), 69108);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2007-05-01', 'YYYY-MM-DD'), to_date('2008-04-30', 'YYYY-MM-DD'), 66812);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2007-01-01', 'YYYY-MM-DD'), to_date('2007-12-31', 'YYYY-MM-DD'), 65505);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2006-05-01', 'YYYY-MM-DD'), to_date('2007-04-30', 'YYYY-MM-DD'), 62892);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2006-01-01', 'YYYY-MM-DD'), to_date('2006-12-31', 'YYYY-MM-DD'), 62161);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2005-05-01', 'YYYY-MM-DD'), to_date('2006-04-30', 'YYYY-MM-DD'), 60699);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2005-01-01', 'YYYY-MM-DD'), to_date('2005-12-31', 'YYYY-MM-DD'), 60059);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2004-05-01', 'YYYY-MM-DD'), to_date('2005-04-30', 'YYYY-MM-DD'), 58778);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2004-01-01', 'YYYY-MM-DD'), to_date('2004-12-31', 'YYYY-MM-DD'), 58139);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2003-05-01', 'YYYY-MM-DD'), to_date('2004-04-30', 'YYYY-MM-DD'), 56861);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2003-01-01', 'YYYY-MM-DD'), to_date('2003-12-31', 'YYYY-MM-DD'), 55964);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2002-05-01', 'YYYY-MM-DD'), to_date('2003-04-30', 'YYYY-MM-DD'), 54170);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2002-01-01', 'YYYY-MM-DD'), to_date('2002-12-31', 'YYYY-MM-DD'), 53233);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2001-05-01', 'YYYY-MM-DD'), to_date('2002-04-30', 'YYYY-MM-DD'), 51360);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2001-01-01', 'YYYY-MM-DD'), to_date('2001-12-31', 'YYYY-MM-DD'), 50603);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2000-05-01', 'YYYY-MM-DD'), to_date('2001-04-30', 'YYYY-MM-DD'), 49090);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2000-01-01', 'YYYY-MM-DD'), to_date('2000-12-31', 'YYYY-MM-DD'), 48377);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1999-05-01', 'YYYY-MM-DD'), to_date('2000-04-30', 'YYYY-MM-DD'), 46950);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1999-01-01', 'YYYY-MM-DD'), to_date('1999-12-31', 'YYYY-MM-DD'), 46423);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1998-05-01', 'YYYY-MM-DD'), to_date('1999-04-30', 'YYYY-MM-DD'), 45370);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1998-01-01', 'YYYY-MM-DD'), to_date('1998-12-31', 'YYYY-MM-DD'), 44413);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1997-05-01', 'YYYY-MM-DD'), to_date('1998-04-30', 'YYYY-MM-DD'), 42500);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1997-01-01', 'YYYY-MM-DD'), to_date('1997-12-31', 'YYYY-MM-DD'), 42000);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1996-05-01', 'YYYY-MM-DD'), to_date('1997-04-30', 'YYYY-MM-DD'), 41000);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1996-01-01', 'YYYY-MM-DD'), to_date('1996-12-31', 'YYYY-MM-DD'), 40410);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1995-05-01', 'YYYY-MM-DD'), to_date('1996-04-30', 'YYYY-MM-DD'), 39230);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1995-01-01', 'YYYY-MM-DD'), to_date('1995-12-31', 'YYYY-MM-DD'), 38847);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1994-05-01', 'YYYY-MM-DD'), to_date('1995-04-30', 'YYYY-MM-DD'), 38080);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1994-01-01', 'YYYY-MM-DD'), to_date('1994-12-31', 'YYYY-MM-DD'), 37820);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1993-05-01', 'YYYY-MM-DD'), to_date('1994-04-30', 'YYYY-MM-DD'), 37300);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1993-01-01', 'YYYY-MM-DD'), to_date('1993-12-31', 'YYYY-MM-DD'), 37033);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1992-05-01', 'YYYY-MM-DD'), to_date('1993-04-30', 'YYYY-MM-DD'), 36500);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1992-01-01', 'YYYY-MM-DD'), to_date('1992-12-31', 'YYYY-MM-DD'), 36167);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1991-05-01', 'YYYY-MM-DD'), to_date('1992-04-30', 'YYYY-MM-DD'), 35500);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1991-01-01', 'YYYY-MM-DD'), to_date('1991-12-31', 'YYYY-MM-DD'), 35033);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1990-05-01', 'YYYY-MM-DD'), to_date('1990-11-30', 'YYYY-MM-DD'), 34000);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1990-12-01', 'YYYY-MM-DD'), to_date('1991-04-30', 'YYYY-MM-DD'), 34100);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1990-01-01', 'YYYY-MM-DD'), to_date('1990-12-31', 'YYYY-MM-DD'), 33575);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1989-04-01', 'YYYY-MM-DD'), to_date('1990-04-30', 'YYYY-MM-DD'), 32700);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1989-01-01', 'YYYY-MM-DD'), to_date('1989-12-31', 'YYYY-MM-DD'), 32275);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1988-01-01', 'YYYY-MM-DD'), to_date('1988-03-31', 'YYYY-MM-DD'), 30400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1988-04-01', 'YYYY-MM-DD'), to_date('1989-03-31', 'YYYY-MM-DD'), 31000);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1988-01-01', 'YYYY-MM-DD'), to_date('1988-12-31', 'YYYY-MM-DD'), 30850);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1987-05-01', 'YYYY-MM-DD'), to_date('1987-12-31', 'YYYY-MM-DD'), 29900);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1987-01-01', 'YYYY-MM-DD'), to_date('1987-12-31', 'YYYY-MM-DD'), 29267);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1986-01-01', 'YYYY-MM-DD'), to_date('1986-04-30', 'YYYY-MM-DD'), 26300);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1986-05-01', 'YYYY-MM-DD'), to_date('1987-04-30', 'YYYY-MM-DD'), 28000);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1986-01-01', 'YYYY-MM-DD'), to_date('1986-12-31', 'YYYY-MM-DD'), 27433);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1985-05-01', 'YYYY-MM-DD'), to_date('1985-12-31', 'YYYY-MM-DD'), 25900);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1985-01-01', 'YYYY-MM-DD'), to_date('1985-12-31', 'YYYY-MM-DD'), 25333);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1984-05-01', 'YYYY-MM-DD'), to_date('1985-04-30', 'YYYY-MM-DD'), 24200);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1984-01-01', 'YYYY-MM-DD'), to_date('1984-12-31', 'YYYY-MM-DD'), 23667);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1983-01-01', 'YYYY-MM-DD'), to_date('1983-04-30', 'YYYY-MM-DD'), 21800);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1983-05-01', 'YYYY-MM-DD'), to_date('1984-04-30', 'YYYY-MM-DD'), 22600);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1983-01-01', 'YYYY-MM-DD'), to_date('1983-12-31', 'YYYY-MM-DD'), 22333);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1982-05-01', 'YYYY-MM-DD'), to_date('1982-12-31', 'YYYY-MM-DD'), 21200);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1982-01-01', 'YYYY-MM-DD'), to_date('1982-12-31', 'YYYY-MM-DD'), 20667);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1981-01-01', 'YYYY-MM-DD'), to_date('1981-04-30', 'YYYY-MM-DD'), 17400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1981-05-01', 'YYYY-MM-DD'), to_date('1981-09-30', 'YYYY-MM-DD'), 19100);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1981-10-01', 'YYYY-MM-DD'), to_date('1982-04-30', 'YYYY-MM-DD'), 19600);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1981-01-01', 'YYYY-MM-DD'), to_date('1981-12-31', 'YYYY-MM-DD'), 18658);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1980-01-01', 'YYYY-MM-DD'), to_date('1980-04-30', 'YYYY-MM-DD'), 16100);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1980-05-01', 'YYYY-MM-DD'), to_date('1980-12-31', 'YYYY-MM-DD'), 16900);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1980-01-01', 'YYYY-MM-DD'), to_date('1980-12-31', 'YYYY-MM-DD'), 16633);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1979-01-01', 'YYYY-MM-DD'), to_date('1979-12-31', 'YYYY-MM-DD'), 15200);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1979-01-01', 'YYYY-MM-DD'), to_date('1979-12-31', 'YYYY-MM-DD'), 15200);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1978-07-01', 'YYYY-MM-DD'), to_date('1978-12-31', 'YYYY-MM-DD'), 14700);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1978-01-01', 'YYYY-MM-DD'), to_date('1978-12-31', 'YYYY-MM-DD'), 14550);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1977-01-01', 'YYYY-MM-DD'), to_date('1977-04-30', 'YYYY-MM-DD'), 13100);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1977-05-01', 'YYYY-MM-DD'), to_date('1977-11-30', 'YYYY-MM-DD'), 13400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1977-12-01', 'YYYY-MM-DD'), to_date('1978-06-30', 'YYYY-MM-DD'), 14400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1977-01-01', 'YYYY-MM-DD'), to_date('1977-12-31', 'YYYY-MM-DD'), 13383);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1976-01-01', 'YYYY-MM-DD'), to_date('1976-04-30', 'YYYY-MM-DD'), 11800);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1976-05-01', 'YYYY-MM-DD'), to_date('1976-12-31', 'YYYY-MM-DD'), 12100);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1976-01-01', 'YYYY-MM-DD'), to_date('1976-12-31', 'YYYY-MM-DD'), 12000);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1975-01-01', 'YYYY-MM-DD'), to_date('1975-04-30', 'YYYY-MM-DD'), 10400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1975-05-01', 'YYYY-MM-DD'), to_date('1975-12-31', 'YYYY-MM-DD'), 11000);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1975-01-01', 'YYYY-MM-DD'), to_date('1975-12-31', 'YYYY-MM-DD'), 10800);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1974-01-01', 'YYYY-MM-DD'), to_date('1974-04-30', 'YYYY-MM-DD'), 9200);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1974-05-01', 'YYYY-MM-DD'), to_date('1974-12-31', 'YYYY-MM-DD'), 9700);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1974-01-01', 'YYYY-MM-DD'), to_date('1974-12-31', 'YYYY-MM-DD'), 9533);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1973-01-01', 'YYYY-MM-DD'), to_date('1973-12-31', 'YYYY-MM-DD'), 8500);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1973-01-01', 'YYYY-MM-DD'), to_date('1973-12-31', 'YYYY-MM-DD'), 8500);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1972-01-01', 'YYYY-MM-DD'), to_date('1972-12-31', 'YYYY-MM-DD'), 7900);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1972-01-01', 'YYYY-MM-DD'), to_date('1972-12-31', 'YYYY-MM-DD'), 7900);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1971-01-01', 'YYYY-MM-DD'), to_date('1971-04-30', 'YYYY-MM-DD'), 7200);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1971-05-01', 'YYYY-MM-DD'), to_date('1971-12-31', 'YYYY-MM-DD'), 7500);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1971-01-01', 'YYYY-MM-DD'), to_date('1971-12-31', 'YYYY-MM-DD'), 7400);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1970-01-01', 'YYYY-MM-DD'), to_date('1970-12-31', 'YYYY-MM-DD'), 6800);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1970-01-01', 'YYYY-MM-DD'), to_date('1970-12-31', 'YYYY-MM-DD'), 6800);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1969-01-01', 'YYYY-MM-DD'), to_date('1969-12-31', 'YYYY-MM-DD'), 6400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1969-01-01', 'YYYY-MM-DD'), to_date('1969-12-31', 'YYYY-MM-DD'), 6400);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1968-01-01', 'YYYY-MM-DD'), to_date('1968-12-31', 'YYYY-MM-DD'), 5900);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1968-01-01', 'YYYY-MM-DD'), to_date('1968-12-31', 'YYYY-MM-DD'), 5900);

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('1967-01-01', 'YYYY-MM-DD'), to_date('1967-12-31', 'YYYY-MM-DD'), 5400);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('1967-01-01', 'YYYY-MM-DD'), to_date('1967-12-31', 'YYYY-MM-DD'), 5400);
