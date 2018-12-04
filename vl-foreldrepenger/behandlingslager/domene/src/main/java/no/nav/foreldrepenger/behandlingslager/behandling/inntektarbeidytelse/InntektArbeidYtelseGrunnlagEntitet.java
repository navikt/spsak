package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;


import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "InntektArbeidGrunnlag")
@Table(name = "GR_ARBEID_INNTEKT")
public class InntektArbeidYtelseGrunnlagEntitet extends BaseEntitet implements InntektArbeidYtelseGrunnlag {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_ARBEID_INNTEKT")
    private Long id;

    @OneToOne
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false, unique = true)
    private Behandling behandling;

    @OneToOne
    @JoinColumn(name = "register_id", updatable = false, unique = true)
    @ChangeTracked
    private InntektArbeidYtelseAggregatEntitet register;

    @OneToOne
    @JoinColumn(name = "saksbehandlet_id", updatable = false, unique = true)
    @ChangeTracked
    private InntektArbeidYtelseAggregatEntitet saksbehandlet;

    @OneToOne
    @JoinColumn(name = "oppgitt_opptjening_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @OneToOne
    @ChangeTracked
    @JoinColumn(name = "inntektsmeldinger_id", updatable = false, unique = true)
    private InntektsmeldingAggregatEntitet inntektsmeldinger;

    @ChangeTracked
    @OneToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true)
    private ArbeidsforholdInformasjonEntitet informasjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Transient
    private LocalDate skjæringstidspunkt;

    InntektArbeidYtelseGrunnlagEntitet() {
    }

    InntektArbeidYtelseGrunnlagEntitet(InntektArbeidYtelseGrunnlag grunnlag) {
        // NB! skal aldri lage ny versjon av oppgitt opptjening!
        grunnlag.getOppgittOpptjening().ifPresent(kopiAvOppgittOpptjening -> this.setOppgittOpptjening((OppgittOpptjeningEntitet) kopiAvOppgittOpptjening));
        ((InntektArbeidYtelseGrunnlagEntitet) grunnlag).getRegisterVersjon().ifPresent(nyRegisterVerson -> this.setRegister((InntektArbeidYtelseAggregatEntitet) nyRegisterVerson));
        grunnlag.getSaksbehandletVersjon().ifPresent(nySaksbehandletFørVersjon -> this.setSaksbehandlet((InntektArbeidYtelseAggregatEntitet) nySaksbehandletFørVersjon));
        grunnlag.getInntektsmeldinger().ifPresent(this::setInntektsmeldinger);
        ((InntektArbeidYtelseGrunnlagEntitet) grunnlag).getInformasjon().ifPresent(info -> this.setInformasjon((ArbeidsforholdInformasjonEntitet) info));
    }

    Behandling getBehandling() {
        return behandling;
    }

    public void setSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    @Override
    public Optional<InntektArbeidYtelseAggregat> getSaksbehandletVersjon() {
        return Optional.ofNullable(saksbehandlet);
    }

    void setSaksbehandlet(InntektArbeidYtelseAggregatEntitet saksbehandletFør) {
        this.saksbehandlet = saksbehandletFør;
    }

    Optional<InntektArbeidYtelseAggregat> getRegisterVersjon() {
        return Optional.ofNullable(register);
    }

    @Override
    public Optional<InntektArbeidYtelseAggregat> getOpplysningerFørSkjæringstidspunkt() {
        if (register != null) {
            final InntektArbeidYtelseAggregatEntitet aggregat = new InntektArbeidYtelseAggregatEntitet(register);
            aggregat.taHensynTilBetraktninger(informasjon);
            aggregat.setSkjæringstidspunkt(skjæringstidspunkt, true);
            return Optional.of(aggregat);
        }
        return Optional.empty();
    }

    @Override
    public Optional<InntektArbeidYtelseAggregat> getOpplysningerEtterSkjæringstidspunkt() {
        if (register != null) {
            final InntektArbeidYtelseAggregatEntitet aggregat = new InntektArbeidYtelseAggregatEntitet(register);
            aggregat.taHensynTilBetraktninger(informasjon);
            aggregat.setSkjæringstidspunkt(skjæringstidspunkt, false);
            return Optional.of(aggregat);
        }
        return Optional.empty();
    }

    @Override
    public boolean harBlittSaksbehandlet() {
        return getSaksbehandletVersjon().isPresent();
    }

    @Override
    public Collection<AktørInntekt> getAktørInntektForFørStp() {
        return getOpplysningerFørSkjæringstidspunkt().map(InntektArbeidYtelseAggregat::getAktørInntekt).orElse(Collections.emptyList());
    }

    @Override
    public Optional<AktørInntekt> getAktørInntektForFørStp(AktørId aktørId) {
        return getAktørInntektForFørStp().stream().filter(a -> aktørId.equals(a.getAktørId())).findFirst();
    }

    @Override
    public Optional<AktørInntekt> getAktørInntektForEtterStp(AktørId aktørId) {
        return getOpplysningerEtterSkjæringstidspunkt()
            .map(InntektArbeidYtelseAggregat::getAktørInntekt).orElse(Collections.emptyList())
            .stream().filter(a -> aktørId.equals(a.getAktørId())).findFirst();
    }

    @Override
    public Collection<AktørArbeid> getAktørArbeidFørStp() {
        return getOpplysningerFørSkjæringstidspunkt().map(InntektArbeidYtelseAggregat::getAktørArbeid).orElse(Collections.emptyList());
    }

    @Override
    public Optional<AktørArbeid> getAktørArbeidFørStp(AktørId aktørId) {
        return getAktørArbeidFørStp().stream().filter(a -> aktørId.equals(a.getAktørId())).findFirst();
    }

    @Override
    public Optional<AktørArbeid> getAktørArbeidEtterStp(AktørId aktørId) {
        return getOpplysningerEtterSkjæringstidspunkt().map(InntektArbeidYtelseAggregat::getAktørArbeid).orElse(Collections.emptyList())
            .stream().filter(a -> aktørId.equals(a.getAktørId())).findFirst();
    }

    @Override
    public Collection<AktørYtelse> getAktørYtelseFørStp() {
        return getOpplysningerFørSkjæringstidspunkt().map(InntektArbeidYtelseAggregat::getAktørYtelse).orElse(Collections.emptyList());
    }

    @Override
    public Optional<AktørYtelse> getAktørYtelseFørStp(AktørId aktørId) {
        return getAktørYtelseFørStp().stream().filter(a -> aktørId.equals(a.getAktørId())).findFirst();
    }

    @Override
    public Optional<AktørYtelse> getAktørYtelseFørStpSaksBehFørReg(AktørId aktørId) {
        return getOpplysningerFørSkjæringstidspunkt().map(InntektArbeidYtelseAggregat::getAktørYtelse)
            .orElse(Collections.emptyList()).stream()
            .filter(a -> aktørId.equals(a.getAktørId()))
            .findFirst();
    }

    @Override
    public Optional<InntektsmeldingAggregat> getInntektsmeldinger() {
        return Optional.ofNullable(inntektsmeldinger);
    }

    void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldingAggregat) {
        this.inntektsmeldinger = (InntektsmeldingAggregatEntitet) inntektsmeldingAggregat;
    }

    @Override
    public Optional<AktørArbeid> getBekreftetAnnenOpptjening() {
        return getSaksbehandletVersjon()
            .map(InntektArbeidYtelseAggregat::getAktørArbeid)
            .flatMap(it -> it.stream().filter(aa -> aa.getAktørId().equals(behandling.getAktørId()))
                .findFirst());
    }

    @Override
    public Collection<Yrkesaktivitet> hentAlleYrkesaktiviteterFørStpFor(AktørId aktørId, boolean overstyrt) {
        if (overstyrt && getSaksbehandletVersjon().isPresent()) {
            InntektArbeidYtelseAggregat inntektArbeidYtelseAggregat = getSaksbehandletVersjon().get(); //$NON-NLS-1$ //NOSONAR
            return inntektArbeidYtelseAggregat.getAktørArbeid().stream()
                .filter(a -> a.getAktørId().equals(aktørId))
                .findAny().map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList());
        } else if (getOpplysningerFørSkjæringstidspunkt().isPresent()) {
            InntektArbeidYtelseAggregat inntektArbeidYtelseAggregat = getOpplysningerFørSkjæringstidspunkt().get(); //$NON-NLS-1$ //NOSONAR
            return inntektArbeidYtelseAggregat.getAktørArbeid().stream()
                .filter(a -> a.getAktørId().equals(aktørId))
                .findAny().map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<OppgittOpptjening> getOppgittOpptjening() {
        return Optional.ofNullable(oppgittOpptjening);
    }

    void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommer() {
        return informasjon == null ? Collections.emptyList() : informasjon.getOverstyringer()
            .stream().filter(ov -> ov.getHandling().equals(BRUK_UTEN_INNTEKTSMELDING))
            .map(ov -> new InntektsmeldingSomIkkeKommer(ov.getArbeidsgiver(), ov.getArbeidsforholdRef()))
            .collect(Collectors.toList());
    }

    @Override
    public List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommerFor(Virksomhet virksomhet) {
        return getInntektsmeldingerSomIkkeKommer()
            .stream()
            .filter(i -> i.getArbeidsgiver().getErVirksomhet()
                && i.getArbeidsgiver().getVirksomhet().equals(virksomhet)).collect(Collectors.toList());
    }

    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    void setAktivt(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    /* eksponeres ikke public for andre. */
    Long getId() {
        return id;
    }

    void setRegister(InntektArbeidYtelseAggregatEntitet registerFør) {
        this.register = registerFør;
    }

    public Optional<ArbeidsforholdInformasjon> getInformasjon() {
        return Optional.ofNullable(informasjon);
    }

    void setInformasjon(ArbeidsforholdInformasjonEntitet informasjon) {
        this.informasjon = informasjon;
    }

    void taHensynTilBetraktninger() {
        Optional.ofNullable(register).ifPresent(it -> it.taHensynTilBetraktninger(this.informasjon));
        Optional.ofNullable(saksbehandlet).ifPresent(it -> it.taHensynTilBetraktninger(this.informasjon));
        Optional.ofNullable(inntektsmeldinger).ifPresent(it -> it.taHensynTilBetraktninger(this.informasjon));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InntektArbeidYtelseGrunnlagEntitet that = (InntektArbeidYtelseGrunnlagEntitet) o;
        return aktiv == that.aktiv &&
            Objects.equals(register, that.register) &&
            Objects.equals(saksbehandlet, that.saksbehandlet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(register, saksbehandlet);
    }
}
