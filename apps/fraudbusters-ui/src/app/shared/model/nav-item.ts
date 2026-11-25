export interface NavItem {
    displayName: string;
    disabled?: boolean;
    iconName?: string;
    route?: string;
    activeRoutes?: string[];
    children?: NavItem[];
    roles?: string[];
}
