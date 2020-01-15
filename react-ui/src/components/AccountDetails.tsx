import React, {FunctionComponent, useEffect, useState} from 'react';
import useAccountByIdService, {AccountBalance} from "../services/AccountByIdService";
import {usePrevious} from "../services/hooks";
import AccountPlot from './js/AccountPlot';

export interface Props {
    accountId?: string;
}


const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const balance = useAccountByIdService(props.accountId, 1000);

    const prevBalance = usePrevious(balance) as AccountBalance;

    const [balances, setBalances] = useState<AccountBalance[]>([]);

    const series = balances.map(b => ({y: b.balance, x: b.updated}));

    useEffect(()  => {
        if(!prevBalance) {
            setBalances(bs => [...bs, balance]);
            return
        }

        if(prevBalance.accountId !== balance.accountId) {
            setBalances([]);
            return
        }

        setBalances(bs => [...bs, balance]);

    },[prevBalance, balance]);

    return (
        <>
            {props.accountId && (
                <>
                    <h3>Account: {balance.accountId}</h3>
                    <h4>Balance: {balance.balance} &euro;</h4>

                    {series.length > 0  && <AccountPlot series={series}/> }
                </>
            )}

            {!props.accountId &&
                <p> Please select an account! </p>
            }
        </>
    );
};

export default AccountDetails;
