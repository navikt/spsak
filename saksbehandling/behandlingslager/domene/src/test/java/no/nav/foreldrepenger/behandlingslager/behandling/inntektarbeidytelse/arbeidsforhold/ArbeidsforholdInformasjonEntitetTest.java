package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;

public class ArbeidsforholdInformasjonEntitetTest {

    @Test
    public void skal_beholde_referanse_til() {
        final VirksomhetEntitet virksomhet1 = new VirksomhetEntitet.Builder().medOrgnr("1234").build();
        final VirksomhetEntitet virksomhet2 = new VirksomhetEntitet.Builder().medOrgnr("5678").build();
        final Arbeidsgiver arbeidsgiver1 = Arbeidsgiver.virksomhet(virksomhet1);
        final Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(virksomhet2);
        final ArbeidsforholdInformasjonEntitet entitet = new ArbeidsforholdInformasjonEntitet();

        final ArbeidsforholdRef ref1 = ArbeidsforholdRef.ref("1234");
        final ArbeidsforholdRef ref2 = ArbeidsforholdRef.ref("5678");
        entitet.finnEllerOpprett(arbeidsgiver1, ref1);
        entitet.finnEllerOpprett(arbeidsgiver1, ref2);
        entitet.finnEllerOpprett(arbeidsgiver2, ref1);
        entitet.finnEllerOpprett(arbeidsgiver2, ref2);
        entitet.erstattArbeidsforhold(arbeidsgiver1, entitet.finnForEkstern(arbeidsgiver1, ref1), entitet.finnForEkstern(arbeidsgiver1, ref2));

        final ArbeidsforholdOverstyringBuilder overstyringBuilderFor = entitet.getOverstyringBuilderFor(arbeidsgiver1, entitet.finnForEkstern(arbeidsgiver1, ref1));
        overstyringBuilderFor.medNyArbeidsforholdRef(entitet.finnForEkstern(arbeidsgiver1, ref2))
            .medHandling(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            .medBeskrivelse("asdf");
        entitet.leggTilOverstyring(overstyringBuilderFor.build());

        assertThat(entitet.finnForEkstern(arbeidsgiver1, ref1)).isNotEqualTo(ref1);
        assertThat(entitet.finnForEkstern(arbeidsgiver1, ref2)).isNotEqualTo(ref2);
        assertThat(entitet.finnForEkstern(arbeidsgiver1, ref1)).isEqualTo(entitet.finnForEkstern(arbeidsgiver1, ref2));
        assertThat(entitet.finnForEksternBeholdHistoriskReferanse(arbeidsgiver1, ref1)).isNotEqualTo(entitet.finnForEkstern(arbeidsgiver1, ref2));
        assertThat(entitet.finnEllerOpprett(arbeidsgiver1, ref1)).isEqualTo(entitet.finnEllerOpprett(arbeidsgiver1, ref2));
        assertThat(entitet.finnForEkstern(arbeidsgiver1, ref1)).isEqualTo(entitet.finnForEksternBeholdHistoriskReferanse(arbeidsgiver1, ref2));
        assertThat(entitet.finnForEkstern(arbeidsgiver2, ref1)).isNotEqualTo(entitet.finnForEkstern(arbeidsgiver2, ref2));
    }
}
