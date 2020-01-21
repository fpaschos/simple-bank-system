import React, {FunctionComponent, useEffect, useReducer} from 'react';
import AccountPlot from './js/AccountPlot';
import {AccountBalance, AccountHistory} from "../model/model";
import useAccountHistoryByIdService from "../services/AccountHistoryByIdService";
import FlexibleComponent from "./FlexibleComponent";


interface Action {
    type: 'append'
    payload: AccountHistory
}

interface State {
    accountId: string;
    offset: number;
    balanceSeries: AccountBalance[];
}


const reducer: React.Reducer<State, Action> = (state, action) => {
    const {accountId, series, endOffset} = action.payload;

    if (state.accountId !== accountId) {
        return {accountId, balanceSeries: series, offset: endOffset}
    } else {
        if(state.offset === endOffset) {

            return state
        }
        return {accountId, balanceSeries: [...state.balanceSeries, ...series], offset: endOffset}
    }
};

const AccountDetailsGraph: FunctionComponent<Props> = (props: Props) => {
    const history = useAccountHistoryByIdService(props.accountId, 0, 2000);
    const [aggregatedHistory, dispatch] = useReducer(reducer, {accountId: '', offset: 0,  balanceSeries: []});

    useEffect(() => {
        dispatch({type: 'append', payload: history})
    }, [history]);

    // Convert the updated history to (x,y) coordinates with date time
    const balanceSeries = aggregatedHistory.balanceSeries.map(b => ({y: b.balance, x: new Date(b.updated)}));

    return (
        <>
            {balanceSeries.length > 0 && <FlexibleComponent>
                {{content: <AccountPlot series={balanceSeries}/> }}
            </FlexibleComponent>}
        </>
    );
};

export interface Props {
    accountId: string;
}

export default AccountDetailsGraph;
