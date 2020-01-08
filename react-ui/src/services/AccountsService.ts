import {useEffect, useState} from 'react';


const useAccountsService = (every ?: number) => {
    const [result, setResult] = useState<Array<string>>(
        []
    );

    const fetchAccounts = () => {
        fetch('accounts')
            .then(resp => resp.json())
            .then(resp => setResult(resp))
            .catch( error => console.log(error));

    };

    useEffect(() => {
        if(every) {
            fetchAccounts(); // First call
            const timer = setInterval(fetchAccounts,  every); // Interval for thre rest
            return () => clearTimeout(timer)
        } else {
            fetchAccounts();
            return () => {}
        }

    }, [every]);

    return result;
};


export default useAccountsService;
