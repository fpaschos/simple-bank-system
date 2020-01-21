import {useEffect, useReducer} from 'react';
import {AccountHistory} from "../model/model";

const emptyHistory: AccountHistory = {
    accountId: '',
    series: [],
    size: 0,
    startOffset: 0,
    endOffset: 0
};


interface Action {
    type: 'call' | 'result',
    id: string,
    initialOffset: number,
    payload?: AccountHistory
}

interface State {
    accountId: string,
    beginOffset: number,
    lastOffset: number,
    result: AccountHistory,
    request: number,
}

const NOOP = () => {
};

const fetchAccountHistory = (id: string, offset: number): Promise<AccountHistory> => {
    return fetch(`account/${id}/history?offset=${offset}`)
        .then(resp => resp.json())
        .catch(error => console.log(error));
};

const initial = (id: string, offset: number) => {
    return  {
        accountId: id,
        result: emptyHistory,
        beginOffset: offset,
        lastOffset: offset,
        request: 0
    } as State
};

const reducer: React.Reducer<State, Action> = (state, action) => {
    switch (action.type) {
        case 'call': {
            const {id, initialOffset} = action;
            if(state.accountId === id) {
                return {...state, lastOffset: state.result.endOffset, request: state.request + 1}
            } else {
                return initial(id, initialOffset)
            }

        }
        case 'result': {
            if (action.payload) {
                return {...state, result: action.payload}
            }
            return state;
        }
        default:
            return state;
    }
};

const useAccountHistoryByIdService: (id: string, offset?: number, every?: number) => AccountHistory =
    (id: string, offset?: number, every?: number) => {

        const initialOffset = offset ? offset : 0;
        const [state, dispatch] = useReducer(reducer, initial(id, initialOffset));
        const {accountId, result, lastOffset, request} = state;

        useEffect(() => {
            fetchAccountHistory(accountId, lastOffset)
                .then((payload: AccountHistory) => {
                    dispatch({type: 'result', id: payload.accountId, initialOffset, payload})
                })
        }, [accountId, initialOffset, lastOffset, request, dispatch]);

        useEffect(() => {
            if (every) {
                dispatch({type: 'call', id, initialOffset});
                const timer = setInterval(() => {
                    dispatch({type: 'call', id, initialOffset});
                }, every);
                return () =>  {
                    clearTimeout(timer);
                }
            } else {
                dispatch({type: 'call', id, initialOffset});
                return NOOP;
            }

        }, [every, dispatch, id, initialOffset]);

        return result;
    };

export default useAccountHistoryByIdService;
