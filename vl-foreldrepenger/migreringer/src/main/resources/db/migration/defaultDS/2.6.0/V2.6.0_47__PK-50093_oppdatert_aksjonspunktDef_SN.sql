-- oppdatering av navn og beskrivelse til aksjonspunktdefinisjon for aksjonspunkt 5039
update aksjonspunkt_def
set
navn = 'Vurder varig endret/nyoppstartet næring selvstendig næringsdrivende',
beskrivelse = 'Vurder varig endret/nyoppstartet næring for selvstendig næringsdrivende'
where
kode = '5039';
