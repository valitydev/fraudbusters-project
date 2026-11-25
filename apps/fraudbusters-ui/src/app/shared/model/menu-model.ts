import { NavItem } from './nav-item';
import { Roles } from '../../auth';

export class MenuModel {
    public static navItems: NavItem[] = [
        {
            displayName: 'Payments',
            iconName: 'credit_card',
            children: [
                {
                    displayName: 'Analytics',
                    iconName: 'pie_chart',
                    route: 'analytics',
                    roles: [Roles.FraudMonitoring],
                },
                {
                    displayName: 'Templates',
                    iconName: 'description',
                    route: 'templates',
                    activeRoutes: ['template', 'reference', 'default-references'],
                    roles: [Roles.FraudOfficer],
                },
                {
                    displayName: 'Groups',
                    iconName: 'group',
                    route: 'groups',
                    activeRoutes: ['group', 'create-group-reference'],
                    roles: [Roles.FraudOfficer],
                },
                {
                    displayName: 'Lists',
                    iconName: 'format_list_bulleted',
                    route: 'lists',
                    activeRoutes: ['list', 'candidates'],
                    roles: [Roles.FraudOfficer, Roles.FraudMonitoring],
                },
                {
                    displayName: 'Testing',
                    iconName: 'bug_report',
                    route: 'testing/data-sets',
                    activeRoutes: ['testing', 'emulation', 'data-sets'],
                    roles: [Roles.FraudOfficer],
                },
                {
                    displayName: 'Historical data',
                    iconName: 'schedule',
                    route: 'historical-data/payments',
                    activeRoutes: ['load'],
                    roles: [Roles.FraudOfficer, Roles.FraudMonitoring],
                },
            ],
        },
        {
            displayName: 'Payouts',
            iconName: 'account_balance_wallet',
            route: 'payouts',
            children: [
                {
                    displayName: 'Templates',
                    iconName: 'description',
                    route: 'payouts/templates',
                    activeRoutes: ['payouts/template', 'payouts/reference'],
                    roles: [Roles.FraudOfficer],
                },
                {
                    displayName: 'Groups',
                    iconName: 'group',
                    route: 'payouts/groups',
                    activeRoutes: ['payouts/group', 'payouts/group-reference'],
                    roles: [Roles.FraudOfficer],
                },
                {
                    displayName: 'Lists',
                    iconName: 'format_list_bulleted',
                    route: 'payouts/lists',
                    activeRoutes: ['payouts/list'],
                    roles: [Roles.FraudOfficer, Roles.FraudMonitoring],
                },
                {
                    displayName: 'Historical data',
                    iconName: 'schedule',
                    route: 'historical-data/payouts',
                    roles: [Roles.FraudOfficer, Roles.FraudMonitoring],
                },
            ],
        },
        {
            displayName: 'Notifications',
            iconName: 'notifications_none',
            route: 'notifications',
            activeRoutes: ['notification', 'channel'],
            roles: [Roles.FraudOfficer],
        },
        {
            displayName: 'Audit',
            iconName: 'article',
            route: 'audit',
            roles: [Roles.FraudOfficer, Roles.FraudMonitoring],
        },
    ];
}
