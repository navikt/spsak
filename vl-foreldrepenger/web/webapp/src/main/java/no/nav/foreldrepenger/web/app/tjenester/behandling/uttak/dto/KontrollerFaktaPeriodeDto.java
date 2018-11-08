package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType.PERIODE_OK;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType.PERIODE_OK_ENDRET;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.FordelingPeriodeKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public class KontrollerFaktaPeriodeDto {

    @NotNull
    private LocalDate tom;

    @NotNull
    private LocalDate fom;

    @NotNull
    @ValidKodeverk
    private UttakPeriodeType uttakPeriodeType;

    @ValidKodeverk
    private UtsettelseÅrsak utsettelseÅrsak = UtsettelseÅrsak.UDEFINERT;

    @ValidKodeverk
    private OverføringÅrsak overføringÅrsak = OverføringÅrsak.UDEFINERT;

    @ValidKodeverk
    private UttakPeriodeVurderingType resultat = UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT;

    @Valid
    @Size(max = 10)
    private List<UttakDokumentasjonDto> dokumentertePerioder;

    @Min(0)
    @Max(100)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal arbeidstidsprosent;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    private boolean bekreftet;

    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @Size(max = 200)
    private String orgnr;

    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @Size(max = 200)
    private String virksomhetNavn;

    private boolean erArbeidstaker;
    private boolean samtidigUttak;

    @Min(0)
    @Max(200)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal samtidigUttaksprosent;

    private boolean flerbarnsdager;

    @ValidKodeverk
    private MorsAktivitet morsAktivitet = MorsAktivitet.UDEFINERT;

    @ValidKodeverk
    private FordelingPeriodeKilde periodeKilde = FordelingPeriodeKilde.SØKNAD;


    KontrollerFaktaPeriodeDto() {//NOSONAR
        //for jackson
    }

    public KontrollerFaktaPeriodeDto(KontrollerFaktaPeriode periode) {
        this.fom = periode.getFom();
        this.tom = periode.getTom();
        this.uttakPeriodeType = periode.getUttakPeriodeType();
        this.arbeidstidsprosent = periode.getArbeidstidsprosent();
        this.begrunnelse = periode.getBegrunnelse();
        this.bekreftet = periode.erBekreftet();
        this.resultat = periode.getVurdering();
        this.orgnr = periode.getOrgnr();
        this.virksomhetNavn = periode.getVirksomhetNavn();
        this.erArbeidstaker = periode.erArbeidstaker();
        this.samtidigUttak = periode.getSamtidigUttak();
        this.samtidigUttaksprosent = periode.getSamtidigUttaksprosent();
        this.morsAktivitet = periode.getMorsAktivitet();
        this.flerbarnsdager = periode.isFlerbarnsdager();
        this.periodeKilde = periode.getPeriodeKilde();

        if (periode.getÅrsak() instanceof UtsettelseÅrsak) {
            this.utsettelseÅrsak = (UtsettelseÅrsak) periode.getÅrsak(); //NOSONAR
        } else if (periode.getÅrsak() instanceof OverføringÅrsak) {
            this.overføringÅrsak = (OverføringÅrsak) periode.getÅrsak(); //NOSONAR
        }

        dokumentertePerioder = periode.getDokumentertePerioder().stream()
            .map(UttakDokumentasjonDto::new)
            .collect(Collectors.toList());
    }

    public LocalDate getTom() {
        return tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public UttakPeriodeType getUttakPeriodeType() {
        return uttakPeriodeType;
    }

    public Årsak getUtsettelseÅrsak() {
        return utsettelseÅrsak;
    }

    public OverføringÅrsak getOverføringÅrsak() {
        return overføringÅrsak;
    }

    public BigDecimal getArbeidstidsprosent() {
        return arbeidstidsprosent;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public boolean isBekreftet() {
        return bekreftet;
    }

    public List<UttakDokumentasjonDto> getDokumentertePerioder() {
        return dokumentertePerioder == null ? Collections.emptyList() : dokumentertePerioder;
    }

    public UttakPeriodeVurderingType getResultat() {
        return resultat;
    }

    public boolean erAvklartDokumentert() {
        return PERIODE_OK.equals(resultat) || PERIODE_OK_ENDRET.equals(resultat);
    }

    public String getOrgnr() {
        return orgnr;
    }

    public String getVirksomhetNavn() {
        return virksomhetNavn;
    }

    public boolean getErArbeidstaker() {
        return erArbeidstaker;
    }

    public boolean getSamtidigUttak() {
        return samtidigUttak;
    }

    public BigDecimal getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    public MorsAktivitet getMorsAktivitet() {
        return morsAktivitet;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public FordelingPeriodeKilde getPeriodeKilde() {
        return periodeKilde;
    }

    @JsonIgnore
    public Optional<Årsak> getÅrsak() {
        if (!Objects.equals(UtsettelseÅrsak.UDEFINERT, utsettelseÅrsak)) {
            return Optional.of(utsettelseÅrsak);
        }
        if (!Objects.equals(OverføringÅrsak.UDEFINERT, overføringÅrsak)) {
            return Optional.of(overføringÅrsak);
        }
        return Optional.empty();
    }
}
