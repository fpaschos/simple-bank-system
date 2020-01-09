import React, {FunctionComponent, useEffect, useState} from 'react';
import useAccountByIdService, {AccountBalance} from "../services/AccountByIdService";
import {usePrevious} from "../services/hooks";
import AccountPlot from './AccountPlot';

export interface Props {
    accountId?: string;
}


const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const balance = useAccountByIdService(props.accountId, 2000);

    const prevBalance = usePrevious(balance) as AccountBalance;

    const [balances, setBalances] = useState<AccountBalance[]>([]);

    const plotData = balances.map(b => ({y: b.balance, x: b.at}));

    useEffect(()  => {
        if(!prevBalance) {
            setBalances([...balances, balance]);
            return
        }

        if(prevBalance.accountId !== balance.accountId) {
            setBalances([]);
            return
        }

        setBalances([...balances, balance]);

    },[balance]);

    return (
        <>
            {props.accountId && (
                <>
                    <p>Account: {balance.accountId}</p>
                    <p>Balance: {balance.balance}</p>

                    {plotData && <AccountPlot data={plotData}/> }

                </>
            )}

            {!props.accountId &&
                <p> Please select an account! </p>
            }
        </>
    );
};

export default AccountDetails;
