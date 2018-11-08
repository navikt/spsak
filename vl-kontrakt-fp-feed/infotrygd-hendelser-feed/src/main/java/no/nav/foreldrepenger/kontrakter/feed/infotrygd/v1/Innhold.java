package no.nav.foreldrepenger.kontrakter.feed.infotrygd.v1;

import java.time.LocalDate;

public abstract class Innhold {
    private String typeYtelse;
    public String getTypeYtelse() {
		return typeYtelse;
	}
	public void setTypeYtelse(String typeYtelse) {
		this.typeYtelse = typeYtelse;
	}
	public LocalDate getFom() {
		return fom;
	}
	public void setFom(LocalDate fom) {
		this.fom = fom;
	}
	public String getAktoerId() {
		return aktoerId;
	}
	public void setAktoerId(String aktoerId) {
		this.aktoerId = aktoerId;
	}
	public String getIdentDato() {
		return identDato;
	}
	public void setIdentDato(String identDato) {
		this.identDato = identDato;
	}
	private LocalDate fom;
    private String aktoerId;
    private String identDato;
}
