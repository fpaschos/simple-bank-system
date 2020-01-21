import React, {FunctionComponent, useEffect, useReducer} from 'react';
import useAccountByIdService from "../services/AccountByIdService";
import AccountPlot from './js/AccountPlot';
import {AccountBalance, AccountHistory} from "../model/model";
import useAccountHistoryByIdService from "../services/AccountHistoryByIdService";
import moment from 'moment';


interface Action {
    type: 'append'
    payload: AccountHistory
}

interface State {
    accountId: string;
    balanceSeries: AccountBalance[];
}


const reducer: React.Reducer<State, Action> = (state, action) => {
    const {accountId, series} = action.payload;

    if (state.accountId !== accountId) {
        return {accountId, balanceSeries: series}
    } else {
        return {accountId, balanceSeries: [...state.balanceSeries, ...series]}
    }
};

const AccountDetailsGraph: FunctionComponent<Props> = (props: Props) => {
    const history = useAccountHistoryByIdService(props.accountId, 1, 2000);
    const [aggregatedHistory, dispatch] = useReducer(reducer, {accountId: '', balanceSeries: []});

    useEffect(() => {
        dispatch({type: 'append', payload: history})
    }, [history]);

    // Convert the updated history to (x,y) coordinates with date time
    const balanceSeries = aggregatedHistory.balanceSeries.map(b => ({y: b.balance, x: new Date(b.updated)}));

    return (
        <>
            {balanceSeries.length > 0 && <AccountPlot series={balanceSeries}/>}
        </>
    );
};

export interface Props {
    accountId: string;
}

export default AccountDetailsGraph;
