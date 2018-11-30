import vut from 'kodeverk/vilkarUtfallType';
import { notNull } from 'utils/objectUtils';
import { isAksjonspunktOpen } from 'kodeverk/aksjonspunktStatus';

class BehandlingspunktProperties {
  constructor(code, titleCode, isVisible, status, bpVilkar, bpAksjonspunkter) {
    this.code = code;
    this.titleCode = titleCode;
    this.isVisible = isVisible;
    this.status = status;
    this.vilkarene = bpVilkar;
    this.apCodes = bpAksjonspunkter ? bpAksjonspunkter.map(ap => ap.definisjon.kode) : [];
  }

  static get Builder() {
    class Builder {
      /**
       * @param {string} bpCode - Obligatorisk streng som er en unik identifikator for behandlingspunktet.
       * @param {string} titleCode - Obligatorisk streng som brukes til å hente ut behandlingspunktnavnet.
       */
      constructor(bpCode, titleCode) {
        this.bpCode = notNull(bpCode);
        // @TODO Make textCode traceable...
        this.titleCode = `Behandlingspunkt.${notNull(titleCode)}`;
      }

      /**
       * Brukes når behandlingspunktet skal kobles mot aksjonspunkter. Et behandlingspunkt kan håndtere samtidige
       * aksjonspunkter. Kodene brukes i @see build til å utlede når behandlingspunkt skal vises og hvilke status det har.
       * @param {string[]} apCodes - Aksjonspunktkoder.
       */
      withAksjonspunktCodes(...apCodes) {
        this.apCodes = apCodes;
        return this;
      }

      /**
       * Brukes når behandlingspunktet skal kobles mot vilkar. Et behandlingspunkt kan håndtere samtidige
       * vilkar. Vilkåret brukes i @see build til å utlede når behandlingspunkt skal vises og hvilke status det har.
       * @param {string[]} vilkarTypes - Vilkårtyper.
       */
      withVilkarTypes(...vilkarTypes) {
        this.vilkarTypes = vilkarTypes;
        return this;
      }

      /**
       * Denne angir at oppgitt vilkår ikke er obligatorisk for å vise behandlingspunktet. Finnes ikke vilkåret vil aksjonspunktene
       * avgjøre om behandlingspunkt skal vises.
       */
      withVilkarIsOptional() {
        this.vilkarIsOptional = true;
        return this;
      }

      /**
       * Vanligvis vil vilkår og aksjonspunkter avgjøre når behandlingspunktet skal vises. Bruk denne metoden når
       * det er nødvendig å overstyre dette.
       */
      withVisibilityWhen(...visibilityFunctions) {
        this.visibilityFunctions = visibilityFunctions;
        return this;
      }

      /**
       * Kombinerer default måte å vise behandlingspunkter på med custom funksjon(er). Når funksjon returnerer false vises ikke
       * behandlingspunktet. Returneres true blir registrerte vilkår og aksjonspunkter nyttet for å avgjøre om det skal vises.
       */
      withDefaultVisibilityWhenCustomFnReturnsTrue(...visibilityFunctions) {
        this.visibilityFunctions = visibilityFunctions;
        this.defaultVisibilityFallback = true;
        return this;
      }

      /**
       * Vanligvis vil vilkår og aksjonspunkter avgjøre statusen til behandlingspunktet. Bruk denne metoden når
       * det er nødvendig å overstyre dette.
       */
      withStatus(statusFunction) {
        this.statusFunction = statusFunction;
        return this;
      }

      // Internal use only
      $$findStatus(bpVilkar, bpAksjonspunkter) { // eslint-disable-line class-methods-use-this
        if (bpVilkar.length > 0) {
          const vilkarStatusCodes = bpVilkar.map(v => v.vilkarStatus.kode);
          if (vilkarStatusCodes.some(vsc => vsc === vut.IKKE_VURDERT)) {
            return vut.IKKE_VURDERT;
          }
          return vilkarStatusCodes.every(vsc => vsc === vut.OPPFYLT) ? vut.OPPFYLT : vut.IKKE_OPPFYLT;
        }

        if (bpAksjonspunkter.length > 0) {
          return bpAksjonspunkter.some(ap => isAksjonspunktOpen(ap.status.kode)) ? vut.IKKE_VURDERT : vut.OPPFYLT;
        }
        return vut.IKKE_VURDERT;
      }

      // Internal use only
      $$isVisible(bpVilkar, bpAksjonspunkter) {
        if (this.vilkarIsOptional) {
          return bpVilkar.length > 0 || bpAksjonspunkter.length > 0;
        }
        const hasVilkar = this.vilkarTypes && bpVilkar.length > 0;
        const hasNoVilkarDefinitionButAksjonspunkter = !this.vilkarTypes && bpAksjonspunkter.length > 0;
        return hasVilkar || hasNoVilkarDefinitionButAksjonspunkter;
      }

      build(builderData, bpLength) {
        const bpVilkar = builderData.vilkar.filter(vilkar => this.vilkarTypes && this.vilkarTypes.includes(vilkar.vilkarType.kode));
        const bpAksjonspunkter = builderData.aksjonspunkter.filter(ap => this.apCodes && this.apCodes.includes(ap.definisjon.kode));

        let isVisible = false;
        if (this.visibilityFunctions) {
          isVisible = this.visibilityFunctions.every(f => f(builderData, bpLength));
          if (this.defaultVisibilityFallback) {
            isVisible = isVisible ? this.$$isVisible(bpVilkar, bpAksjonspunkter) : false;
          }
        } else {
          isVisible = this.$$isVisible(bpVilkar, bpAksjonspunkter);
        }
        if (!isVisible) {
          return new BehandlingspunktProperties(this.bpCode, this.titleCode, isVisible);
        }

        const status = this.statusFunction ? this.statusFunction(builderData, bpAksjonspunkter) : this.$$findStatus(bpVilkar, bpAksjonspunkter);
        return new BehandlingspunktProperties(this.bpCode, this.titleCode, isVisible, status, bpVilkar, bpAksjonspunkter);
      }
    }
    return Builder;
  }
}

export default BehandlingspunktProperties;
