package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.math.BigDecimal;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;

public class YtelseBeregningsgrunnlagArbeidsforhold {
    private final BigDecimal inntektForPerioden;
    private final String orgnr;
    private InntektPeriodeType inntektPeriodeType;


    YtelseBeregningsgrunnlagArbeidsforhold(Arbeidsforhold arbeidsforhold, KodeverkRepository kodeverkRepository) {
        inntektForPerioden = arbeidsforhold.getInntektForPerioden();
        orgnr = arbeidsforhold.getOrgnr();
        if(arbeidsforhold.getInntektsPeriode() != null) {
            this.inntektPeriodeType = kodeverkRepository.finnForKodeverkEiersKode(InntektPeriodeType.class, arbeidsforhold.getInntektsPeriode().getValue());
            // TODO (Termitt): Den over gjør ikke nødvendigvis jobben. Finner fra EM, men orElseGet dropper ballen - oversetter ikke nødvendigvis til IAT.
            if (this.inntektPeriodeType == null) {
                this.inntektPeriodeType = InntektPeriodeType.MÅNEDLIG;
            }
        }
    }

    public BigDecimal getInntektForPerioden() {
        return inntektForPerioden;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public boolean harGyldigOrgnr() {
        return OrganisasjonsNummerValidator.erGyldig(orgnr);
    }

    public InntektPeriodeType getInntektPeriodeType() {
        return inntektPeriodeType;
    }
}
