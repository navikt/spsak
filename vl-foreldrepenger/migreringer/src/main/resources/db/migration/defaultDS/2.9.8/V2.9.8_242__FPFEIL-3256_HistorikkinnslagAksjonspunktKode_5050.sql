update historikkinnslag_felt
set til_verdi = '5058',
    ENDRET_AV = 'FPFEIL-3256',
    ENDRET_TID = systimestamp,
    VERSJON = VERSJON + 1
where historikkinnslag_felt_type = 'AKSJONSPUNKT_KODE'
  and til_verdi = '5050';
