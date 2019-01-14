package no.nav.foreldrepenger.behandling.historikk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public class HistorikkinnslagDelDto {
    private Kodeliste begrunnelse;
    private String begrunnelseFritekst;
    private HistorikkinnslagHendelseDto hendelse;
    private HistorikkinnslagSoeknadsperiodeDto soeknadsperiode;
    private SkjermlenkeType skjermlenke;
    private Kodeliste aarsak;
    private HistorikkInnslagTemaDto tema;
    private HistorikkInnslagGjeldendeFraDto gjeldendeFra;
    private String resultat;
    private List<HistorikkinnslagEndretFeltDto> endredeFelter;
    private List<HistorikkinnslagTotrinnsVurderingDto> aksjonspunkter;

    public Kodeliste getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(Kodeliste begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public String getBegrunnelseFritekst() {
        return begrunnelseFritekst;
    }

    public void setBegrunnelseFritekst(String begrunnelseFritekst) {
        this.begrunnelseFritekst = begrunnelseFritekst;
    }

    public HistorikkinnslagHendelseDto getHendelse() {
        return hendelse;
    }

    public void setHendelse(HistorikkinnslagHendelseDto hendelse) {
        this.hendelse = hendelse;
    }

    public SkjermlenkeType getSkjermlenke() {
        return skjermlenke;
    }

    public void setSkjermlenke(SkjermlenkeType skjermlenke) {
        this.skjermlenke = skjermlenke;
    }

    public Kodeliste getAarsak() {
        return aarsak;
    }

    public void setAarsak(Kodeliste aarsak) {
        this.aarsak = aarsak;
    }

    public HistorikkInnslagTemaDto getTema() {
        return tema;
    }

    public void setTema(HistorikkInnslagTemaDto tema) {
        this.tema = tema;
    }

    public HistorikkInnslagGjeldendeFraDto getGjeldendeFra() {
        return gjeldendeFra;
    }

    public void setGjeldendeFra(String fra) {
        if (this.gjeldendeFra == null) {
            this.gjeldendeFra = new HistorikkInnslagGjeldendeFraDto(fra);
        } else {
            this.gjeldendeFra.setFra(fra);
        }
    }

    public void setGjeldendeFra(String fra, String navn, String verdi) {
        if (this.gjeldendeFra == null) {
            this.gjeldendeFra = new HistorikkInnslagGjeldendeFraDto(fra, navn, verdi);
        } else {
            this.gjeldendeFra.setFra(fra);
            this.gjeldendeFra.setNavn(navn);
            this.gjeldendeFra.setVerdi(verdi);
        }
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public List<HistorikkinnslagEndretFeltDto> getEndredeFelter() {
        return endredeFelter;
    }

    public void setEndredeFelter(List<HistorikkinnslagEndretFeltDto> endredeFelter) {
        this.endredeFelter = endredeFelter;
    }

    public HistorikkinnslagSoeknadsperiodeDto getSoeknadsperiode() {
        return soeknadsperiode;
    }

    public void setSoeknadsperiode(HistorikkinnslagSoeknadsperiodeDto soeknadsperiode) {
        this.soeknadsperiode = soeknadsperiode;
    }

    public List<HistorikkinnslagTotrinnsVurderingDto> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public void setAksjonspunkter(List<HistorikkinnslagTotrinnsVurderingDto> aksjonspunkter) {
        this.aksjonspunkter = aksjonspunkter;
    }

    static List<HistorikkinnslagDelDto> mapFra(List<HistorikkinnslagDel> historikkinnslagDelList, KodeverkRepository kodeverkRepository, AksjonspunktRepository aksjonspunktRepository) {
        List<HistorikkinnslagDelDto> historikkinnslagDelDtoList = new ArrayList<>();
        for (HistorikkinnslagDel historikkinnslagDel : historikkinnslagDelList) {
            historikkinnslagDelDtoList.add(mapFra(historikkinnslagDel, kodeverkRepository, aksjonspunktRepository));
        }
        return historikkinnslagDelDtoList;
    }

    private static HistorikkinnslagDelDto mapFra(HistorikkinnslagDel historikkinnslagDel, KodeverkRepository kodeverkRepository, AksjonspunktRepository aksjonspunktRepository) {
        HistorikkinnslagDelDto dto = new HistorikkinnslagDelDto();
        historikkinnslagDel.getBegrunnelseFelt().ifPresent(begrunnelse -> dto.setBegrunnelse(finnÅrsakKodeListe(kodeverkRepository, begrunnelse).orElse(null)));
        if (dto.getBegrunnelse() == null) {
            historikkinnslagDel.getBegrunnelse().ifPresent(dto::setBegrunnelseFritekst);
        }
        historikkinnslagDel.getAarsakFelt().ifPresent(aarsak -> dto.setAarsak(finnÅrsakKodeListe(kodeverkRepository, aarsak).orElse(null)));
        historikkinnslagDel.getTema().ifPresent(felt -> dto.setTema(HistorikkInnslagTemaDto.mapFra(felt, kodeverkRepository)));
        historikkinnslagDel.getGjeldendeFraFelt().ifPresent(felt -> {
            if (felt.getNavn() != null && felt.getNavnVerdi() != null && felt.getTilVerdi() != null) {
                dto.setGjeldendeFra(felt.getTilVerdi(), felt.getNavn(), felt.getNavnVerdi());
            } else if (felt.getTilVerdi() != null) {
                dto.setGjeldendeFra(felt.getTilVerdi());
            }
        });
        historikkinnslagDel.getResultat().ifPresent(dto::setResultat);
        historikkinnslagDel.getHendelse().ifPresent(hendelse -> {
            HistorikkinnslagHendelseDto hendelseDto = HistorikkinnslagHendelseDto.mapFra(hendelse, kodeverkRepository);
            dto.setHendelse(hendelseDto);
        });
        historikkinnslagDel.getSkjermlenke().ifPresent(skjermlenke -> {
            SkjermlenkeType type = kodeverkRepository.finn(SkjermlenkeType.class, skjermlenke);
            dto.setSkjermlenke(type);
        });
        if (!historikkinnslagDel.getTotrinnsvurderinger(aksjonspunktRepository).isEmpty()) {
            dto.setAksjonspunkter(HistorikkinnslagTotrinnsVurderingDto.mapFra(historikkinnslagDel.getTotrinnsvurderinger(aksjonspunktRepository)));
        }
        if (!historikkinnslagDel.getEndredeFelt().isEmpty()) {
            dto.setEndredeFelter(HistorikkinnslagEndretFeltDto.mapFra(historikkinnslagDel.getEndredeFelt(), kodeverkRepository));
        }
        historikkinnslagDel.getAvklartSoeknadsperiode().ifPresent(soeknadsperiode -> {
            HistorikkinnslagSoeknadsperiodeDto soeknadsperiodeDto = HistorikkinnslagSoeknadsperiodeDto.mapFra(soeknadsperiode, kodeverkRepository);
            dto.setSoeknadsperiode(soeknadsperiodeDto);
        });
        return dto;
    }

    private static Optional<Kodeliste> finnÅrsakKodeListe(KodeverkRepository kodeverkRepository, HistorikkinnslagFelt aarsak) {
        String aarsakVerdi = aarsak.getTilVerdi();
        if (Objects.equals("-", aarsakVerdi)) {
            return Optional.empty();
        }
        Optional<Class<Kodeliste>> aarsakClass = kodeverkRepository.finnKodelisteForKodeverk(aarsak.getKlTilVerdi());
        return aarsakClass.map(aClass -> kodeverkRepository.finn(aClass, aarsakVerdi));
    }

}
