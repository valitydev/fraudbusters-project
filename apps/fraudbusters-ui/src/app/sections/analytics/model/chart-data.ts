export interface Series {
    name?: string;
    data: {
        x: string | number | Date;
        y: string | number;
        fillColor?: string;
        strokeColor?: string;
    }[];
}

export interface ChartData {
    currency: string;
    series: Series[];
}
