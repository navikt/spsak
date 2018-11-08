import React from 'react';
import PropTypes from 'prop-types';
import { FormattedDate, FormattedMessage } from 'react-intl';

/**
 * PeriodLabel
 *
 * Presentasjonskomponent. Formaterer til og fra dato til en periode på formatet dd.mm.yyyy - dd.mm.yyyy.
 *
 * Eksempel:
 * ```html
 * <PeriodLabel dateStringFom="2017-08-25" dateStringTom="2017-08-31" />
 * ```
 */
const PeriodLabel = ({
  dateStringFom,
  dateStringTom,
  showTodayString,
}) => (
  <span>
    <FormattedDate day="2-digit" month="2-digit" year="numeric" value={new Date(dateStringFom)} />
    {' - '}
    {dateStringTom
      && <FormattedDate day="2-digit" month="2-digit" year="numeric" value={new Date(dateStringTom)} />
    }
    {showTodayString && !dateStringTom
      && (
      <span>
        <FormattedMessage id="PeriodLabel.DateToday" />
      </span>
      )
    }
  </span>
);

PeriodLabel.propTypes = {
  dateStringFom: PropTypes.string.isRequired,
  dateStringTom: PropTypes.string,
  showTodayString: PropTypes.bool,
};

PeriodLabel.defaultProps = {
  dateStringTom: undefined,
  showTodayString: false,
};

export default PeriodLabel;
