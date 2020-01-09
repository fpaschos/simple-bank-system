import React from 'react';
import {FunctionComponent} from 'react';
import Select from 'react-select';

export interface Props {
    accounts: string[];
    selected: string;

    onSelect(accountId: string): void;
}

const collator = new Intl.Collator(undefined, {numeric: true, sensitivity: 'base'});
const AccountList: FunctionComponent<Props> = (props: Props) => {
    const options = props.accounts
        .sort(collator.compare)
        .map(acc => ({value: acc, label: acc}));

    return (
        <>
            <div>Online accounts: {options.length}</div>
            <Select options={options} onChange={(acc:any) => {if(acc) props.onSelect(acc.value)}}/>
        </>
    );
};

export default AccountList;
