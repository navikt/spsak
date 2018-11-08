import React from 'react';
import { FormattedHTMLMessage, FormattedMessage } from 'react-intl';
import { Column, Row } from 'nav-frontend-grid';
import { Undertekst, Normaltekst } from 'nav-frontend-typografi';
import stonadskontoType from 'kodeverk/stonadskontoType';
import PropTypes from 'prop-types';
import { stonadskontoerPropType } from 'behandling/proptypes/stonadskontoPropType';

import styles from './timeLineTab.less';

const findKorrektLabelForKvote = (stonadtype) => {
  switch (stonadtype) {
    case stonadskontoType.FEDREKVOTE:
      return 'TimeLineTab.Stonadinfo.Fedrekvote';
    case stonadskontoType.MODREKVOTE:
      return 'TimeLineTab.Stonadinfo.Modrekvote';
    case stonadskontoType.FELLESPERIODE:
      return 'TimeLineTab.Stonadinfo.Fellesperiode';
    case stonadskontoType.FORELDREPENGER_FOR_FODSEL:
      return 'TimeLineTab.Stonadinfo.ForeldrepengerFF';
    case stonadskontoType.FLERBARNSDAGER:
      return 'TimeLineTab.Stonadinfo.Flerbarnsdager';
    case stonadskontoType.FORELDREPENGER:
      return 'TimeLineTab.Stonadinfo.ForeldrePenger';
    default:
      return 'TimeLineTab.Stonadinfo.Empty';
  }
};

const findAntallUkerOgDager = (kontoinfo) => {
  const modifier = kontoinfo.saldo < 0 ? -1 : 1;
  const justertSaldo = kontoinfo.saldo * modifier;
  return {
    uker: (Math.floor(justertSaldo / 5)) * modifier,
    dager: (justertSaldo % 5) * modifier,
  };
};

const TimeLineTab = ({
  stonadskonto,
  onClickCallback,
  aktiv,
}) => {
  const fordelteDager = findAntallUkerOgDager(stonadskonto.kontoinfo);
  return (
    <div className={styles.tabs}>
      <li role="presentation" className={aktiv ? styles.aktiv : styles.inaktiv}>
        <button
          role="tab"
          className={styles.tabInner}
          type="button"
          onClick={onClickCallback}
          aria-selected={aktiv}
        >
          <Column>
            <Row>
              <Undertekst>
                <FormattedMessage
                  id={findKorrektLabelForKvote(stonadskonto.kontonavn)}
                />
              </Undertekst>
            </Row>
            <Row>
              <Normaltekst>
                <FormattedHTMLMessage
                  id="TimeLineTab.Stonadinfo.UkerDager"
                  values={{
                    ukerVerdi: fordelteDager.uker,
                    dagerVerdi: fordelteDager.dager,
                  }}
                />
              </Normaltekst>
            </Row>
          </Column>
        </button>
      </li>
    </div>);
};

TimeLineTab.propTypes = {
  stonadskonto: stonadskontoerPropType.isRequired,
  onClickCallback: PropTypes.func.isRequired,
  aktiv: PropTypes.bool,
};

TimeLineTab.defaultProps = {
  aktiv: false,
};


export default TimeLineTab;
