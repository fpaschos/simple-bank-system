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
    type: 'call' | 'result' | 'clear',
    id?: string,
    offset?: number,
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
    return <State>{
        accountId: id,
        result: emptyHistory,
        beginOffset: offset,
        lastOffset: offset,
        request: 0
    }
}

const reducer: React.Reducer<State, Action> = (state, action) => {
    switch (action.type) {
        case 'call': {
            const {id} = action;
            if(id) {
                return {...state, lastOffset: state.result.endOffset, request: state.request + 1}
            }
            return state;

        }
        case 'result': {
            if (action.payload) {
                return {...state, result: action.payload}
            }
            return state;
        }
        case 'clear': {
            const {id, offset} = action;
            if(id && offset) {
                return initial(id, offset)

            }
        }
        default:
            return state;
    }
};

const useAccountHistoryByIdService: (id: string, offset?: number, every?: number) => AccountHistory =
    (id: string, offset?: number, every?: number) => {


        const [state, dispatch] = useReducer(reducer, initial(id, offset ? offset : 1));


        const {accountId, result, lastOffset, request} = state;

        useEffect(() => {
            fetchAccountHistory(id, lastOffset)
                .then((resp: AccountHistory) => {
                    dispatch({type: 'result', payload: resp})
                })
        }, [accountId, lastOffset, request, dispatch]);

        useEffect(() => {
            dispatch({type: 'clear', id, offset});
            if (every) {
                const timer = setInterval(() => {
                    dispatch({type: 'call', id});
                }, every);
                return () => clearTimeout(timer);
            } else {
                dispatch({type: 'call', id});
                return NOOP;
            }

        }, [every, dispatch, id]);

        return result;
    };

export default useAccountHistoryByIdService;
