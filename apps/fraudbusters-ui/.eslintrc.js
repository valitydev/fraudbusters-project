const rules = require('./tools/eslint-config/rules');

const baseTsRules = {
    parserOptions: {
        project: ['tsconfig.json'],
        createDefaultProgram: true,
    },
    extends: [
        './tools/eslint-config/typescript',
        './tools/eslint-config/angular',
        './tools/eslint-config/lodash',
        'prettier',
    ],
    rules: {
        ...rules.createImportOrderRule({ internalPathsPattern: '@fb/**' }),
        // TODO: pretenders for error
        '@typescript-eslint/no-unsafe-call': 'warn',
        '@typescript-eslint/no-unsafe-member-access': 'warn',
        '@typescript-eslint/no-unsafe-assignment': 'warn',
        '@typescript-eslint/no-unsafe-return': 'warn',
        '@typescript-eslint/no-misused-promises': 'warn',
        '@typescript-eslint/unbound-method': 'warn',
        '@typescript-eslint/restrict-plus-operands': 'warn',
        '@typescript-eslint/restrict-template-expressions': 'warn',
        '@typescript-eslint/no-unsafe-argument': 'warn',
    },
};

module.exports = {
    root: true,
    parser: '@typescript-eslint/parser',
    ignorePatterns: [
        '**/openapi-codegen/**/*.ts',
        '**/swagger-codegen/**/*.ts',
        'src/app/shared/components/inputs/custom-form-control/custom-form-control.component.ts',
        'src/app/shared/components/create-template/services/template/template.service.ts',
        'src/app/sections/default-reference/create-default-reference/services/create-default-payment-reference.service.ts',
        'src/app/shared/components/create-notification/services/notification.service.ts',
        'src/app/sections/testing/services/payment-data-sets/remove-data-sets.service.ts',
        'src/app/sections/templates/services/remove-payment-template.service.ts',
        'src/app/sections/templates/services/remove-reference.service.ts',
        'src/app/sections/templates/services/remove-default-reference.service.ts',
        'src/app/sections/create-group-reference/services/create-payment-reference.service.ts',
        'src/app/sections/reference/create-reference/services/create-payment-reference.service.ts',
        'src/app/sections/groups/services/fetch-references/fetch-references.service.ts',
        'src/app/shared/components/create-channel/services/channel.service.ts',
        'src/app/sections/notifications/services/fetch-notifications.service.ts',
        'src/app/sections/notifications/services/remove-channels.service.ts',
        'src/app/sections/notifications/services/archive-notification.service.ts',
        'src/app/sections/notifications/notifications.component.ts',
        'src/app/sections/load/fraud-uploader/dnd.directive.ts',
        'src/app/sections/groups/services/remove-reference/remove-reference.service.ts',
        'src/app/shared/components/testing-data-set-list/services/data-set/testing-analytics.service.ts',
    ],
    overrides: [
        {
            ...baseTsRules,
            files: ['*.ts'],
            rules: {
                ...baseTsRules.rules,
                ...rules.createAngularSelectorRules({ prefix: 'fb' }),
                // TODO: pretenders for error
                '@typescript-eslint/no-floating-promises': 'warn',
            },
        },
        {
            ...baseTsRules,
            files: ['*.spec.ts'],
            extends: [...baseTsRules.extends, './tools/eslint-config/jasmine'],
        },
        {
            files: ['*.html'],
            extends: ['plugin:@angular-eslint/template/recommended'],
            rules: {
                // TODO: pretenders for error
                '@angular-eslint/template/no-negated-async': 'warn',
            },
        },
    ],
};
