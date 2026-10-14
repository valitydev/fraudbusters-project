'use strict';

module.exports = {
    plugins: ['jasmine', '@typescript-eslint'],
    extends: ['plugin:jasmine/recommended'],
    rules: {
        'jasmine/new-line-before-expect': 'off',
        '@typescript-eslint/no-floating-promises': 'off',
    },
};
