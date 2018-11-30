import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FormattedHTMLMessage, FormattedMessage } from 'react-intl';
import { Column, Row } from 'nav-frontend-grid';
import TableColumn from 'sharedComponents/TableColumn';
import TableRow from 'sharedComponents/TableRow';
import Table from 'sharedComponents/Table';
import { Element, Normaltekst } from 'nav-frontend-typografi';
import { stonadskontoerPropType } from 'behandling/proptypes/stonadskontoPropType';
import uttakArbeidTypeKodeverk from 'kodeverk/uttakArbeidType';
import uttakArbeidTypeTekstCodes from 'kodeverk/uttakArbeidTypeCodes';
import stonadskontoType from 'kodeverk/stonadskontoType';
import { DDMMYYYY_DATE_FORMAT } from 'utils/formats';
import moment from 'moment';

import TimeLineTab from './TimeLineTab';
import styles from './timeLineInfo.less';

/**
 * TimeLineInfo
 *
 * Presentationskomponent. Viser de ulike perioderne og kvarvarende uker/dagar for soknad
 */

const headerTextCodes = [
  'TimeLineInfo.Aktivitet',
  'TimeLineInfo.Disponibelt',
];

const findTilgjengeligeUker = (stonadskontoer) => {
  let sumDager = 0;
  Object.keys(stonadskontoer).forEach((key) => {
    if (key !== stonadskontoType.FLERBARNSDAGER) {
      sumDager += stonadskontoer[key].maxDager ? stonadskontoer[key].maxDager : 0;
    }
  });
  return Math.floor(sumDager / 5);
};

const findAntallUkerOgDager = (saldo) => {
  const modifier = saldo < 0 ? -1 : 1;
  const justertSaldo = saldo * modifier;
  return {
    uker: (Math.floor(justertSaldo / 5)) * modifier,
    dager: (justertSaldo % 5) * modifier,
  };
};

const createTextStrings = (arbforhold) => {
  const {
    arbeidsforholdNavn, arbeidsforholdOrgnr, arbeidsforholdId, uttakArbeidType,
  } = arbforhold;
  let arbeidsforhold = '';

  if (uttakArbeidType && uttakArbeidType.kode !== uttakArbeidTypeKodeverk.ORDINÆRT_ARBEID) {
    arbeidsforhold = <FormattedMessage id={uttakArbeidTypeTekstCodes[uttakArbeidType.kode]} />;
  } else {
    arbeidsforhold = arbeidsforholdNavn ? `${arbeidsforholdNavn}` : `${uttakArbeidType.navn}`;
    arbeidsforhold = arbeidsforholdOrgnr ? `${arbeidsforhold} (${arbeidsforholdOrgnr})` : arbeidsforhold;
    arbeidsforhold = arbeidsforholdId ? `${arbeidsforhold}...${arbeidsforholdId.substr(-4)}` : arbeidsforhold;
  }
  return arbeidsforhold;
};

class TimeLineInfo extends Component {
  constructor() {
    super();

    this.state = {
      aktiv: undefined,
      visKonto: undefined,
    };
    this.handleChange = this.handleChange.bind(this);
    this.shouldComponentUpdate = this.shouldComponentUpdate.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    const { aktiv } = this.state;
    if (nextState.aktiv !== aktiv) {
      return true;
    }
    return false;
  }


  handleChange(konto, index) {
    const { aktiv } = this.state;
    if (aktiv === index) {
      this.setState({
        aktiv: undefined,
        visKonto: undefined,
      });
    } else {
      this.setState({
        aktiv: index,
        visKonto: konto,
      });
    }
  }

