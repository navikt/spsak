-- Ta bort "til søker" som er misvisende når mottaker kan være annen enn søker (f.eks. verge)
UPDATE HISTORIKKINNSLAG_TYPE SET NAVN = 'Melding er sendt' WHERE KODE = 'BREV_SENT';
