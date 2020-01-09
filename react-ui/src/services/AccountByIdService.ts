import {useEffect, useState} from 'react';

export interface AccountBalance {
    accountId: string,
    balance: number,
    at: Date
}


const useAccountByIdService: (id?: string, every?: number) => AccountBalance =
    (id?: string, every ?: number) => {
        const [result, setResult] = useState<AccountBalance>(
            {accountId: '', balance: 0.0, at: new Date()}
        );


        const fetchAccountBalance = (id: string) => {
            fetch(`account/${id}`)
                .then(resp => resp.json())
                .then(resp => setResult({...resp, at: new Date()}))
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
