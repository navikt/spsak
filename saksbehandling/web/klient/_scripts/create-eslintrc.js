const path = require('path');
const fs = require('fs');
const eslintConf = require(path.join(__dirname, '../eslint/eslintrc.dev'));
fs.writeFileSync(path.join(__dirname, '../.eslintrc'), JSON.stringify(eslintConf));
