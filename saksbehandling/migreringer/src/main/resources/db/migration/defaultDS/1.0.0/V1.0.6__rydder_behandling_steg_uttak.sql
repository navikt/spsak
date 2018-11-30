delete from behandling_type_steg_sekv where behandling_steg_type in ('KOFAKUT', 'SØKNADSFRIST_FP', 'VURDER_UTTAK');
delete from aksjonspunkt_def where vurderingspunkt like 'KOFAKUT%' OR vurderingspunkt like 'SØKNADSFRIST_FP%' or vurderingspunkt like 'VURDER_UTTAK%';
delete from vurderingspunkt_def where kode  like 'KOFAKUT%' OR kode like 'SØKNADSFRIST_FP%' or kode like 'VURDER_UTTAK%';

