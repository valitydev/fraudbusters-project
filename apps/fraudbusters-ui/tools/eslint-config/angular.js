'use strict';

module.exports = {
    plugins: ['@angular-eslint', 'import', '@typescript-eslint'],
    extends: [
        'plugin:@angular-eslint/recommended',
        'plugin:@angular-eslint/template/process-inline-templates',
        require.resolve('./typescript.js'),
    ],
};
