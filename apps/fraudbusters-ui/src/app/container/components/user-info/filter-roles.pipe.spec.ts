import { FilterRolesPipe } from './filter-roles.pipe';

describe('FilterRolesPipe', () => {
    const pipe = new FilterRolesPipe();

    it('keeps only Fraudbusters roles, without Keycloak defaults', () => {
        expect(
            pipe.transform([
                'default-roles-fraudbusters-realm',
                'offline_access',
                'uma_authorization',
                'fraud-monitoring',
                'fraud-officer',
            ])
        ).toEqual(['fraud-officer', 'fraud-monitoring']);
    });

    it('returns nothing when the user has no Fraudbusters roles', () => {
        expect(pipe.transform(['default-roles-fraudbusters-realm'])).toEqual([]);
        expect(pipe.transform(undefined)).toEqual([]);
    });
});
