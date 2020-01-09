import React, {useState} from 'react';
import {FunctionComponent} from 'react';
import useAccountsService from "../services/AccountsService";
import AccountList from "./AccountList";
import AccountDetails from "./AccountDetails";
import {Box, BoxContainer} from '../styles/DashboardStyles';


const Dashboard: FunctionComponent = () => {
    const accounts = useAccountsService(2000);
    const [selected, setSelected] = useState('');

    return (
        <BoxContainer>
            <Box w={50}>
                <AccountList accounts={accounts} selected={selected} onSelect={(accountId) =>  {setSelected(accountId)}}/>
            </Box>
            <Box w={50}>
                <AccountDetails accountId={selected}/>
            </Box>
        </BoxContainer>
    );
};

export default Dashboard;
