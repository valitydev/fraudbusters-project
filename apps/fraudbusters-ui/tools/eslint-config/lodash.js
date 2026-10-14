'use strict';

module.exports = {
    plugins: ['you-dont-need-lodash-underscore'],
    extends: ['plugin:you-dont-need-lodash-underscore/compatible'],
    rules: {
        'you-dont-need-lodash-underscore/is-nil': 'off',
    },
};
