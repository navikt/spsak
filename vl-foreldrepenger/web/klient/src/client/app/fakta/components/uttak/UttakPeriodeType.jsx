import React from 'react';
import PropTypes from 'prop-types';
import { Element, Undertekst, Normaltekst } from 'nav-frontend-typografi';
import { FormattedMessage } from 'react-intl';
import { dateFormat, calcDaysAndWeeks } from 'utils/dateUtils';
import utsettelseArsakCodes, { utsettelseArsakTexts } from 'kodeverk/utsettelseArsakCodes';
import overforingArsakCodes, { overforingArsakTexts } from 'kodeverk/overforingArsakCodes';
import Image from 'sharedComponents/Image';
import { ISO_DATE_FORMAT } from 'utils/formats';
import editPeriodeIcon from 'images/endre.svg';
import editPeriodeDisabledIcon from 'images/endre_disablet.svg';
import removePeriod from 'images/remove.svg';
import removePeriodDisabled from 'images/remove_disabled.svg';
import styles from './uttakPeriodeType.less';

const formatProsent = prosent => `${prosent}%`;

const getUttakTypeTitle = (utsettelseArsak, overforingArsak, arbeidstidprosent) => {
  if (overforingArsak.kode !== overforingArsakCodes.UDEFINERT) {
    return (
      <FormattedMessage
        id="UttakInfoPanel.OverføringMedÅrsak"
        values={{ årsak: overforingArsakTexts[overforingArsak.kode] }}
      />
    );
  }

  if (utsettelseArsak.kode !== utsettelseArsakCodes.UDEFINERT) {
    return (
      <FormattedMessage
        id="UttakInfoPanel.UtsettelseMedÅrsak"
        values={{ årsak: utsettelseArsakTexts[utsettelseArsak.kode] }}
      />
    );
  }

  if (arbeidstidprosent !== null && arbeidstidprosent !== undefined) {
    return <FormattedMessage id="UttakInfoPanel.UttakMedGradering" />;
  }

  return <FormattedMessage id="UttakInfoPanel.Uttak" />;
};

const UttakPeriodeType = ({ // NOSONAR
  tilDato,
  fraDato,
  openSlettPeriodeModalCallback,
  id,
  editPeriode,
  isAnyFormOpen,
  isNyPeriodeFormOpen,
  readOnly,
  arbeidstidprosent,
  utsettelseArsak,
  overforingArsak,
  uttakPeriodeType,
  isFromSøknad,
  virksomhetNavn,
  orgnr,
  erArbeidstaker,
  samtidigUttak,
  samtidigUttaksprosent,
  flerbarnsdager,
}) => {
  const isAnyFormOrNyPeriodeOpen = isAnyFormOpen() || isNyPeriodeFormOpen;
  const numberOfDaysAndWeeks = calcDaysAndWeeks(fraDato, tilDato, ISO_DATE_FORMAT);
  const isGradering = arbeidstidprosent !== null && arbeidstidprosent !== undefined;
  return (
    <div className={styles.periodeType}>
      <div className={styles.headerWrapper}>
        <div>
          {isFromSøknad && <Undertekst><FormattedMessage id="UttakInfoPanel.FraSøknad" /></Undertekst>}
          <Element>{getUttakTypeTitle(utsettelseArsak, overforingArsak, arbeidstidprosent)}</Element>
          <Normaltekst>{uttakPeriodeType.navn}</Normaltekst>
        </div>
        {!readOnly
          && (
          <div className={styles.iconContainer}>
            <Image
              className={styles.editIcon}
              src={isAnyFormOrNyPeriodeOpen ? editPeriodeDisabledIcon : editPeriodeIcon}
              onClick={isAnyFormOrNyPeriodeOpen ? () => undefined : () => editPeriode(id)}
              altCode="UttakInfoPanel.EndrePerioden"
            />
            <Image
              className={styles.removeIcon}
              src={isAnyFormOrNyPeriodeOpen ? removePeriodDisabled : removePeriod}
              onClick={isAnyFormOrNyPeriodeOpen ? () => undefined : () => openSlettPeriodeModalCallback(id)}
              altCode="UttakInfoPanel.SlettPerioden"
            />
          </div>
          )
        }
      </div>
      <div className={styles.textWrapper}>
        <Element>{`${dateFormat(fraDato)} - ${dateFormat(tilDato)}`}</Element>
        <Undertekst>
          <FormattedMessage
            id={numberOfDaysAndWeeks.id}
            values={{
              weeks: numberOfDaysAndWeeks.weeks,
              days: numberOfDaysAndWeeks.days,
            }}
          />
        </Undertekst>
      </div>

      {samtidigUttak && (
        <div className={styles.textWrapper}>
          <Undertekst><FormattedMessage id="UttakInfoPanel.SamtidigUttak" /></Undertekst>
          {samtidigUttaksprosent && (
            <Normaltekst>{formatProsent(samtidigUttaksprosent)}</Normaltekst>
          )}
        </div>
      )}

      {flerbarnsdager && (
        <div className={styles.textWrapper}>
          <Undertekst><FormattedMessage id="UttakInfoPanel.Flerbarnsdager" /></Undertekst>
        </div>
      )}

      {(arbeidstidprosent === 0 || arbeidstidprosent)
        && (
        <div className={styles.textWrapper}>
          <Undertekst><FormattedMessage id="UttakInfoPanel.AndelIArbeid" /></Undertekst>
          <Normaltekst>{formatProsent(arbeidstidprosent)}</Normaltekst>
        </div>
        )
      }
      {isGradering && !erArbeidstaker
      && (
        <div className={styles.textWrapper}>
          <Element><FormattedMessage id="UttakInfoPanel.FrilansSelvstendignæringsdrivende" /></Element>
        </div>
      )
      }
      {isGradering && virksomhetNavn && orgnr
      && (
      <div className={styles.textWrapper}>
        <Element>{`${virksomhetNavn} ${orgnr}`}</Element>
      </div>
      )
      }
    </div>
  );
};

UttakPeriodeType.propTypes = {
  fraDato: PropTypes.string.isRequired,
  tilDato: PropTypes.string.isRequired,
  uttakPeriodeType: PropTypes.shape().isRequired,
  openSlettPeriodeModalCallback: PropTypes.func.isRequired,
  id: PropTypes.string.isRequired,
  editPeriode: PropTypes.func.isRequired,
  isAnyFormOpen: PropTypes.func.isRequired,
  readOnly: PropTypes.bool.isRequired,
  arbeidstidprosent: PropTypes.number,
  virksomhetNavn: PropTypes.string,
  orgnr: PropTypes.string,
  isNyPeriodeFormOpen: PropTypes.bool.isRequired,
  utsettelseArsak: PropTypes.shape().isRequired,
  overforingArsak: PropTypes.shape().isRequired,
  isFromSøknad: PropTypes.bool.isRequired,
  flerbarnsdager: PropTypes.bool.isRequired,
  samtidigUttak: PropTypes.bool.isRequired,
  samtidigUttaksprosent: PropTypes.string,
  erArbeidstaker: PropTypes.bool,
};

UttakPeriodeType.defaultProps = {
  arbeidstidprosent: null,
  samtidigUttaksprosent: null,
  virksomhetNavn: null,
  orgnr: null,
  erArbeidstaker: false,
};

export default UttakPeriodeType;
