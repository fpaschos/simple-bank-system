export interface AccountBalance {
    accountId: string,
    balance: number,
    updated: number
}

export interface AccountHistory {
    series: AccountBalance[];
    size: number;
    startOffset: number;
    endOffset: number;
}