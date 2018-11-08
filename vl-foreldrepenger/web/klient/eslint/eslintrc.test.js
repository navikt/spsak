const merge = require('webpack-merge');
const common = require('./eslintrc.common.js');

const OFF = 0, ERROR = 2;

const config = {
  settings: {
    "import/resolver": {
      webpack: {
        config: "webpack/webpack.test.js",
      },
    },
  },

  rules: {
    "no-unused-expressions": OFF,
    "no-debugger": ERROR,
	  "no-console": ERROR,
  },
};

module.exports = merge(common, config);
