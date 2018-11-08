package no.nav.foreldrepenger.web.app.tjenester.behandling.innsyn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.VedtaksdokumentasjonDto;

public class InnsynsbehandlingDto {

	private LocalDate innsynMottattDato;
	private InnsynResultatType innsynResultatType;
	private List<VedtaksdokumentasjonDto> vedtaksdokumentasjon  = new ArrayList<>();
	private List<InnsynDokumentDto> dokumenter = new ArrayList<>();

	public LocalDate getInnsynMottattDato() {
		return innsynMottattDato;
	}

	public InnsynResultatType getInnsynResultatType() {
		return innsynResultatType;
	}

	public void setInnsynMottattDato(LocalDate innsynMottattDato) {
		this.innsynMottattDato = innsynMottattDato;
	}

	public void setInnsynResultatType(InnsynResultatType innsynResultatType) {
		this.innsynResultatType = innsynResultatType;
	}

	public void setVedtaksdokumentasjon(List<VedtaksdokumentasjonDto> vedtaksdokumentasjon) {
		this.vedtaksdokumentasjon = vedtaksdokumentasjon;
	}

	public List<VedtaksdokumentasjonDto> getVedtaksdokumentasjon() {
		return vedtaksdokumentasjon;
	}

	public void setDokumenter(List<InnsynDokumentDto> dokumenter) {
	    this.dokumenter = dokumenter;
    }

    public List<InnsynDokumentDto> getDokumenter() {
        return dokumenter;
    }
}
