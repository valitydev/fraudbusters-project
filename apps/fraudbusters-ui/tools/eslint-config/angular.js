'use strict';

module.exports = {
    plugins: ['@angular-eslint', 'import', '@typescript-eslint'],
    extends: [
        'plugin:@angular-eslint/recommended',
        'plugin:@angular-eslint/template/process-inline-templates',
        require.resolve('./typescript.js'),
    ],
    rules: {
        // The app is still built from NgModules; the Angular 19 migration marks those declarations
        // `standalone: false`. Moving to standalone components is a separate refactor.
        '@angular-eslint/prefer-standalone': 'off',
        // Constructor injection is the established pattern here; inject() is a separate refactor.
        '@angular-eslint/prefer-inject': 'off',
    },
};
