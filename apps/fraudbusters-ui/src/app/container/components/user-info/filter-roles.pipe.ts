import { Pipe, PipeTransform } from '@angular/core';

import { Roles } from '../../../auth';

/**
 * Fraudbusters roles the UI knows, in display order. Keycloak service roles
 * (default-roles-*, offline_access, uma_authorization, …) are left out.
 */
export const APP_ROLES: readonly string[] = [
    Roles.FraudOfficer,
    Roles.FraudMonitoring,
    'fraud-auditor',
    Roles.FraudSupport,
];

@Pipe({
    name: 'filterRoles',
})
export class FilterRolesPipe implements PipeTransform {
    transform(roles: string[] | null | undefined): string[] {
        const owned = new Set(roles ?? []);
        return APP_ROLES.filter((role) => owned.has(role));
    }
}
