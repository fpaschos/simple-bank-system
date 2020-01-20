import {useCallback, useEffect, useReducer, useState} from 'react';
import {AccountHistory} from "../model/model";
import {act} from "react-dom/test-utils";


const NOOP = () => {
};

const fetchAccountHistory = (id: string, offset: number): Promise<AccountHistory> => {
    return fetch(`account/${id}/history?offset=${offset}`)
        .then(resp => resp.json())
        .catch(error => console.log(error));
};


const emptyHistory: AccountHistory = {
    series: [],
    size: 0,
    startOffset: 0,
    endOffset: 0
};


interface Action {
    type: 'call' | 'result',
    payload?: AccountHistory
}

interface State {
    result: AccountHistory,
    beginOffset: number,
    lastOffset: number | null,
    request: number,
}


const reducer: React.Reducer<State, Action> = (state, action) => {
    switch(action.type) {
        case 'call': {
            if(state.lastOffset === null){
                return {...state, lastOffset: state.beginOffset, request: state.request + 1}
            } else {
                return {...state, lastOffset: state.result.endOffset, request: state.request + 1}
            }
        }
        case 'result': {
            if(action.payload) {
                return {...state, result: action.payload}
            }
            return state;
        }
        default:
            return state
    }
};

const useAccountHistoryByIdService: (id?: string, offset?: number, every?: number) => AccountHistory =
    (id?: string, offset?: number, every?: number) => {
        const initial: State = {
            result: emptyHistory,
            beginOffset: offset ? offset : 1,
            lastOffset: null,
            request: 0
        };


        const [state, dispatch] = useReducer(reducer, initial);


        const {lastOffset, result, request} = state;

        useEffect(() => {
            if (id && lastOffset !== null) {
                fetchAccountHistory(id, lastOffset)
                    .then((resp: AccountHistory) => {
                       dispatch({type: 'result', payload: resp})
                    })
            }
        },[id, lastOffset, dispatch, request]);

        useEffect(() => {
            if (every) {
                dispatch({type: 'call'});
                const timer = setInterval(() => {
                        dispatch({type: 'call'});
                    }, every);
                return () => clearTimeout(timer);
            } else {
                dispatch({type: 'call'});
                return NOOP;
            }

        }, [every, dispatch]);

        return result;
    };

export default useAccountHistoryByIdService;
