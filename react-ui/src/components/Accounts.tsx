import React from 'react';
import {FunctionComponent} from 'react';
import useAccountsService from "../services/AccountsService";


const Accounts: FunctionComponent = () => {
    const service = useAccountsService();


    return(
        <>
            <div>Accounts Here</div>
            <p>{service.length}</p>
        </>
    );
}

export default Accounts;
