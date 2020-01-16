import React, {FunctionComponent, useEffect, useState} from 'react';
import useAccountByIdService from "../services/AccountByIdService";
import {usePrevious} from "../services/hooks";
import AccountPlot from './js/AccountPlot';
import {AccountBalance} from "../model/model";
import useAccountHistoryByIdService from "../services/AccountHistoryByIdService";
import moment from  'moment';

export interface Props {
    accountId?: string;
}


const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const history = useAccountHistoryByIdService(props.accountId);
    const balance = useAccountByIdService(props.accountId, 2000);

    // const prevBalance = usePrevious(balance) as AccountBalance;

    const [balances, setBalances] = useState<AccountBalance[]>([]);

    const series = history.map(b => ({y: b.balance, x: new Date(b.updated)}));

    // useEffect(()  => {
    //     if(!prevBalance) {
    //         setBalances(bs => [...bs, balance]);
    //         return
    //     }
    //
    //     if(prevBalance.accountId !== balance.accountId) {
    //         setBalances([]);
    //         return
    //     }
    //
    //     setBalances(bs => [...bs, balance]);
    //
    // },[prevBalance, balance]);



    return (
        <>
            {props.accountId && (
                <>
                    <h3>Account: {balance.accountId}</h3>
                    <h5>Balance: {balance.balance} &euro; </h5>
                    <h5>Last Update: {moment(balance.updated).format("DD/MM/YYYY hh:mm a")} </h5>

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
