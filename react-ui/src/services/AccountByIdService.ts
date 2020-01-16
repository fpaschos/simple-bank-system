import {useEffect, useState} from 'react';
import {AccountBalance} from "../model/model";



const useAccountByIdService: (id?: string, every?: number) => AccountBalance =
    (id?: string, every ?: number) => {
        const [result, setResult] = useState<AccountBalance>(
            {accountId: '', balance: 0.0, updated: 0}
        );

        const fetchAccountBalance = (id: string) => {
            fetch(`account/${id}`)
                .then(resp => resp.json())
                .then(resp => setResult(resp))
                .catch(error => console.log(error));
        };

        useEffect(() => {
            if (id) {
                if (every) {
                    fetchAccountBalance(id); // First call
                    const timer = setInterval(() => fetchAccountBalance(id), every);
                    return () => clearTimeout(timer);
                } else {
                    fetchAccountBalance(id);
                    return () => {
                    }
                }
            }
        }, [id, every]);


        return result;
    };
export default useAccountByIdService;
