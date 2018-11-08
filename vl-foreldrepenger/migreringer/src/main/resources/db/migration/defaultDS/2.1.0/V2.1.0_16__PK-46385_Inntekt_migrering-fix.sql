update INNTEKTSPOST set kl_ytelse_type = 'YTELSE_FRA_OFFENTLIGE', ytelse_type = '-' where INNTEKTSPOST_TYPE = 'LONN' and YTELSE_TYPE is null;
