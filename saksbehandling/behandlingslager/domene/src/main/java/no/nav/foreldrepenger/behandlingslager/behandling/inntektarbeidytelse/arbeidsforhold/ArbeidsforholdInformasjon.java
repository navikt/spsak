package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;

public interface ArbeidsforholdInformasjon {
    ArbeidsforholdRef finnForEkstern(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref);

    ArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref);

    List<ArbeidsforholdOverstyringEntitet> getOverstyringer();

    ArbeidsforholdRef finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef);
}
