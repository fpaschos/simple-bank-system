import React, {FunctionComponent, useEffect, useState} from 'react';
import useAccountByIdService from "../services/AccountByIdService";
import AccountPlot from './js/AccountPlot';
import {AccountBalance, AccountHistory} from "../model/model";
import useAccountHistoryByIdService from "../services/AccountHistoryByIdService";
import moment from 'moment';

export interface Props {
    accountId?: string;
}


const AccountDetails: FunctionComponent<Props> = (props: Props) => {
    const history = useAccountHistoryByIdService(props.accountId, 1, 2000);
    const balance = useAccountByIdService(props.accountId, 2000);

    const [aggregatedHistory, setAggregatedHistory] = useState<AccountHistory>(history);



    // useEffect(() => {
    //     // Aggregate old and new samples for each iteration
    //     const updatedHistory = {
    //         size: aggregatedHistory.size + history.size,
    //         startOffset: aggregatedHistory.startOffset,
    //         endOffset: history.endOffset,
    //         series: [...aggregatedHistory.series, ...history.series]
    //
    //     };
    //     setAggregatedHistory(updatedHistory);
    // }, [history]);
    //

    // Convert the updated history to (x,y) coordinates with date time
    const series  = history.series.map(b => ({y: b.balance, x: new Date(b.updated)}));

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
