'use strict';

module.exports = {
    /**
     * @param prefix app
     */
    createAngularSelectorRules({ prefix } = {}) {
        return {
            '@angular-eslint/directive-selector': [
                'error',
                {
                    type: 'attribute',
                    prefix: prefix,
                    style: 'camelCase',
                },
            ],
            '@angular-eslint/component-selector': [
                'error',
                {
                    type: 'element',
                    prefix: prefix,
                    style: 'kebab-case',
                },
            ],
        };
    },
    /**
     * @param internalPathsPattern @app/**
     */
    createImportOrderRule({ internalPathsPattern } = {}) {
        return {
            'import/order': [
                'error',
                {
                    groups: [['builtin', 'external'], 'internal', ['parent', 'sibling', 'index'], 'object'],
                    pathGroups: internalPathsPattern
                        ? [
                              {
                                  pattern: internalPathsPattern,
                                  group: 'internal',
                              },
                          ]
                        : [],
                    pathGroupsExcludedImportTypes: ['builtin'],
                    'newlines-between': 'always',
                    alphabetize: {
                        order: 'asc',
                        caseInsensitive: true,
                    },
                },
            ],
        };
    },
};
