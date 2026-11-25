export interface Series {
    name?: string;
    color?: string;
    data: {
        x: string | number | Date;
        y: string | number;
    }[];
}

export interface ChartData {
    currency: string;
    series: Series[];
    categorical?: boolean;
}
