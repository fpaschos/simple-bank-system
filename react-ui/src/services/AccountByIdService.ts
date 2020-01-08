import {useEffect, useState} from 'react';

interface AccountBalance {
    accountId: string,
    balance: number
}

const useAccountByIdService = (id?: string, every ?: number) => {
    const [result, setResult] = useState<AccountBalance>(
        {accountId: '', balance: 0.0}
    );


    const fetchAccountBalance = (id: string) => {
        fetch(`account/${id}`)
            .then(resp => resp.json())
            .then(resp => setResult(resp))
            .catch(error => console.log(error));

    };

    useEffect(() => {
        if(id) {
            if (every) {
                fetchAccountBalance(id); // First call
                const timer = setInterval(() => fetchAccountBalance(id), every); // Interval for thre rest
                return () => clearTimeout(timer)
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
