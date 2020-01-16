import {useEffect, useState} from 'react';
import {AccountBalance} from "../model/model";


const useAccountHistoryByIdService: (id?: string) => AccountBalance[] =
    (id?: string) => {
        const [result, setResult] = useState<AccountBalance[]>([]);

        const fetchAccountHistory = (id: string) => {
            fetch(`account/${id}/history`)
                .then(resp => resp.json())
                .then(resp => setResult(resp))
                .catch(error => console.log(error));
        };

        useEffect(() => {
            if (id) {
                fetchAccountHistory(id);
            }
            return () => {
            }

        }, [id]);


        return result;
    };
export default useAccountHistoryByIdService;
