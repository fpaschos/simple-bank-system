import {useEffect, useState} from 'react';


const useAccountsService = () => {
    const [result, setResult] = useState<Array<String>>(
        []
    );


    useEffect(() => {
        fetch('accounts')
            .then(resp => resp.json())
            .then(resp => setResult(resp))

    }, []);

    return result;
};


export default useAccountsService;