  render() {
    const {
      stonadskonto,
      maksDatoUttak,
    } = this.props;
    const {
      aktiv,
      visKonto,
    } = this.state;

    const gjelderFodsel = true;

    const sortedPerioder = [
      stonadskontoType.FORELDREPENGER_FOR_FODSEL,
      stonadskontoType.FELLESPERIODE,
      stonadskontoType.MODREKVOTE,
      stonadskontoType.FEDREKVOTE,
      stonadskontoType.FORELDREPENGER,
      stonadskontoType.FLERBARNSDAGER];

    const sortAsArray = () => {
      const stonadArray = Object.keys(stonadskonto).map(key => ({
        kontonavn: key,
        kontoinfo: stonadskonto[key],
      }));
      const ordering = {};
      sortedPerioder.forEach((s, index) => {
        ordering[s] = index;
      });
      stonadArray.sort((a, b) => (ordering[a.kontonavn] - ordering[b.kontonavn]));
      return stonadArray;
    };

    const createKey = arbeidsforhold => arbeidsforhold.aktivitetIdentifikator.uttakArbeidType.kode
      + arbeidsforhold.aktivitetIdentifikator.arbeidsforholdNavn
      + arbeidsforhold.aktivitetIdentifikator.arbeidsforholdId
      + arbeidsforhold.aktivitetIdentifikator.arbeidsforholdOrgnr
      + arbeidsforhold.fordelteDager;

    return (
      <div>
        {gjelderFodsel // Denne vi lager i første omgang av iterasjonen
        && (
          <Column xs="12">
            <div className={styles.remainingUttak}>
              <Row>
                <Column xs="5">
                  <Element>
                    <FormattedMessage id="TimeLineInfo.Stonadinfo.DisponibleStonadsdager" />
                  </Element>
                </Column>
                <Column xs="4">
                  <Normaltekst>
                    <FormattedHTMLMessage
                      id="TimeLineInfo.Stonadinfo.Total"
                      values={{ ukerVerdi: findTilgjengeligeUker(stonadskonto) }}
                    />
                  </Normaltekst>
                </Column>
                {maksDatoUttak
                && (
                  <Column xs="3">
                    <Normaltekst>
                      <FormattedHTMLMessage
                        id="TimeLineInfo.Stonadinfo.MaksDato"
                        values={{ dato: moment(maksDatoUttak).format(DDMMYYYY_DATE_FORMAT) }}
                      />
                    </Normaltekst>
                  </Column>
                )
                }
              </Row>
              <Row>
                <div className={styles.tabs}>
                  <ul role="tablist">
                    {sortAsArray().map((konto, index) => (
                      <TimeLineTab key={konto.kontonavn} aktiv={index === aktiv} stonadskonto={konto} onClickCallback={() => this.handleChange(konto, index)} />
                    ))
                    }
                  </ul>
                </div>
              </Row>
              <Row>
                {visKonto
                && (
                  <div className={styles.visKonto}>
                    <Table headerTextCodes={headerTextCodes}>
                      {visKonto.kontoinfo.aktivitetSaldoDtoList.map(arbforhold => (
                        <TableRow key={createKey(arbforhold)}>
                          <TableColumn>
                            <Normaltekst>{createTextStrings(arbforhold.aktivitetIdentifikator)}</Normaltekst>
                          </TableColumn>
                          <TableColumn>
                            <Normaltekst>
                              {arbforhold.saldo
                              && (
                              <FormattedHTMLMessage
                                id="TimeLineInfo.Stonadinfo.UkerDager"
                                values={{
                                  ukerVerdi: findAntallUkerOgDager(
                                    arbforhold.saldo,
                                  ).uker,
                                  dagerVerdi: findAntallUkerOgDager(
                                    arbforhold.saldo,
                                  ).dager,
                                }}
                              />
                              )
                              }
                            </Normaltekst>
                          </TableColumn>
                        </TableRow>
                      ))
                      }
                    </Table>
                  </div>
                )
                }
              </Row>
            </div>
          </Column>
        )
        }
      </div>
    );
  }
}

TimeLineInfo.propTypes = {
  stonadskonto: stonadskontoerPropType,
  maksDatoUttak: PropTypes.string,
};

TimeLineInfo.defaultProps = {
  stonadskonto: undefined,
  maksDatoUttak: '',
};

export default TimeLineInfo;
