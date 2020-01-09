import React from 'react';
import {FunctionComponent} from 'react';
import Select from 'react-select';
import {selectDarkStyles, selectDarkTheme} from '../styles/DashboardStyles';

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
            <h3>Online accounts: {options.length}</h3>
            <Select options={options}
                    onChange={(acc: any) => {
                        if (acc) props.onSelect(acc.value)
                    }}
                    theme={selectDarkTheme}
                    styles={selectDarkStyles}/>
        </>
    );
};

export default AccountList;
