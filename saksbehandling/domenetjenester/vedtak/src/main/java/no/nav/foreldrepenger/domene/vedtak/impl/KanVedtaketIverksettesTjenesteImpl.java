package no.nav.foreldrepenger.domene.vedtak.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.vedtak.KanVedtaketIverksettesTjeneste;

@ApplicationScoped
public class KanVedtaketIverksettesTjenesteImpl implements KanVedtaketIverksettesTjeneste {

    private static final Logger log = LoggerFactory.getLogger(KanVedtaketIverksettesTjenesteImpl.class);

    KanVedtaketIverksettesTjenesteImpl() {
        // for CDI
    }

    @Inject
    public KanVedtaketIverksettesTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
    }

    @Override
    public boolean kanVedtaketIverksettes(Behandling behandling) {

        log.warn("All sjekk om vedtaket kan iverksettes er DEAKTIVERT. Iverksetter vedtak");
        return true;
    }

}
