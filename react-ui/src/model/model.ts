export interface AccountBalance {
    accountId: string,
    balance: number,
    updated: number
}

export interface AccountHistory {
    accountId: string;
    series: AccountBalance[];
    size: number;
    startOffset: number;
    endOffset: number;
}
