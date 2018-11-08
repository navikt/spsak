package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.math.BigDecimal;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidUtil;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidPåHeltidTjeneste;

public class ArbeidPåHeltidTjenesteImpl implements ArbeidPåHeltidTjeneste {
    private final Behandling behandling;
    private UttakArbeidTjeneste uttakArbeidTjeneste;

    public ArbeidPåHeltidTjenesteImpl(Behandling behandling, UttakArbeidTjeneste uttakArbeidTjeneste) {
        this.behandling = behandling;
        this.uttakArbeidTjeneste = uttakArbeidTjeneste;
    }

    @Override
    public boolean jobberFulltid(OppgittPeriode søknadsperiode) {
        return beregnStillingsprosent(søknadsperiode).compareTo(BigDecimal.valueOf(100)) >= 0;
    }

    private BigDecimal beregnStillingsprosent(OppgittPeriode søknadsperiode) {
        BigDecimal stillingsprosent = BigDecimal.ZERO;
        List<Yrkesaktivitet> yrkesAktiviteter = uttakArbeidTjeneste.hentYrkesAktiviteterOrdinærtArbeidsforhold(behandling);
        for (Yrkesaktivitet yrkesaktivitet : yrkesAktiviteter) {
            for (AktivitetsAvtale aktivitetsAvtale : yrkesaktivitet.getAktivitetsAvtaler()) {
                if (!aktivitetsAvtale.getTilOgMed().isBefore(søknadsperiode.getTom())) {
                    stillingsprosent = stillingsprosent.add(UttakArbeidUtil.hentStillingsprosent(aktivitetsAvtale));
                }
            }
        }
        return stillingsprosent;
    }

}
