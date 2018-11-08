package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;

@ApplicationScoped
@BehandlingTypeRef("BT-002")
@FagsakYtelseTypeRef("FP")
public class KompletthetssjekkerInntektsmeldingImpl implements KompletthetssjekkerInntektsmelding {

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    KompletthetssjekkerInntektsmeldingImpl() {
        // CDI
    }

    @Inject
    public KompletthetssjekkerInntektsmeldingImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    /**
     * Henter alle påkrevde inntektsmeldinger fra aa-reg, og filtrerer ut alle
     * mottate.
     *
     * @return Manglende påkrevde inntektsmeldinger som ennå ikke er mottatt
     */
    @Override
    public List<ManglendeVedlegg> utledManglendeInntektsmeldinger(Behandling behandling) {
        return doUtledManglendeInntektsmeldinger(behandling, true);
    }

    @Override
    public List<ManglendeVedlegg> utledManglendeInntektsmeldingerFraGrunnlag(Behandling behandling) {
        return doUtledManglendeInntektsmeldinger(behandling, false);
    }

    private List<ManglendeVedlegg> doUtledManglendeInntektsmeldinger(Behandling behandling, boolean brukArkiv) {
        List<ManglendeVedlegg> manglendeVedlegg = (brukArkiv ? inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraArkiv(behandling)
            : inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraGrunnlag(behandling))
            .entrySet()
            .stream()
            .map(it -> new ManglendeVedlegg(DokumentTypeId.INNTEKTSMELDING, it.getKey()))
            .collect(Collectors.toList());
        return manglendeVedlegg;
    }
}
