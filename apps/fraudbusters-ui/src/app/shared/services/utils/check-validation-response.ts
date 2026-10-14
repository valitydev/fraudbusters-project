// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const checkValidateResponse = (response: any): string => {
    return response.validateResults?.length ? response.validateResults[0].errors[0] : 'All rules is valid';
};
