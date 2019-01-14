package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.aksjonspunkt.BekreftOpptjeningPeriodeDto;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;

class BekreftOpptjeningPeriodeAksjonspunkt {
    private KodeverkRepository kodeverkRepository;
    private VirksomhetTjeneste virksomhetTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private AksjonspunktutlederForVurderOpptjening vurderOpptjening;

    BekreftOpptjeningPeriodeAksjonspunkt(GrunnlagRepositoryProvider provider, VirksomhetTjeneste virksomhetTjeneste,
                                         InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, AksjonspunktutlederForVurderOpptjening vurderOpptjening) {
        this.kodeverkRepository = provider.getKodeverkRepository();
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.vurderOpptjening = vurderOpptjening;
    }

    void oppdater(Behandling behandling, Collection<BekreftOpptjeningPeriodeDto> bekreftOpptjeningPerioder) {

        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForSaksbehandlet(behandling);
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder overstyrtBuilder = builder.getAktørArbeidBuilder(behandling.getAktørId());

        Map<OpptjeningAktivitetType, Set<ArbeidType>> kodeRelasjonMap = kodeverkRepository.hentKodeRelasjonForKodeverk(OpptjeningAktivitetType.class, ArbeidType.class);

        final List<BekreftOpptjeningPeriodeDto> bekreftetOverstyrtPeriode = bekreftOpptjeningPerioder.stream()
            .filter(it -> kanOverstyresOgSkalKunneLagreResultat(behandling, kodeRelasjonMap, it))
            .collect(Collectors.toList());
        for (BekreftOpptjeningPeriodeDto periode : bekreftetOverstyrtPeriode) {
            YrkesaktivitetBuilder yrkesaktivitetBuilder = getYrkesaktivitetBuilder(behandling, overstyrtBuilder, periode, kodeRelasjonMap.get(periode.getAktivitetType()));
            if (periode.getErGodkjent()) {
                YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder
                    .getAktivitetsAvtaleBuilder(getOrginalPeriode(periode), true);

                håndterPeriodeForAnnenOpptjening(periode, yrkesaktivitetBuilder, aktivitetsAvtaleBuilder);
                aktivitetsAvtaleBuilder.medBeskrivelse(periode.getBegrunnelse());
                yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder);
                overstyrtBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
            } else {
                yrkesaktivitetBuilder.fjernPeriode(getOrginalPeriode(periode));
                if (yrkesaktivitetBuilder.harIngenAvtaler() && yrkesaktivitetBuilder.getErOppdatering()) {
                    // Finnes perioden i builder så skal den fjernes.
                    overstyrtBuilder.fjernYrkesaktivitetHvisFinnes(yrkesaktivitetBuilder);
                }
            }
        }
        builder.leggTilAktørArbeid(overstyrtBuilder);
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);
    }

    private void håndterPeriodeForAnnenOpptjening(BekreftOpptjeningPeriodeDto periode, YrkesaktivitetBuilder yrkesaktivitetBuilder,
                                                  YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder) {
        if (OpptjeningAktivitetType.ANNEN_OPPTJENING.contains(periode.getAktivitetType()) || periode.getAktivitetType().equals(OpptjeningAktivitetType.NÆRING)) {
            if (periode.getAktivitetType().equals(OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD)) {
                settArbeidsgiverInformasjon(periode.getArbeidsgiver(), periode.getOppdragsgiverOrg(), yrkesaktivitetBuilder);
            }
            final DatoIntervallEntitet aktivitetsPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(periode.getOpptjeningFom(), periode.getOpptjeningTom());
            aktivitetsAvtaleBuilder.medPeriode(aktivitetsPeriode);
        }
    }

    private void settArbeidsgiverInformasjon(String arbeidsgiver, String oppdragsgiverOrg, YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        if (arbeidsgiver != null) {
            yrkesaktivitetBuilder.medArbeidsgiverNavn(arbeidsgiver);
        }
        if (oppdragsgiverOrg != null) {
            Virksomhet virksomhet = virksomhetTjeneste.finnOrganisasjon(oppdragsgiverOrg)
                .orElseThrow(IllegalArgumentException::new); // Utvikler feil hvis exception;
            yrkesaktivitetBuilder.medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        }
    }

    private boolean kanOverstyresOgSkalKunneLagreResultat(Behandling behandling, Map<OpptjeningAktivitetType, Set<ArbeidType>> kodeRelasjonMap, BekreftOpptjeningPeriodeDto periode) {
        if (!kodeRelasjonMap.containsKey(periode.getAktivitetType())) {
            return false;
        }
        final Set<ArbeidType> arbeidTypes = kodeRelasjonMap.get(periode.getAktivitetType());
        return kanSaksbehandles(behandling, arbeidTypes, periode);
    }

    private boolean kanSaksbehandles(Behandling behandling, Set<ArbeidType> arbeidTypes, BekreftOpptjeningPeriodeDto periode) {
        if (OpptjeningAktivitetType.ARBEID.equals(periode.getAktivitetType())) {
            return harGittAksjonspunktForArbeidsforhold(behandling, arbeidTypes, periode);
        } else if (OpptjeningAktivitetType.NÆRING.equals(periode.getAktivitetType())) {
            return harGittAksjonspunktForNæring(behandling);
        }
        return OpptjeningAktivitetType.ANNEN_OPPTJENING.contains(periode.getAktivitetType());
    }

    private boolean harGittAksjonspunktForArbeidsforhold(Behandling behandling, Set<ArbeidType> arbeidTypes, BekreftOpptjeningPeriodeDto periode) {
        final Optional<InntektArbeidYtelseAggregat> registerFørVersjon = inntektArbeidYtelseTjeneste.hentAggregat(behandling).getOpplysningerFørSkjæringstidspunkt();
        if (!registerFørVersjon.isPresent()) {
            return false;
        }
        final InntektArbeidYtelseAggregat aggregat = registerFørVersjon.get();
        final Optional<AktørArbeid> aktørArbeidOptional = aggregat.getAktørArbeid().stream().filter(it -> it.getAktørId().equals(behandling.getAktørId())).findFirst();

        if (!aktørArbeidOptional.isPresent()) {
            return false;
        }
        final AktørArbeid aktørArbeid = aktørArbeidOptional.get();
        // Sjekk om 0%
        return aktørArbeid.getYrkesaktiviteter()
            .stream()
            .anyMatch(it -> arbeidTypes.contains(it.getArbeidType())
                && it.getArbeidsgiver().getIdentifikator().equals(periode.getOppdragsgiverOrg())
                && it.getAktivitetsAvtaler().stream().anyMatch(aa -> aa.getProsentsats() == null || aa.getProsentsats().erNulltall()));
    }

    private boolean harGittAksjonspunktForNæring(Behandling behandling) {
        return vurderOpptjening.girAksjonspunktForOppgittNæring(behandling);
    }

    private DatoIntervallEntitet getOrginalPeriode(BekreftOpptjeningPeriodeDto periode) {
        if (periode.getErManueltOpprettet()) {
            final LocalDate tomDato = periode.getOpptjeningTom() != null ? periode.getOpptjeningTom() : Tid.TIDENES_ENDE;
            return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getOpptjeningFom(), tomDato);
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getOriginalFom(), periode.getOriginalTom());
    }

    private YrkesaktivitetBuilder getYrkesaktivitetBuilder(Behandling behandling, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder overstyrtBuilder,
                                                           BekreftOpptjeningPeriodeDto periodeDto,
                                                           Set<ArbeidType> arbeidType) {
        if (arbeidType == null || arbeidType.isEmpty()) {
            throw new IllegalStateException("Støtter ikke " + periodeDto.getAktivitetType().getKode());
        }
        YrkesaktivitetBuilder builder;
        if (periodeDto.getAktivitetType().equals(OpptjeningAktivitetType.ARBEID)) {
            Opptjeningsnøkkel nøkkel;
            if(OrganisasjonsNummerValidator.erGyldig(periodeDto.getOppdragsgiverOrg())) {
                nøkkel = new Opptjeningsnøkkel(periodeDto.getArbeidsforholdRef(), periodeDto.getOppdragsgiverOrg(), null);
            } else {
                nøkkel = new Opptjeningsnøkkel(periodeDto.getArbeidsforholdRef(), null, periodeDto.getOppdragsgiverOrg());
            }
            builder = overstyrtBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidType);

            if (!builder.getErOppdatering()) {
                // Bør få med all informasjon om arbeidsforholdet over i overstyrt slik at ingenting blir mistet.
                builder = getRegisterBuilder(behandling).getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidType)
                    .migrerFraRegisterTilOverstyrt();
            }
            builder.medArbeidsforholdId(ArbeidsforholdRef.ref(periodeDto.getArbeidsforholdRef()));
        } else {
            builder = overstyrtBuilder.getYrkesaktivitetBuilderForType(arbeidType.stream().findFirst().orElse(ArbeidType.UDEFINERT));
        }
        return builder;
    }

    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder getRegisterBuilder(Behandling behandling) {
        return inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling).getAktørArbeidBuilder(behandling.getAktørId());
    }
}
