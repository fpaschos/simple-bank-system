import React, {useState} from 'react';
import {FunctionComponent} from 'react';
import useAccountsService from "../services/AccountsService";
import AccountList from "./AccountList";
import AccountDetails from "./AccountDetails";


const Dashboard: FunctionComponent = () => {
    const accounts = useAccountsService(2000);
    const [selected, setSelected] = useState('');

    return (
        <>
            <AccountList accounts={accounts} selected={selected} onSelect={(accountId) =>  {setSelected(accountId)}}/>
            <AccountDetails accountId={selected}/>
        </>
    );
};

export default Dashboard;
