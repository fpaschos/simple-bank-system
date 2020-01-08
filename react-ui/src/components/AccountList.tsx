import React from 'react';
import {FunctionComponent} from 'react';

export interface Props {
    accounts: string[];
    selected: string;
    onSelect(accountId: string): void;
}

const AccountList: FunctionComponent<Props> = (props: Props) => {
    const accounts = props.accounts;
    return (
        <>
            <div>Online accounts: {accounts.length}</div>
            <select name="select" onChange={(el) => props.onSelect(el.target.value)}>
                <option value=''>Select an account</option>
                {
                    accounts
                        .sort()
                        .map(acc => <option value={acc} key={acc} selected={props.selected == acc}>{acc}</option>)
                }
            </select>
        </>
    );
}

export default AccountList;
