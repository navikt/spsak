update KODELISTE SET ekstra_data='{"infotrygdOppe":"true"}' where offisiell_kode in ('IP', 'UB', 'SG', 'UK', 'RT', 'ST', 'VD', 'VI', 'VT') 
and kodeverk='RELATERT_YTELSE_STATUS';

update KODELISTE set ekstra_data = '{"mal": "' || ekstra_data || '"}' where kodeverk='HISTORIKKINNSLAG_TYPE' and ekstra_data like 'TYPE%';