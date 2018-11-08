package no.nav.foreldrepenger.domene.mottak.hendelser.impl.oversetter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.ForretningshendelsestypeRef;
import no.nav.foreldrepenger.domene.mottak.hendelser.ForretningshendelseOversetter;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.ForretningshendelseDto;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.YtelseHendelse;
import no.nav.foreldrepenger.domene.mottak.ytelse.YtelseForretningshendelse;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

@ApplicationScoped
@ForretningshendelsestypeRef(ForretningshendelsestypeRef.YTELSE_HENDELSE)
public class YtelseForretningshendelseOversetter implements ForretningshendelseOversetter<YtelseForretningshendelse> {

    private KodeverkRepository kodeverkRepository;

    @Inject
    public YtelseForretningshendelseOversetter(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public YtelseForretningshendelse oversett(ForretningshendelseDto forretningshendelse) {
        ForretningshendelseType forretningshendelseType = kodeverkRepository.finn(ForretningshendelseType.class, forretningshendelse.getForretningshendelseType());

        YtelseHendelse ytelseHendelse = JsonMapper.fromJson(forretningshendelse.getPayloadJson(), YtelseHendelse.class);
        return new YtelseForretningshendelse(forretningshendelseType, ytelseHendelse.getAktoerId(), ytelseHendelse.getFom());
    }
}
